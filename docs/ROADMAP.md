# Sley — Development Roadmap

## Status

Early development.

This roadmap is a planning document, not a commitment
to specific dates or release versions.

Only mark milestones complete after verification.

## Milestone 0 — Project Foundation

### Deliverables
- [ ] Scala 3 project initialized
- [ ] sbt build configured
- [ ] MUnit configured
- [ ] Repository documentation established
- [ ] Basic CI pipeline
- [ ] Initial architectural decisions reviewed

### Exit Criteria

The project builds and runs its test suite reliably.

## Milestone 1 — Minimal HTTP/1.1 Server

### Deliverables
- [ ] TCP listener
- [ ] Basic request-line parsing
- [ ] Initial header parsing
- [ ] Minimal request and response models
- [ ] HTTP response serialization
- [ ] Static GET endpoint
- [ ] Connection closure
- [ ] Unit and integration tests

### Exit Criteria

A supported HTTP/1.1 request can be sent over TCP
and receives a valid response.

Unsupported or malformed input is handled according
to explicitly documented initial limitations.

## Milestone 2 — Routing and Middleware

### Deliverables
- [ ] Method-based routing
- [ ] Path matching
- [ ] Path parameters
- [ ] Query parameter handling
- [ ] Handler abstraction
- [ ] Middleware composition
- [ ] Routing tests

### Exit Criteria

Multiple handlers can be composed and selected
predictably using the public routing API.

## Milestone 3 — HTTP/1.1 Robustness

### Deliverables
- [ ] Incremental request parsing
- [ ] Content-Length handling
- [ ] Chunked transfer coding
- [ ] Persistent connections
- [ ] Multiple requests per connection
- [ ] Message and header limits
- [ ] Timeouts
- [ ] Malformed request handling
- [ ] Protocol regression tests
- [ ] Property-based parser tests

### Exit Criteria

The supported HTTP/1.1 behavior is documented,
tested, and reviewed against applicable RFCs.

## Milestone 4 — Concurrency and Streaming

### Deliverables
- [ ] Concurrent connections
- [ ] Resource-safe connection handling
- [ ] Request body streaming
- [ ] Response body streaming
- [ ] Backpressure strategy
- [ ] Cancellation semantics
- [ ] Graceful shutdown

### Exit Criteria

The server processes concurrent connections
reliably under defined resource constraints.

## Milestone 5 — Performance and Extensibility

### Deliverables
- [ ] Reproducible benchmarks
- [ ] Profiling infrastructure
- [ ] Buffer management improvements
- [ ] Evaluation of NIO
- [ ] Transport abstraction review
- [ ] TLS integration using established libraries

### Exit Criteria

Performance and resource behavior are measured,
documented, and regression-tested where practical.

## Milestone 6 — Open-Source Readiness

### Deliverables
- [ ] Public API review
- [ ] User documentation
- [ ] Usage examples
- [ ] Contribution guidelines
- [ ] License verification
- [ ] Security testing
- [ ] Release automation
- [ ] Artifact publishing

### Exit Criteria

The project has a documented support scope,
reproducible builds, and a controlled release process.

Production suitability requires a separate assessment.

## Future Exploration

Potential topics:

- HTTP/2
- HTTP/3
- WebSockets
- Additional transport implementations
- Cats Effect integration
- ZIO integration

These are not committed roadmap milestones.

## Development Principles

- Complete small vertical slices.
- Keep each milestone independently testable.
- Prefer correctness over premature optimization.
- Avoid implementing future milestones speculatively.
- Update this document when priorities change.
- Record significant architectural decisions separately.
