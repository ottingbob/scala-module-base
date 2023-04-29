package caskServer.api

import io.undertow.Undertow
import munit.FunSuite
import requests._

import java.util.UUID

class CaskServerSpec extends FunSuite {

  def withServer[T](caskServer: cask.main.Main)(f: String => T): T = {
    val server = Undertow.builder
      .addHttpListener(8081, "localhost")
      .setHandler(caskServer.defaultHandler)
      .build
    server.start()

    try f("http://localhost:8081")
    finally server.stop()
  }

  // TODO: Optionally use an munit fixture
  // override def munitFixtures: Seq[Fixture[_]] = Seq(withServer)

  test("should respond to hello on the base route") {
    withServer(MinimalApp) { host =>
      assertEquals(
        requests.get(s"$host/").text(),
        "Hello World!"
      )
    }
  }

  test("should echo headers") {
    val expected = s"""accept: */*
                      |user-agent: requests-scala
                      |host: localhost:8081
                      |cache-control: no-cache
                      |pragma: no-cache
                      |connection: keep-alive
                      |accept-encoding: gzip, deflate""".stripMargin

    withServer(MinimalApp) { host =>
      assertEquals(
        requests.get(s"$host/headers").text().split("\n").reduce((r, p) => s"$r,$p"),
        expected.split("\n").reduce((r, p) => s"$r,$p")
      )
    }
  }

  test("should reverse post body") {
    val toReverse = scala.util.Random.alphanumeric.take(25).mkString
    withServer(MinimalApp) { host =>
      assertEquals(
        requests.post(s"$host/reverse", data = toReverse).text(),
        toReverse.reverse
      )
    }
  }

  test("should list movies") {
    withServer(MinimalApp) { host =>
      assertEquals(
        requests.get(s"$host/movies").text(),
        upickle.default.write(
          MinimalDb.database
            .map((key: UUID, movie: Model.Movie) => movie)
        )
      )
    }
  }

  test("should fail on a non-existent movie") {
    withServer(MinimalApp) { host =>
      val invalidUUID = UUID.randomUUID()
      /*
       * TODO: Figure out how to get the expected HTTP failure response from the server
       *
        interceptMessage[requests.RequestFailedException](
          "failed with invalid movie data: java.util.NoSuchElementException: None.get"
        )
       */
      intercept[requests.RequestFailedException] {
        requests
          .get(s"$host/movies/$invalidUUID")
      }
    }
  }

  test("should create a movie") {
    withServer(MinimalApp) { host =>
      val response: requests.Response = requests.post(
        s"$host/movies",
        data = s"""{
            "name": "test above the rest",
            "director": "mr.testo",
            "year": 3004
          }"""
      )
      val createdMovieId = ujson.read(response.data.toString)("id").str

      assertEquals(
        requests.get(s"$host/movies/$createdMovieId").text(),
        upickle.default.write(
          MinimalDb.database
            .get(UUID.fromString(createdMovieId))
            .get
        )
      )
    }
  }
}
