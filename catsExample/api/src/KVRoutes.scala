package catsExample.api

import cats.data.OptionT
import cats.effect.IO
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
      timestamp: String
  )

  case class CountryJson(
      code: String,
      name: String,
      population: Int
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

  // TODO: Check if there can be multiple routes or maybe split them up
  // into different groups ..?
  def apply(service: KVService): HttpRoutes[IO] = {
    object dsl extends Http4sDsl[IO]; import dsl._

    HttpRoutes.of[IO] {
      case req @ POST -> Root / "countries" / "create" =>
        service
          .createCountryReq()
          .flatMap {
            case Some(resp) => Ok(resp)
            case None => {
              logger.info(s"Unable to create KV record...")
              NotFound()
            }
          }

      case req @ GET -> Root / "countries" =>
        service
          .handleCountriesReq()
          .flatMap {
            case Some(resp) => Ok(resp)
            case None => {
              logger.info(s"countries request returned no records...")
              NotFound()
            }
          }

      // Get a value from the store
      case req @ GET -> Root / id =>
        service
          .handleRequest(id)
          .flatMap {
            case Some(resp) => Ok(resp)
            case None => {
              logger.info(s"[$id] returned no record...")
              NotFound()
            }
          }

      case req @ POST -> Root / "create" =>
        service
          .handleCreate()
          .flatMap {
            case Some(resp) => Ok(resp)
            case None => {
              logger.info(s"Unable to create KV record...")
              NotFound()
            }
          }
    }
  }
}
