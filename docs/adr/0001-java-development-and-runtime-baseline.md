# ADR-0001: Java development and runtime baseline

- Status: Accepted
- Date: 2026-11-01
- Decision makers: Sley project maintainer

## Context

Sley is an experimental Scala 3 HTTP server with the long-term goal of being
usable as a library. Requiring Java 25 at runtime would prevent applications
running on Java 21 from adopting it without upgrading their runtime.

The project prefers Java 25 LTS for development while retaining Java 21 LTS
as its minimum supported runtime. The development JDK and the library's
runtime requirements therefore need to be configured and verified separately.

## Decision drivers

- Compatibility with applications running on Java 21.
- Use of Java 25 for local development and CI builds.
- Explicit, verifiable runtime requirements.
- Manageable setup and CI maintenance costs.

## Considered options

1. Use Java 21 for development and as the minimum runtime.
2. Use Java 25 for development and as the minimum runtime.
3. Use Java 25 for development and Java 21 as the minimum runtime.

## Decision

We will use Java 25 for local development and CI builds, and retain Java 21
as the minimum runtime for Sley.

We choose this combination to allow development on the preferred newer JDK
without requiring consumers to upgrade from Java 21. We accept the additional
configuration and testing needed to maintain this separation.

The Scala compiler must support the development JDK and target the Java 21
platform. Library code must not require JDK APIs or bytecode features that
are unavailable on Java 21. Runtime dependencies must also remain compatible
with Java 21.

This decision does not enable Java 25 language features in Java 21-targeted
code. Scala language features are governed by the selected Scala compiler,
subject to the runtime compatibility requirement.

JDK patch versions may be updated without changing this decision. Raising
the minimum runtime requires a superseding ADR.

## Alternatives

### Java 21 for development and as the minimum runtime

This would simplify the setup by using the same JDK baseline for compilation
and execution. It is a viable alternative, but does not satisfy the project's
preference to develop and build on Java 25.

### Java 25 for development and as the minimum runtime

This would simplify the compatibility policy and allow library code to depend
on Java 25 APIs. It was rejected because consumers running Java 21 would need
to upgrade their runtime to use Sley.

## Consequences

### Benefits

- Developers can use Java 25 while preserving Java 21 as the runtime baseline.
- Consumers are not required to adopt the development JDK to use the library.
- Explicit compiler settings and runtime tests make the compatibility policy
  verifiable as the implementation grows.

### Costs and limitations

- Library code is restricted to the Java 21 platform despite being built on
  Java 25.
- CI must provide both JDKs and test on both runtime versions.
- Dependency upgrades must be checked for changes in minimum Java requirements.
- Passing tests only demonstrates compatibility for the behavior exercised;
  it does not establish support for every newer JDK, platform, or code path.

## Verification

The build configures the Scala compiler with `-release:21`. The local SDKMAN
configuration selects Java 25, and CI explicitly selects Java 25 for the build.

CI runs the test suites on Java 25, then runs them in a forked JVM selected
through `Test / javaHome` using Java 21. Both runs must succeed.

At acceptance, verification covers the setup test only. A local diagnostic
confirmed Java 21 in the forked test process, and the maintainer reported a
successful CI run. This is evidence for the toolchain setup, not comprehensive
library compatibility or production readiness.

As implementation proceeds, protocol and socket integration tests must run
on both runtimes. If Java sources are introduced, the Java compiler must also
be configured with `--release 21`; the current Scala setting does not configure
`javac`. Dependency runtime requirements must be reviewed when dependencies
are added or upgraded.

## Reconsideration

Revisit this decision if a required feature or dependency cannot support
Java 21, the selected toolchain no longer supports the development/runtime
combination, or consumer requirements justify a different baseline.

Any proposal to raise the minimum runtime must explain the concrete benefit,
the impact on consumers, and the migration implications in a superseding ADR.
The availability of a newer JDK alone does not change this baseline.

## References

- [Java platform requirements](../../AGENTS.md#java-platform)
- [JVM compatibility](../ARCHITECTURE.md#jvm-compatibility)
- [Build configuration](../../build.sbt)
- [Development toolchain](../../.sdkmanrc)
- [CI workflow](../../.github/workflows/ci.yml)
- [Setup test](../../src/test/scala/sley/BuildSetupSuite.scala)
