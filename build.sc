import mill._
import mill.scalalib._

val ScalaVersion = "3.2.2"

trait BaseScalaModule extends ScalaModule {
  def scalaVersion = ScalaVersion

  def organization = "robs.dev"
  def organizationName = "Robs dev"

  override def scalacOptions = Seq(
    "-deprecation",     // Emit warning and location for usages of deprecated APIs.
    // In Scala 3 the compiler option `warn-unused` is not available yet...
    // Ref: https://scalacenter.github.io/scalafix/docs/rules/RemoveUnused.html
    "-Wunused:all",     // Warn on all unused dependencies.
    "-Xfatal-warnings", // Fail compilation if there are any warnings
  )
}

object `bar` extends ScalaModule {
  def scalaVersion = ScalaVersion

  def ivyDeps = Agg(
    // Scala deps use `::` to separate the first 2 fields
    ivy"com.lihaoyi::mainargs:0.4.0",
  )
}

object `foo` extends BaseScalaModule {

  def version = "0.0.1-SNAPSHOT"

  def lineCount: T[Int] = T {
    sources()
      .flatMap(pathRef => os.walk(pathRef.path))
      .filter(_.ext == "scala")
      .map(os.read.lines(_))
      .map(_.size)
      .sum
  }
}

object `hello-world` extends BaseScalaModule {

  def millSourcePath = millOuterCtx.millSourcePath / "helloWorld"

  /* 
   * Unsure what this specifically changes with setting source path directly
   * VS setting the sources. IE what else is effected by changing the `millSourcePath`.
   *
   * This setting is equivalent to:
    def sources = T.sources{
      super.sources() ++ Seq(PathRef(millOuterCtx.millSourcePath / "helloWorld"))
    }
  */

  def version = "0.0.2-SNAPSHOT"
  def name = "hello-world"

  def mainClass: T[Option[String]] = Some("helloWorld.HelloWorld")
}

object `cats` extends Module {

  def millSourcePath = millOuterCtx.millSourcePath / "catsExample"

  def catsEffect = ivy"org.typelevel::cats-effect::3.4.8"
  def catsLogger = ivy"org.typelevel::log4cats-slf4j::2.2.0"
  def catsLoggerCore = ivy"org.typelevel::log4cats-core::2.2.0"

  // Important Note:
  // ---------------
  // logback classic < 1.3 includes the `org.slf4j.impl.StaticLoggerBinder` which outputs
  // the logging to the terminal. Otherwise the NOOP logger is used
  def logback = ivy"ch.qos.logback:logback-classic:1.2.3"

  // These were other attempts at getting logging working:
  // def slf4j = "org.slf4j:slf4j-api:2.0.7"
  // def slf4j = "org.slf4j:slf4j-log4j12:2.0.7"
  // def config = ivy"com.typesafe:config:1.4.0"
  // def scalaLogging = ivy"com.typesafe.scala-logging::scala-logging:3.1.0"

  def ivyDeps = Agg(
    logback,
    catsEffect,
    catsLogger,
    catsLoggerCore,
  )

  object hello extends BaseScalaModule {

    def ivyDeps = Agg(
      catsEffect,
    )

    def version = "0.0.2-SNAPSHOT"
    def name = "cats-hello"
  }

  object api extends BaseScalaModule {

    /*
      def resources = T.sources(
        millSourcePath / "resources"
      )
    */

    def ivyDeps = super.ivyDeps() ++ Agg(
      // ORDER IS IMPORTANT -- logback needs to be first...
      logback,
      ivy"org.http4s::http4s-ember-server:0.23.12",
      ivy"org.http4s::http4s-server:0.23.12",
      ivy"org.http4s::http4s-dsl:0.23.12",
      ivy"org.http4s::http4s-circe:0.23.12",
      ivy"io.circe::circe-core:0.14.3",
      ivy"io.circe::circe-generic:0.14.3",
      ivy"io.circe::circe-literal:0.14.3",
    )

    def version = "0.1.1-SNAPSHOT"
    def name = "cats-api"
  }
}
