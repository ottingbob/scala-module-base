package catsExample.api

import cats.data.OptionT
import cats.effect.{IO}
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.{Instant}

class KVService {

  private val logger = Slf4jLogger.getLogger[IO]

  def handleRequest(id: String): IO[Option[KVRoutes.KVResponse]] = 
    for {
      now <- IO(Instant.now())
    } yield Some(KVRoutes.KVResponse(
      id = Integer(id),
      timestamp = s"$now"
    ))

}

object KVService {}

