package catsExample.api

import cats.data.OptionT
import cats.effect.IO
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant

class CountryService(
    repo: CountryRepository
) {

  private val logger = Slf4jLogger.getLogger[IO]

  // TODO: Make the response be the record that was inserted
  def createCountryReq(): IO[Option[CountryRoutes.CountryJson]] =
    repo
      .create()
      .map { _ =>
        // Some(CountryRoutes.CountryJson(r.code, r.name, r.population))
        Some(CountryRoutes.CountryJson("cdx", "whut-do", 385))
      }

  def listCountriesReq(): IO[Option[Seq[CountryRoutes.CountryJson]]] =
    for {
      countries <- repo
        .list()
        .handleErrors()
    } yield Some(
      countries.map(toCountryJson)
    )

  def toCountryJson(model: CountryRepository.Country): CountryRoutes.CountryJson =
    CountryRoutes.CountryJson(
      code = model.code,
      name = model.name,
      population = model.population
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

object CountryService {}
