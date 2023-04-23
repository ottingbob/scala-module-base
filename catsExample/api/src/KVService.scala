package catsExample.api

import cats.data.OptionT
import cats.effect.IO
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.Instant

class KVService(
    repo: KVRepository
) {

  private val logger = Slf4jLogger.getLogger[IO]

  def handleRequest(id: String): IO[Option[KVRoutes.KVResponse]] =
    for {
      // resp <- repo.get(id)
      resp <- repo.test_get()
      now <- IO(Instant.now())
      _ <- logger.info(s"we got the repo resp: ${resp}")
    } yield Some(
      KVRoutes.KVResponse(
        id = Integer(id),
        timestamp = s"$now"
      )
    )

  def handleCreate(): IO[Option[KVRoutes.KVResponse]] =
    for {
      record <- repo.create()
      now <- IO(Instant.now())
      _ <- logger.info(s"we got the record created!!: ${record}")
    } yield Some(
      KVRoutes.KVResponse(
        id = Integer(5),
        timestamp = s"$now"
      )
    )
}

object KVService {}
