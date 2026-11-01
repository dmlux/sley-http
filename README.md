# Sley

**A lightweight, functional HTTP server for Scala 3.**

Sley is an experimental HTTP server built from the ground up using Scala 3.

The project explores functional programming, HTTP protocol implementation, concurrent request processing, and modular server architecture.

Inspired by the *sley*, a component of a weaving loom that guides and coordinates threads, Sley aims to bring the same principles of structure and coordination to HTTP request processing.

## Goals

- Idiomatic Scala 3
- Functional core, imperative shell
- Lightweight and modular architecture
- Minimal external dependencies
- HTTP/1.1 support
- Incremental development with a focus on correctness and testability

## Status

**Early development — experimental.**

Sley is currently a learning and research project. It is not production-ready.

## Development setup

Development requires JDK 25. The library targets Java 21 or newer.

### Using SDKMAN

With SDKMAN installed, run the following commands from the repository root:

```bash
sdk env install
sdk env
```

This installs missing tools and activates the versions configured in
`.sdkmanrc`. Scala is downloaded automatically by sbt.

Alternatively, install JDK 25 and an sbt launcher using your preferred
installation method. The build selects its sbt version through
`project/build.properties`.

### Verify the setup

```
sbt --server --batch "scalafmtCheckAll; scalafmtSbtCheck; testOnly *"
```

### Format code

```
sbt --server --batch "scalafmtAll; scalafmtSbt"
```

### IntelliJ IDEA

- Install and enable the Scala plugin.
- Open `build.sbt` as a project.
- Select JDK 25 as the project SDK and the sbt JVM.
- Select Scalafmt as the Scala formatter and enable formatting on save.

### Continuous integration

GitHub Actions checks formatting and runs tests on Java 25.
It also runs the test suites in a separate Java 21 JVM.

## License

Apache License 2.0.
