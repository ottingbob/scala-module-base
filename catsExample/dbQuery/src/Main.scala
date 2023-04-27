import cats.Show
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all._
import doobie._
import doobie.implicits._
import fs2.Stream

object Models {

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
}

/* DAO module provides ConnectionIO constructors for end users. */
trait DAO {
  import DAO._

  def coffeesLessThan(price: Double): Stream[ConnectionIO, (String, String)]

  def insertSuppliers(ss: List[Models.Supplier]): ConnectionIO[Int]

  def insertCoffees(cs: List[Models.Coffee]): ConnectionIO[Int]

  def allCoffees: Stream[ConnectionIO, Models.Coffee]

  def create: ConnectionIO[Unit]

  def drop: ConnectionIO[Unit]
}

object DAO {

  def coffeesLessThan(price: Double): Stream[ConnectionIO, (String, String)] =
    Queries.coffeesLessThan(price).stream

  def insertSuppliers(ss: List[Models.Supplier]): ConnectionIO[Int] =
    Queries.insertSupplier.updateMany(ss) // bulk insert (!)

  def insertCoffees(cs: List[Models.Coffee]): ConnectionIO[Int] =
    Queries.insertCoffee.updateMany(cs)

  def allCoffees: Stream[ConnectionIO, Models.Coffee] =
    Queries.allCoffees.stream

  def create: ConnectionIO[Unit] =
    Queries.create.run.void

  def drop: ConnectionIO[Unit] =
    Queries.drop.run.void
}

/** Queries module contains "raw" Query/Update values. */
object Queries {

  def coffeesLessThan(price: Double): Query0[(String, String)] =
    sql"""
        SELECT cof_name, sup_name
        FROM coffees JOIN suppliers ON coffees.sup_id = suppliers.sup_id
        WHERE price < $price
      """.query[(String, String)]

  val insertSupplier: Update[Models.Supplier] =
    Update[Models.Supplier]("INSERT INTO suppliers VALUES (?, ?, ?, ?, ?, ?)", None)

  val insertCoffee: Update[Models.Coffee] =
    Update[Models.Coffee]("INSERT INTO coffees VALUES (?, ?, ?, ?, ?)", None)

  def allCoffees[A]: Query0[Models.Coffee] =
    sql"SELECT cof_name, sup_id, price, sales, total FROM coffees".query[Models.Coffee]

  def create: Update0 =
    sql"""
        CREATE TABLE IF NOT EXISTS suppliers (
          sup_id   INT     NOT NULL PRIMARY KEY,
          sup_name VARCHAR NOT NULL,
          street   VARCHAR NOT NULL,
          city     VARCHAR NOT NULL,
          state    VARCHAR NOT NULL,
          zip      VARCHAR NOT NULL
        );

        CREATE TABLE IF NOT EXISTS coffees (
          cof_name VARCHAR NOT NULL,
          sup_id   INT     NOT NULL,
          price    DOUBLE PRECISION NOT NULL,
          sales    INT     NOT NULL,
          total    INT     NOT NULL
        );

        ALTER TABLE coffees
        ADD CONSTRAINT coffees_suppliers_fk FOREIGN KEY (sup_id) REFERENCES suppliers(sup_id);
      """.update

  def drop: Update0 =
    sql"""
        ALTER TABLE IF EXISTS coffees
          DROP CONSTRAINT IF EXISTS coffees_suppliers_fk;

        DROP TABLE IF EXISTS suppliers;
        DROP TABLE IF EXISTS coffees;
    """.update
}

// Adapted from this example:
// https://github.com/tpolecat/doobie/blob/main/modules/example/src/main/scala/example/FirstExample.scala
object Main extends IOApp.Simple {

  // Some suppliers
  val suppliers = List(
    Models.Supplier(101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"),
    Models.Supplier(49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460"),
    Models.Supplier(150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966")
  )

  // Some coffees
  val coffees = List(
    Models.Coffee("Colombian", 101, 7.99, 0, 0),
    Models.Coffee("French_Roast", 49, 8.99, 0, 0),
    Models.Coffee("Espresso", 150, 9.99, 0, 0),
    Models.Coffee("Colombian_Decaf", 101, 8.99, 0, 0),
    Models.Coffee("French_Roast_Decaf", 49, 9.99, 0, 0)
  )

  // Our example database action
  def examples: ConnectionIO[String] =
    for {
      // Drop if tables / records exists to start new
      _ <- DAO.drop

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
      "jdbc:postgresql://localhost:5432/kv-store?sslmode=disable",
      "postgres-user",
      "postgres-pass"
    )
    for {
      a <- examples.transact(db).attempt
      _ <- IO(println(a))
    } yield ()
  }

  // Lifted println
  def putStrLn(s: => String): ConnectionIO[Unit] =
    FC.delay(println(s))
}
