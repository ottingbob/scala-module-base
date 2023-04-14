import mill._
import mill.scalalib._

val ScalaVersion = "3.2.2"

trait BaseScalaModule extends ScalaModule {
  def scalaVersion = ScalaVersion

  def organization = "robs.dev"
  def organizationName = "Robs dev"

  override def scalacOptions = Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
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

