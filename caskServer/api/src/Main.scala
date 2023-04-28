package caskServer.api

import cask._
import ujson._

import java.util.UUID
import scala.collection.mutable.HashMap
import scala.io.Source

// Database Model
object Model {

  case class Movie(
      name: String,
      director: String,
      year: Int
  )
}

object MinimalDb {
  val database = HashMap.empty[UUID, Model.Movie]
}

// TODO: See what we can do with context
case class BaseRoutes() extends cask.Routes {
// case class BaseRoutes()(implicit cc: castor.Context, log: cask.Logger) extends cask.Routes {
  @cask.get("/")
  def hello() = {
    "Hello World!"
  }

  initialize()
}

case class MovieRoutes() extends cask.Routes {

  // case class Movie(checked: Boolean, text: String)
  object Movie {
    implicit def todoRW: upickle.default.ReadWriter[Model.Movie] =
      upickle.default.macroRW[Model.Movie]
  }

  // TODO: Hook this up to the minimal db
  @cask.get("/movies")
  def listMovies(request: Request) = {
    // MinimalDb.database.get(UUID.randomUUID()).toString()
    println(s"Database: {${MinimalDb.database}}")

    MinimalDb.database
      .map((key: UUID, movie: Model.Movie) => s"${key}: ${movie}")
      // TODO: Map the model movie to the JSON response defined in this class...
      .reduce((resp, moviePair) => s"$resp,\n$moviePair")
  }

  @cask.post("/movies")
  def createMovie(request: Request) =
    try {
      val movieJson = ujson
        .read(request.data)

      val movie = Model.Movie(
        name = movieJson("name").str,
        director = movieJson("director").str,
        year = movieJson("year").num.toInt
      )

      MinimalDb.database += (UUID.randomUUID() -> movie)
      Response(s"Created Movie: ${movie}", 201, Seq(), Seq())
    } catch
      case nsee: java.util.NoSuchElementException =>
        Response(s"failed with invalid movie data: $nsee", 400, Seq(), Seq())

  initialize()
}

class logger extends cask.RawDecorator {
  class LoggerError(val value: cask.router.Result.Error) extends Exception

  def wrapFunction(pctx: cask.Request, delegate: Delegate) = {
    println(s"Getting request: ${pctx}")

    delegate(Map()) match {
      case cask.router.Result.Success(t) => cask.router.Result.Success(t)
      case e: cask.router.Result.Error   => throw new LoggerError(e)
    }
  }
}

// TODO: Figure out how to get debug logging over all routes with a decorator
//
// Essentially is there a way to extend cask.MainRoutes to hook into how each of the routes
// are ran...

class getWithLogging(override val path: String, override val subpath: Boolean = false)
    extends cask.get(path, subpath) {

  override def wrapFunction(
      ctx: Request,
      delegate: Map[String, Seq[String]] => cask.router.Result[cask.model.Response.Raw]
  ): cask.router.Result[cask.model.Response.Raw] = {
    println(s"Getting request: ${ctx}")
    super.wrapFunction(ctx, delegate)
  }
}

object MinimalApp extends cask.MainRoutes {

  override val allRoutes = Seq(BaseRoutes(), MovieRoutes(), this)

  override def port: Int = 8883

  // This is equivalent to the combination of:
  // @logger
  // @cask.get("/headers")

  @getWithLogging("/headers")
  def echoHeaders(request: Request) = {
    request.headers.map((key, headerValues) => s"$key: ${headerValues.mkString}\n").mkString
  }

  @logger
  @cask.post("/reverse")
  def doThing(request: Request) = {
    request.text().reverse
  }

  initialize()
}
