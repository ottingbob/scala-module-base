package catsExample.api

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Resource
import com.comcast.ip4s.Port
import com.comcast.ip4s.ipv4
import com.typesafe.config.ConfigFactory
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.http4s.Response
import org.http4s.Status
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.io.PrintWriter
import java.io.StringWriter

object Main extends IOApp.Simple {

  private val logger = Slf4jLogger.getLogger[IO]

  // Our resources define our dependencies when constructing the app.
  // In this case we need to load in a repository and the application
  // configuration to connect to it.
  private case class Resources(
      kvRepo: KVRepository,
      countryRepo: CountryRepository
  )

  def run: IO[Unit] =
    // TODO: If there are errors loading resources consider doing a retry
    resources.use { resources =>
      logger.info("Resources successfully loaded") >>
        serve(resources).void
    }

  private def serve(resources: Resources): IO[Unit] = {
    logger.info(s"Starting KV Store API") >>
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(Port.fromInt(8080).get)
        .withHttpApp(
          Router(
            "/store" -> KVRoutes(new KVService(resources.kvRepo)),
            "/countries" -> CountryRoutes(new CountryService(resources.countryRepo)).countryRoutes
          ).orNotFound
        )
        .withErrorHandler {
          case err => {
            val sw = new StringWriter()
            err.printStackTrace(new PrintWriter(sw))
            logger
              .error(s"KV Store Api: ${sw.toString}")
              .as(Response(status = Status.InternalServerError))
          }
        }
        .build
        .useForever
  }

  private def resources: Resource[IO, Resources] =
    for {
      // TODO: Hook in resources later
      // config <- Resource.eval(IO(ConfigFactory.load()))
      ce <- ExecutionContexts.fixedThreadPool[IO](8)
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        // LOAD THESE VALUES INTO THE CONFIG
        // "jdbc:postgresql://0.0.0.0:5432/DB_NAME?sslmode=disable",
        // When running in docker need to call out the docker container name...
        "jdbc:postgresql://scala-module-base-postgres-1:5432/kv-store?sslmode=disable",
        "postgres-user",
        "postgres-pass",
        ce
      )
    } yield Resources(
      kvRepo = KVRepository(xa),
      countryRepo = CountryRepository(xa)
    )
}
