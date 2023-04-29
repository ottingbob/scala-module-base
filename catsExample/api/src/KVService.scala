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

  // TODO: Actually handle the get on the provided ID
  def handleRequest(id: String): IO[Option[KVRoutes.KVResponse]] =
    for {
      resp <- repo.get(id).handleErrors()
      now <- IO(Instant.now())
    } yield Some(
      KVRoutes.KVResponse(
        id = 5,
        timestamp = s"$now"
      )
    )

  // TODO: Actually return the created record from the db
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
