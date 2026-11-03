# Sley — Architecture

## Status

Initial architectural direction.

This document describes intended boundaries and design
principles. It is not a claim that these components
already exist.

Update this document as implementation decisions are made.

## Architectural Approach

Sley initially follows the functional core /
imperative shell pattern.

The goal is to separate deterministic HTTP protocol
processing from effectful network operations.

## Conceptual Layers

### Application Layer

Potential responsibilities:

- Route definitions
- Request handlers
- Middleware
- Application configuration

This layer is introduced incrementally.

### HTTP Core

Potential responsibilities:

- Request and response representations
- HTTP parsing
- Message validation
- Response encoding
- Protocol errors
- Connection-relevant protocol state

Prefer pure functions and immutable data structures.

Parsing may use explicit state machines to handle
incremental network input.

### Server Runtime

Potential responsibilities:

- Accepting connections
- Reading and writing bytes
- Managing connection state
- Resource ownership and cleanup
- Concurrency
- Timeouts
- Shutdown

Initially, the runtime may use blocking JVM sockets.

The runtime should not own HTTP semantics unnecessarily.

### Transport

The initial transport is TCP through JVM networking APIs.

Alternative transports or execution strategies may be
introduced when justified.

## Dependency Direction

Conceptually:

    Application
         |
         v
      HTTP Core
         ^
         |
    Server Runtime
         |
         v
     JVM Networking

The HTTP Core should not depend on the Server Runtime
or concrete socket implementations.

Actual module boundaries may differ from these
conceptual boundaries.

## Parsing Model

HTTP messages arrive as sequences of bytes.

TCP read boundaries do not correspond to HTTP message
boundaries.

The parser must eventually support:

- Incomplete input
- Complete messages
- Invalid input
- Unconsumed bytes
- Explicit framing and limits

Incremental parsing may require a model resembling:

    parse(state, bytes) -> result

Possible results include:

- More input required
- Message completed
- Protocol failure

The specific types and algorithms are not yet decided.

## Syntactic Validation

The HTTP Core currently enforces HTTP message syntax
only.

Parsers report which syntactic rule an input violates.
They do not choose HTTP status codes or build responses.
Mapping protocol errors to responses belongs to a later
layer and is not yet designed.

Syntactic validity does not imply server support.
For example, the parser accepts an unknown method name
if it is a valid HTTP token.

## Request-Line Model

The request line is modeled with small validated
domain types:

- `RequestMethod`: a value object for a case-sensitive
  HTTP token.
- `RequestTarget`: a sealed hierarchy of request-target
  forms. Only `OriginForm` is implemented. Other forms
  are rejected as malformed.
- `RequestLine`: the method, the request target, and the
  protocol version. The version is still a raw string
  and is not validated yet.

Validated types are created through smart constructors
that return `Either`. Each component has its own error
enum, so callers can tell which part of the request
line is invalid.

## State Management

Distinguish:

- HTTP message state
- Parser state
- Connection lifecycle state
- Application state

Do not combine these responsibilities into a single
mutable object without a clear reason.

Use immutable state transitions when appropriate.

Controlled local mutation remains acceptable when it
improves efficiency and preserves encapsulation.

## Error Handling

Expected protocol failures should be represented
explicitly where practical.

Distinguish:

- Invalid client input
- Unsupported protocol features
- Internal server failures
- Transport failures
- Cancellation and shutdown

Not all failures should produce an HTTP response.
Some conditions may require connection termination.

## Concurrency

The initial server may process connections using a
simple blocking implementation.

Concurrency strategy is an open design decision.

Possible future approaches:

- JVM threads
- Virtual threads
- NIO event loops
- Functional effect runtimes

Do not introduce a particular concurrency abstraction
before evaluating its requirements.

## Performance

Correctness and understandable design come first.

Potential optimizations include:

- Efficient byte buffers
- Reduced allocations
- Incremental parsing
- Streaming
- Controlled resource utilization

Benchmark before optimizing.

## Testing Strategy

### Unit Tests
Test pure protocol transformations and state transitions.

### Integration Tests
Run the server and exercise it over real TCP connections.

### Property-Based Tests
Verify invariants and parser behavior across generated inputs.

### Adversarial Tests
Exercise malformed messages, ambiguous framing,
partial input, and resource exhaustion scenarios.

## Architecture Decisions

Record significant design choices in docs/adr/.

Each ADR should include:

- Context
- Decision
- Alternatives
- Consequences
- Status

Avoid creating ADRs for trivial implementation details.

## Open Questions

- What should the public request model look like?
- How should incremental parsing be represented?
- Who owns connection-level protocol state?
- When should streaming be introduced?
- Should the runtime use an effect system?
- What is the correct abstraction boundary for handlers?
- When should the project become multi-module?

Resolve these questions through focused design work,
not speculative abstraction.

## JVM Compatibility

Sley uses Java 25 LTS as its development JDK.

The initial compatibility target is Java 21 LTS.

This allows development on a modern JVM while retaining
compatibility with existing JVM deployments.

The minimum Java runtime is an architectural constraint,
not merely a local development preference.

Any decision to raise the minimum required runtime
must be evaluated for its impact on library consumers
and documented in an ADR.
