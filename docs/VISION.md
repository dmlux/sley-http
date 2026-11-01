# Sley — Project Vision

## Overview

Sley is an experimental, lightweight HTTP server
implemented in Scala 3.

The project explores how HTTP server infrastructure can
be designed around functional programming, explicit
state transitions, modular architecture, and strong
protocol correctness.

Sley is developed as an educational engineering project
with the long-term possibility of becoming an open-source
library.

## Mission

Build a small, understandable, and extensible HTTP server
that demonstrates idiomatic Scala 3 and functional design
without hiding fundamental networking concepts behind
large frameworks.

## Primary Goals

### Correctness

Protocol correctness takes priority over premature
performance optimization.

HTTP behavior should follow applicable specifications,
and invalid or ambiguous inputs must be handled
explicitly.

### Functional Design

Pure transformations and explicit state modeling should
form the conceptual core of the system.

Network operations, concurrency, and resource management
belong at clearly defined effect boundaries.

### Simplicity

Prefer minimal abstractions, readable APIs, and clear
responsibilities.

Complexity must be justified by actual requirements.

### Modularity

Separate HTTP protocol handling from transport,
routing, and application-level functionality where
doing so provides clear benefits.

### Extensibility

The architecture should support future capabilities
without requiring unnecessary infrastructure upfront.

### Educational Value

The development process should encourage deliberate
architectural reasoning and an understanding of Scala 3,
functional programming, networking, and concurrency.

## Initial Scope

The first major milestone is a minimal HTTP/1.1 server
capable of:

- Accepting TCP connections
- Parsing a small, explicitly supported subset of requests
- Generating valid HTTP responses
- Serving a basic route
- Closing connections cleanly
- Providing automated tests

Capabilities outside this subset must not be silently
treated as supported.

## Long-Term Possibilities

Potential future capabilities include:

- Advanced routing and middleware
- Incremental HTTP parsing
- Persistent connections
- Request and response streaming
- Non-blocking networking
- Structured concurrency
- Functional effect integration
- TLS
- HTTP/2
- WebSockets

These are possibilities, not current commitments.

## Non-Goals

Sley does not initially aim to:

- Compete with mature HTTP servers on performance
- Replace http4s, ZIO HTTP, or Netty
- Provide a complete web application framework
- Support every HTTP version
- Guarantee production readiness
- Introduce abstractions for hypothetical requirements

## Design Philosophy

Sley favors explicit behavior, transparent architecture,
and incremental evolution.

The implementation should be simple enough to understand
while remaining rigorous enough to reveal real engineering
trade-offs.

## Open-Source Direction

Sley may eventually become a publicly consumable library.

This requires additional work in:

- Protocol compliance
- Security
- Documentation
- Public API stability
- Integration testing
- Performance evaluation
- Release engineering

Experimental versions must be clearly labeled as such.

## Project Status

Experimental — early development.

Architecture and implementation decisions are expected
to evolve as the project progresses.
