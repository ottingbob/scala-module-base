package catsExample.api

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Resource
import com.comcast.ip4s.ipv4
import com.comcast.ip4s.Port
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.Response
import org.http4s.Status
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  private val logger = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = serve().void

  private def serve(): IO[Unit] = {
    logger.info("Starting KV Store API")
    EmberServerBuilder.default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(Port.fromInt(8080).get)
      .withHttpApp(
        Router("/store" -> KVRoutes(new KVService())).orNotFound
      )
      .withErrorHandler{
        case err =>
          logger.error(s"KV Store Api: $err")
            .as(Response(status = Status.InternalServerError))
      }
      .build
      .useForever
  }

  /*
   * TODO: Hook in resources later
   *
    import com.typesafe.config.ConfigFactory

    private def resources: Resource[IO, Resources] = for {
      config <- Resource.eval(IO(ConfigFactory.load()))
    }
  */
}

