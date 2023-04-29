package catsExample.api

import cats.data.OptionT
import cats.effect.IO
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.Method._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.io._
import org.typelevel.log4cats.slf4j.Slf4jLogger

class CountryRoutes(service: CountryService) {

  private val logger = Slf4jLogger.getLogger[IO]

  // Unsure what the implications are of not using an `apply` method here...
  val countryRoutes = HttpRoutes.of[IO] {
    case req @ POST -> Root =>
      service
        .createCountryReq()
        .flatMap {
          case Some(resp) => Ok(resp)
          case None => {
            logger.info(s"Unable to create country record...")
            NotFound()
          }
        }

    case req @ GET -> Root =>
      service
        .listCountriesReq()
        .flatMap {
          case Some(resp) => Ok(resp)
          case None => {
            logger.info(s"countries request returned no records...")
            NotFound()
          }
        }
  }
}

object CountryRoutes {

  case class CountryJson(
      code: String,
      name: String,
      population: Int
  )
}

object KVRoutes {

  private val logger = Slf4jLogger.getLogger[IO]

  case class KVResponse(
      id: Int,
      timestamp: String
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
