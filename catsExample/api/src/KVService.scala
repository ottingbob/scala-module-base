package catsExample.api

import cats.data.OptionT
import cats.effect.IO
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant

class KVService(
    repo: KVRepository
) {

  private val logger = Slf4jLogger.getLogger[IO]

  // DEBUG AND TRY NEW TABLE...
  def createCountryReq(): IO[Option[KVRoutes.CountryJson]] =
    repo
      .createCountry()
      .map { _ =>
        // Some(KVRoutes.CountryJson(r.code, r.name, r.population))
        Some(KVRoutes.CountryJson("cdx", "whut-do", 385))
      }

  def handleCountriesReq(): IO[Option[Seq[KVRoutes.CountryJson]]] =
    for {
      countries <- repo
        .listCountries()
        .handleErrors()
        .map(c =>
          KVRoutes.CountryJson(
            code = c.code,
            name = c.name,
            population = c.population
          )
        )
      // resp <- repo.test_get().handleErrors()
      _ <- logger.info("how is this still running")

      now <- IO(Instant.now())
      // _ <- logger.info(s"KV Record response: ${resp}")
    } yield Some(
      KVRoutes.KVResponse(
        // id = Integer(id),
        id = 5,
        timestamp = s"$now"
      )
    )

  def handleRequest(id: String): IO[Option[KVRoutes.KVResponse]] =
    for {
      _ <- logger.info("about to bomb out")
      resp <- repo.get(id).handleErrors()
      // resp <- repo.test_get().handleErrors()
      _ <- logger.info("how is this still running")

      now <- IO(Instant.now())
      // _ <- logger.info(s"KV Record response: ${resp}")
    } yield Some(
      KVRoutes.KVResponse(
        // id = Integer(id),
        id = 5,
        timestamp = s"$now"
      )
    )

  def handleCreate(): IO[Option[KVRoutes.KVResponse]] =
    for {
      record <- repo.create()
      now <- IO(Instant.now())
      _ <- logger.info(s"NEW KV Record created: ${record}")
    } yield Some(
      KVRoutes.KVResponse(
        id = Integer(5),
        timestamp = s"$now"
      )
    )

  private implicit class ErrorSyntax[A](op: IO[A]) {
    def handleErrors(): IO[A] = {
      logger.info("Handling errrors...")
      op.handleErrorWith { case e: NullPointerException =>
        logger.info(e.getMessage()) >>
          IO.raiseError(e)
      }
    }
  }
}

object KVService {}

object ErrorSyntax {
  implicit class ErrorSyntaxOps[E <: Throwable](err: E) {
    def stacktrace: String = {
      val sw = new StringWriter()
      err.printStackTrace(new PrintWriter(sw))
      sw.toString
    }
  }
}
