# Sley – Repository Instructions

## Technology
- Language: Scala 3
- Build system: sbt
- Testing: MUnit
- Initial transport: JVM blocking sockets

## Java Platform

- Development JDK: Java 25 LTS
- Target minimum runtime: Java 21 LTS
- Scala: Scala 3, using a JDK 25-compatible release
- Build tool: sbt

Use Java 25 for local development and CI builds.

Maintain Java 21 runtime compatibility unless an
explicit architecture decision changes this requirement.

Do not introduce APIs or bytecode requirements that
force Java 25 as the minimum runtime without approval.

Verify runtime compatibility through compilation
configuration and CI tests.


## Architecture
- Prefer a functional core with explicit effect boundaries.
- Separate HTTP protocol logic from network I/O.
- Model invalid states explicitly where practical.
- Avoid unnecessary abstractions and premature modularization.
- Prefer immutable domain models.
- Allow local mutation when justified by performance or clarity.

## Development
- Keep changes focused and incremental.
- Add tests for new behavior and regressions.
- Explain significant architectural trade-offs.
- Record important decisions in docs/adr/.
- Do not introduce major dependencies without justification.
- Do not change public APIs casually.

## HTTP Correctness
- Follow applicable HTTP RFCs.
- Treat untrusted input defensively.
- Enforce reasonable resource limits.
- Never claim production readiness without evidence.


## Project Context

Before making significant architectural or implementation
decisions, consult the relevant project documentation:

- docs/vision.md: Project goals and non-goals
- docs/architecture.md: Architectural direction and boundaries
- docs/roadmap.md: Planned milestones and current progress
- docs/adr/: Accepted architecture decisions

Keep documentation consistent with the implementation.

Do not treat planned features as already implemented.

## Collaboration

Sley is both an engineering project and a learning project.

For design discussions and learning exercises:
- Encourage the developer to reason independently.
- Explain important technical trade-offs.
- Prefer incremental design over premature abstraction.

For explicitly requested implementation work:
- Complete the requested scope.
- Write or update relevant tests.
- Report important decisions and limitations.
- Avoid unrelated changes.

Use relevant skills for specialized workflows, including
Scala mentoring, functional modeling, and HTTP reviews.

## Communication

Respond in German unless the user requests otherwise.

Write code, code identifiers, and public project
documentation in English.

## Scala 3 Code Style

Follow the balanced Scala 3 style proposed by
Martin Odersky, Björn Regnell, and Rex Kerr:

https://contributors.scala-lang.org/t/towards-a-common-scala-style-recommendation/7383

Conventions:
- Prefer braces over `end` markers.
- Use braces for long scopes containing structural blank lines.
- Use significant indentation for short, clear expressions.
- Prefer modern Scala 3 control syntax.
- Use braces where they improve readability.
- Prefer braces for top-level classes, objects, traits, and enums.
- Preserve meaningful whitespace and consistent formatting.
- Avoid unrelated formatting changes.

The global `scala-code-style` skill provides additional
guidance when available, but these rules apply independently
of the skill.

