package catsExample.api

import cats.effect.IO
import doobie._
import doobie.implicits._
import doobie.postgres._
import doobie.postgres.free.pgconnection
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant

trait KVRepository {
  import KVRepository._

  def test_get(): IO[String]
  def get(id: String): IO[Option[String]]
  def create(): IO[Option[KVRecord]]
}

object KVRepository {

  private val logger = Slf4jLogger.getLogger[IO]

  case class KVRecord(
      // UUID
      id: String,
      createdAt: Option[Timestamp]
  )

  def apply(xa: Transactor[IO]): KVRepository =
    // logger.info("Instantiated my KVRepository...")
    new KVRepository {

      override def test_get(): IO[String] =
        fr"""SELECT id from kv_items WHERE id = 'abcd'"""
          .query[String]
          .unique
          .transact(xa)

      override def get(id: String): IO[Option[String]] = {
        logger.info("Starting get response...")
        val resp = PHC
          .pgGetConnection(pgconnection.escapeLiteral(""))
          .flatMap { schema =>
            // val kv_table = Fragment.const(s"""$schema."kv_items"""")
            // logger.info(kv_table.toString())
            fr"""
                SELECT
                  id, created_at
                FROM
                  kv_items
                WHERE
                  id = "abcd"
              """
              .query[KVRecord]
              .option
          }
          .transact(xa)
          .handleErrorWith { case err: SQLException =>
            err.getSQLState() match {
              case sqlstate.class42.UNDEFINED_TABLE.value => IO.none
              case _                                      => IO.raiseError(err)
            }
          }
        logger.info(s"GET RESPONSE: ${resp}")
        return IO(Some("Meep"))
      }

      override def create(): IO[Option[KVRecord]] = {
        logger.info("attempting create...")
        val now = Instant.now()
        val kvRecord: IO[Option[KVRecord]] = PHC
          .pgGetConnection(pgconnection.escapeLiteral(""))
          .flatMap { schema =>
            // val kv_table = Fragment.const(s"""$schema."kv_items"""")
            val now_sql = Fragment.const(s"${now}")
            logger.info(s"${now_sql}")
            fr"""
                INSERT INTO
                  kv_items (id, created_at)
                VALUES
                  ("meep", $now_sql)
              """
              .query[KVRecord]
              .option
          }
          .transact(xa)
          .handleErrorWith {
            case err: SQLException =>
              err.getSQLState() match {
                case sqlstate.class42.UNDEFINED_TABLE.value => IO.none
                case _ => IO.raiseError(err)
              }
            case e: Exception =>
              logger.error(s"meep meep ${e.getMessage()}")
              IO.raiseError(e)
          }
        return kvRecord
      }
    }
}
