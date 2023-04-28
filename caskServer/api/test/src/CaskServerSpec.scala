package caskServer.api

import io.undertow.Undertow
import munit.FunSuite

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

  // override def munitFixtures: Seq[Fixture[_]] = Seq(withServer)

  /*
    test("TodoMvcApi") - withServer(TodoMvcApi){ host =>
      requests.get(s"$host/list/all").text() ==>
        """[{"checked":true,"text":"Get started with Cask"},{"checked":false,"text":"Profit!"}]"""
      requests.get(s"$host/list/active").text() ==>
        """[{"checked":false,"text":"Profit!"}]"""
      requests.get(s"$host/list/completed").text() ==>
        """[{"checked":true,"text":"Get started with Cask"}]"""
   */

  test("should run") {
    val obtained = "works!"
    val expected = "does it?"

    assertNotEquals(obtained, expected)
    assertEquals(1, 1)
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
}
