## Example Scala Module

Using this as a setup for scala modules & scala patterns

This uses [mill](https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html) to build the Scala code.
It works with the [`build.sc`](/build.sc) file to be able to run tasks / targets like you would using `mvn`, `gradle` etc.

The current [`3.2.x`](https://www.scala-lang.org/download/3.2.2.html) version that is being used is `3.2.2`.

### Commands

Here are some helpful tasks to use through mill

```bash
# =====================================
# Check out downloaded deps:
$ ./mill bar.ivyDepsTree

# To download deps:
$ ./mill bar.resolvedIvyDeps

# To compile:
$ ./mill bar.compile

# To run:
$ ./mill bar.run

# TODO: Add in a test stage...
# To test:

# To package:
$ ./mill bar.assembly
$ ./out/bar/assmbly.dest/out.jar
```

### Setup

Here are steps for my scala setup

> I am on `Ubuntu 22.04.2 LTS x86_64`
>
> [Get scala](https://www.scala-lang.org/download/) (most likely will want to move the alias from `.zprofile` into another file like `.zshrc`)
```bash
$ curl -fL https://github.com/coursier/coursier/releases/latest/download/cs-x86_64-pc-linux.gz | gzip -d > cs && chmod +x cs && ./cs setup

$ . .zprofile
```
>
> [Get mill](https://com-lihaoyi.github.io/mill/mill/Installation.html#_mills_bootstrap_script_linuxos_x_only)
```bash
$ curl -L https://github.com/com-lihaoyi/mill/releases/download/0.11.0-M7/0.11.0-M7 > mill && chmod +x mill
```

##### TODO: Add in editor setup...


