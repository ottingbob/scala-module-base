## Example Scala Module

Using this as a setup for scala modules & scala patterns

This uses [mill](https://com-lihaoyi.github.io/mill/mill/Intro_to_Mill.html) to build the Scala code.
It works with the [`build.sc`](/build.sc) file to be able to run tasks / targets like you would using `mvn`, `gradle` etc.

The current [`3.2.x`](https://www.scala-lang.org/download/3.2.2.html) version that is being used is `3.2.2`.

I use [`make`](/Makefile) to help me run some of the mill / docker related commands for testing

### Commands

Here are some commands I setup through `make`
```bash
# Build the cats.api project into a docker container and run the local containerized setup
# for the API and related components through docker-compose
$ make local-api

# Test out curl commands against the `local-api` setup
$ make test-local-api
```

Here are some helpful tasks to use through `mill`
```bash
# =====================================
# Check out downloaded deps:
$ ./mill cats.api.ivyDepsTree

# To download deps:
$ ./mill cats.api.resolvedIvyDeps

# To compile:
$ ./mill cats.hello.compile

# To run:
$ ./mill cats.hello.run

# TODO: Add in a test stage...
# To test:

# To package:
$ ./mill bar.assembly
$ ./out/bar/assmbly.dest/out.jar

# To Docker build:
$ ./mill -i cats.api.docker.build

# To prune old docker images:
$ ./mill cats.api.docker.pruneImages
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
>
> [Get metals](https://github.com/scalameta/nvim-metals) this provides LSP support / editor setup
> This is an [example configuration](https://github.com/scalameta/nvim-metals/discussions/39) for nvim LSP & metals integration
```lua
use {
	'scalameta/nvim-metals',
	requires = { "nvim-lua/plenary.nvim" }
}
```

### References

Uses the [Docker Plugin](https://com-lihaoyi.github.io/mill/mill/Plugin_Docker.html) in order to add `mill` tasks related to docker actions
