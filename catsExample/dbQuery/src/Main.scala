import cats.Show
import cats.syntax.all._
import cats.effect.{IO, IOApp}
import fs2.Stream
import doobie._, doobie.implicits._

// Adapted from this example:
// https://github.com/tpolecat/doobie/blob/main/modules/example/src/main/scala/example/FirstExample.scala
object Main extends IOApp.Simple {

  // Our data model
  final case class Supplier(
      id: Int,
      name: String,
      street: String,
      city: String,
      state: String,
      zip: String
  )
  final case class Coffee(name: String, supId: Int, price: Double, sales: Int, total: Int)
  object Coffee {
    implicit val show: Show[Coffee] = Show.fromToString
  }

  final case class Coffee(code: String,name: Int, population:Int , sales: Int, total: Int)

  // Some suppliers
  val suppliers = List(
    Supplier(101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"),
    Supplier(49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460"),
    Supplier(150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966")
  )

  // Some coffees
  val coffees = List(
    Coffee("Colombian", 101, 7.99, 0, 0),
    Coffee("French_Roast", 49, 8.99, 0, 0),
    Coffee("Espresso", 150, 9.99, 0, 0),
    Coffee("Colombian_Decaf", 101, 8.99, 0, 0),
    Coffee("French_Roast_Decaf", 49, 9.99, 0, 0)
  )

  /** DAO module provides ConnectionIO constructors for end users. */
  object DAO {

    def coffeesLessThan(price: Double): Stream[ConnectionIO, (String, String)] =
      Queries.coffeesLessThan(price).stream

    def insertSuppliers(ss: List[Supplier]): ConnectionIO[Int] =
      Queries.insertSupplier.updateMany(ss) // bulk insert (!)

    def insertCoffees(cs: List[Coffee]): ConnectionIO[Int] =
      Queries.insertCoffee.updateMany(cs)

    def allCoffees: Stream[ConnectionIO, Coffee] =
      Queries.allCoffees.stream

    def create: ConnectionIO[Unit] =
      Queries.create.run.void

    def allCountries: Stream[ConnectionIO, Country]

  }

  // Our example database action
  def examples: ConnectionIO[String] =
    for {
      // Create and populate
      _ <- DAO.create
      ns <- DAO.insertSuppliers(suppliers)
      nc <- DAO.insertCoffees(coffees)
      _ <- putStrLn(show"Inserted $ns suppliers and $nc coffees.")

      // Select and stream the coffees to stdout
      _ <- DAO.allCoffees.evalMap(c => putStrLn(show"$c")).compile.drain

      // Get the names and supplier names for all coffees costing less than $9.00,
      // again streamed directly to stdout
      _ <- DAO.coffeesLessThan(9.0).evalMap(p => putStrLn(show"$p")).compile.drain

      // Same thing, but read into a list this time
      l <- DAO.coffeesLessThan(9.0).compile.toList
      _ <- putStrLn(l.toString)

      // Read into a vector this time, with some stream processing
      v <- DAO.coffeesLessThan(9.0).take(2).map(p => p._1 + "*" + p._2).compile.toVector
      _ <- putStrLn(v.toString)

    } yield "All done!"

  // Entry point.
  def run: IO[Unit] = {
    val db = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      // LOAD THESE VALUES INTO THE CONFIG
      // "jdbc:postgresql://0.0.0.0:5432/DB_NAME?sslmode=disable,
      // When running in docker need to call out the docker container name...
      "jdbc:postgresql://scala-module-base-postgres-1:5432/kv-store?sslmode=disable",
      "postgres-user",
      "postgres-pass"
    )
    for {
      a <- examples.transact(db).attempt
      _ <- IO(println(a))
    } yield ()
  }

}
