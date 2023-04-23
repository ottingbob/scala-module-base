package catsExample.hello

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- IO.println("Running CATS Example")
  } yield ExitCode(0)
}
