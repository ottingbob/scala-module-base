package catsExample.api

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.free.pgconnection
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.sql.SQLException

trait KVRepository {
  import KVRepository._

  def get(id: String): IO[Option[String]]
}

object KVRepository {

  private val logger = Slf4jLogger.getLogger[IO]

  def apply(xa: Transactor[IO]): KVRepository =
    new KVRepository {
      override def get(id: String): IO[Option[String]] = {
        PHC
          .pgGetConnection(pgconnection.escapeLiteral(""))
          .flatMap {
            schema =>
              val kv_table = Fragment.const(s"""$schema."kv_items"""")
              fr"""
                SELECT
                  id
                FROM
                  $kv_table
                WHERE
                  id = 1
              """
              .query[Boolean]
              .option
          }
          .transact(xa)
          .handleErrorWith {
            case err: SQLException =>
              err.getSQLState() match {
                case sqlstate.class42.UNDEFINED_TABLE.value => IO.none
                case _ => IO.raiseError(err)
              }
          }
        return IO(Some("Meep"))
    }
  }
}

