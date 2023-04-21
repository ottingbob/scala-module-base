package catsExample.api

import cats.data.OptionT
import cats.effect.{IO}
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.Method._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.slf4j.Slf4jLogger

object KVRoutes {

  private val logger = Slf4jLogger.getLogger[IO]

  case class KVResponse(
    id: Int,
    timestamp: String,
  )

  /*
   * To explicitly define the encoder:
   *
   * import io.circe.Encoder
   * import io.circe.literal._
   *
    private implicit val kvResponseEncoder: Encoder[this.KVResponse] =
      Encoder.instance {
        (resp: this.KVResponse) => 
          json"""{
            "timestamp": ${resp.timestamp}
          }"""
      }
  */

  def apply(service: KVService): HttpRoutes[IO] = {
    object dsl extends Http4sDsl[IO]; import dsl._

    HttpRoutes.of[IO] {
      // Get a value from the store
      case req @ GET -> Root / id =>
        service.handleRequest(id)
          .flatMap {
            case Some(resp) => Ok(resp)
            case None => {
              logger.info(s"[$id] returned no record...")
              NotFound()
            }
          }
    }
  }
}
