package caskServer.api

import cask._

import java.util.HashMap
import java.util.UUID

object Model {

  case class Movie(
      name: String,
      director: String,
      year: Int
  )
}

object MinimalDb {
  def database = HashMap[UUID, Model.Movie]()
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

object MinimalApp extends cask.MainRoutes {

  override val allRoutes = Seq(BaseRoutes()) ++ Seq(this)

  /*
   * TODO: Figure out how to get debug logging over all routes with a decorator
    override def main(args: Array[String]) = {
      log.toString()
    }
   */
  override def port: Int = 8883

  @cask.get("/headers")
  def echoHeaders(request: Request) = {
    request.headers.map((key, headerValues) => s"$key: ${headerValues.mkString}\n").mkString
  }

  // TODO: Hook this up to the minimal db
  @cask.get("/movies")
  def getMovies(request: Request) = {
    request.headers.map((key, headerValues) => s"$key: ${headerValues.mkString}\n").mkString
  }

  @cask.post("/reverse")
  def doThing(request: Request) = {
    request.text().reverse
  }

  initialize()
}
