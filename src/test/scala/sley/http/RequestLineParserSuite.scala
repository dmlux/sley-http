package sley.http

import sley.http.{RequestMethodError, OriginForm, RequestLineError}

class RequestLineParserSuite extends munit.FunSuite {
  test("parse(\"GET /hello HTTP/1.1\")") {
    RequestLine.fromString("GET /hello HTTP/1.1") match {
      case Right(line) =>
        assertEquals(line.method.raw, "GET")
        assertEquals(line.version.rawName, "HTTP")
        assertEquals(line.version.rawVersion, "1.1")

        line.target match
          case origin: OriginForm =>
            assertEquals(origin.path, "/hello")
            assertEquals(origin.query, None)

      case Left(error) =>
        fail(s"Expected a valid request line, got $error")
    }
  }

  test("parse(\"CUSTOM /hello HTTP/1.1\")") {
    RequestLine.fromString("CUSTOM /hello HTTP/1.1") match {
      case Right(line) =>
        assertEquals(line.method.raw, "CUSTOM")
        assertEquals(line.version.rawName, "HTTP")
        assertEquals(line.version.rawVersion, "1.1")

        line.target match
          case origin: OriginForm =>
            assertEquals(origin.path, "/hello")
            assertEquals(origin.query, None)

      case Left(error) =>
        fail(s"Expected a valid request line, got $error")
    }
  }

  test("parse(\"GET /hello\")") {
    assertEquals(
      RequestLine.fromString("GET /hello"),
      Left(
        RequestLineError.MalformedRequestLine(
          "Expected three non-empty components separated by single spaces."
        )
      )
    )
  }

  test("parse(\"GET /hello HTTP/1.1 extra\")") {
    assertEquals(
      RequestLine.fromString("GET /hello HTTP/1.1 extra"),
      Left(
        RequestLineError.MalformedRequestLine(
          "Expected three non-empty components separated by single spaces."
        )
      )
    )
  }

  test("parse(\"GET /hello HTTP/1.1 \")") {
    assertEquals(
      RequestLine.fromString("GET /hello HTTP/1.1 "),
      Left(
        RequestLineError.MalformedRequestLine(
          "Expected three non-empty components separated by single spaces."
        )
      )
    )
  }

  test("parse(\"GET  HTTP/1.1\")") {
    assertEquals(
      RequestLine.fromString("GET  HTTP/1.1"),
      Left(
        RequestLineError.MalformedRequestLine(
          "Expected three non-empty components separated by single spaces."
        )
      )
    )
  }

  test("parse(\"G@T /hello HTTP/1.1\")") {
    val result = RequestLine.fromString("G@T /hello HTTP/1.1")
    assertEquals(
      result,
      Left(RequestLineError.InvalidRequestMethod(RequestMethodError.InvalidCharacters))
    )
  }

  test("rejects a tab inside the method") {
    val result = RequestLine.fromString("GE\tT /hello HTTP/1.1")
    assert(result.isLeft)
  }

  test("rejects a tab inside the request target") {
    val result = RequestLine.fromString("GET /hel\tlo HTTP/1.1")
    assert(result.isLeft)
  }

  test("rejects a trailing CR") {
    val result = RequestLine.fromString("GET /hello HTTP/1.1\r")
    assert(result.isLeft)
  }

  test("rejects a trailing LF") {
    val result = RequestLine.fromString("GET /hello HTTP/1.1\n")
    assert(result.isLeft)
  }

  test("rejects NUL inside the method") {
    val result = RequestLine.fromString("GE\u0000T /hello HTTP/1.1")
    assert(result.isLeft)
  }

  test("rejects ASCII 0x1F inside the method") {
    val result = RequestLine.fromString("GE\u001fT /hello HTTP/1.1")
    assert(result.isLeft)
  }

  test("rejects DEL inside the request target") {
    val result = RequestLine.fromString("GET /h\u007fello HTTP/1.1")
    assert(result.isLeft)
  }
}
