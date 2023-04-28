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
