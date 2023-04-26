package catsExample.api

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.implicits.javasql._
import doobie.postgres._
import doobie.postgres.free.pgconnection
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant

trait KVRepository {
  import KVRepository._

  def test_get(): IO[String]
  def get(id: String): IO[Either[Exception, KVRecord]]
  def create(): IO[Int]

  def createCountry(): IO[Int]
  def listCountries(): IO[Seq[Country]]
}

object KVRepository {

  private val logger = Slf4jLogger.getLogger[IO]

  case class KVRecord(
      // UUID
      id: String,
      // createdAt: Option[Timestamp]
      description: Option[String],
      createdAt: Timestamp,
      updatedAt: Option[Timestamp]
  )

  // TODO: This should be split out...
  final case class Country(
      // Only 3 chars...
      code: String,
      name: String,
      population: Int
  )

  def apply(xa: Transactor[IO]): KVRepository =
    new KVRepository {

      // TODO: Split out to another repo and make methods for adding countries...

      // Create a random country
      override def createCountry(): IO[Int] = {
        val code = scala.util.Random.alphanumeric.take(3).mkString
        val name = scala.util.Random.alphanumeric.take(25).mkString
        val population = scala.util.Random.nextInt(500)
        logger.info(s"$code, $name, $population")
        // val country = Country(code, name, population)
        sql"""INSERT INTO
                  countries
                VALUES
                  ($code, $name, $population)
              """.update.run
          .transact(xa)
      }

      override def listCountries(): IO[Seq[Country]] =
        sql"""SELECT code, name, population
             |FROM countries
              """.stripMargin
          .query[Country]
          .stream
          .compile
          .toList
          .transact(xa)

      override def test_get(): IO[String] =
        sql"""SELECT id from kv_items WHERE id = '312aae5b-16aa-4835-9e2f-e27a15117993 '"""
          .query[String]
          .unique
          .transact(xa)

      override def get(id: String): IO[Either[Exception, KVRecord]] = {
        logger.info("What is going on..")
        val my_id = "312aae5b-16aa-4835-9e2f-e27a15117993 "
        sql"""
               SELECT
                 id, description, created_at, updated_at
               FROM
                 kv_items
              """
          .query[KVRecord]
          .stream
          .take(1)
          .compile
          .toList
          .map(recordList => {
            recordList match {
              case _: List[KVRecord] =>
                Right(recordList.head)
              case null =>
                Left(new RuntimeException("gotem"))
            }
          })
          .transact(xa)
        /*
              .handleErrorWith { case err: SQLException =>
                err.getSQLState() match {
                  case sqlstate.class42.UNDEFINED_TABLE.value => IO.none
                  case _ => {
                    logger.error(err.getMessage()) >>
                      IO.raiseError(err)
                  }
                }
              }
         */
      }

      override def create(): IO[Int] = {
        val now_sql = Fragment.const(s"${Instant.now()}")
        val uuid = Fragment.const(s"${java.util.UUID.randomUUID()}")

        return sql"""
              INSERT INTO
                kv_items (id, created_at)
              VALUES
                ('${uuid}', '$now_sql')
            """.update.run
          .transact(xa)
      }
    }
}
