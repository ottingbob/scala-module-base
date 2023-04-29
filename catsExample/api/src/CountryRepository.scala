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

trait CountryRepository {
  import CountryRepository._

  def create(): IO[Int]
  def list(): IO[Seq[Country]]
}

object CountryRepository {

  private val logger = Slf4jLogger.getLogger[IO]

  final case class Country(
      // Only 3 chars...
      code: String,
      name: String,
      population: Int
  )

  def apply(xa: Transactor[IO]): CountryRepository =
    new CountryRepository {

      // Create a random country
      override def create(): IO[Int] = {
        val code = scala.util.Random.alphanumeric.take(3).mkString
        val name = scala.util.Random.alphanumeric.take(25).mkString
        val population = scala.util.Random.nextInt(500)
        logger.info(s"$code, $name, $population")

        // TODO: Construct the country and return it instead...
        // val country = Country(code, name, population)
        sql"""INSERT INTO
                  countries
                VALUES
                  ($code, $name, $population)
              """.update.run
          .transact(xa)
      }

      override def list(): IO[Seq[Country]] =
        sql"""SELECT code, name, population
             |FROM countries
              """.stripMargin
          .query[Country]
          .stream
          .compile
          .toList
          .transact(xa)

    }
}
