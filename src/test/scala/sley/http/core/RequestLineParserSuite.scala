package sley.http.core

import sley.http.{HttpMethodError, OriginForm, RequestLineError}

class RequestLineParserSuite extends munit.FunSuite {
  test("parse(\"GET /hello HTTP/1.1\")") {
    RequestLineParser.parse("GET /hello HTTP/1.1") match {
      case Right(line) =>
        assertEquals(line.method.value, "GET")
        assertEquals(line.version, "HTTP/1.1")

        line.requestTarget match
          case origin: OriginForm =>
            assertEquals(origin.path, "/hello")
            assertEquals(origin.query, None)

      case Left(error) =>
        fail(s"Expected a valid request line, got $error")
    }
  }

  test("parse(\"CUSTOM /hello HTTP/1.1\")") {
    RequestLineParser.parse("CUSTOM /hello HTTP/1.1") match {
      case Right(line) =>
        assertEquals(line.method.value, "CUSTOM")
        assertEquals(line.version, "HTTP/1.1")

        line.requestTarget match
          case origin: OriginForm =>
            assertEquals(origin.path, "/hello")
            assertEquals(origin.query, None)

      case Left(error) =>
        fail(s"Expected a valid request line, got $error")
    }
  }

  test("parse(\"GET /hello\")") {
    assertEquals(
      RequestLineParser.parse("GET /hello"),
      Left(
        RequestLineError.MalformedRequestLine(
          "Expected three non-empty components separated by single spaces."
        )
      )
    )
  }

  test("parse(\"GET /hello HTTP/1.1 extra\")") {
    assertEquals(
      RequestLineParser.parse("GET /hello HTTP/1.1 extra"),
      Left(
        RequestLineError.MalformedRequestLine(
          "Expected three non-empty components separated by single spaces."
        )
      )
    )
  }

  test("parse(\"GET /hello HTTP/1.1 \")") {
    assertEquals(
      RequestLineParser.parse("GET /hello HTTP/1.1 "),
      Left(
        RequestLineError.MalformedRequestLine(
          "Expected three non-empty components separated by single spaces."
        )
      )
    )
  }

  test("parse(\"GET  HTTP/1.1\")") {
    assertEquals(
      RequestLineParser.parse("GET  HTTP/1.1"),
      Left(
        RequestLineError.MalformedRequestLine(
          "Expected three non-empty components separated by single spaces."
        )
      )
    )
  }

  test("parse(\"G@T /hello HTTP/1.1\")") {
    val result = RequestLineParser.parse("G@T /hello HTTP/1.1")
    assertEquals(
      result,
      Left(RequestLineError.InvalidMethod(HttpMethodError.InvalidMethodSyntax))
    )
  }

  test("rejects a tab inside the method") {
    val result = RequestLineParser.parse("GE\tT /hello HTTP/1.1")
    assert(result.isLeft)
  }

  test("rejects a tab inside the request target") {
    val result = RequestLineParser.parse("GET /hel\tlo HTTP/1.1")
    assert(result.isLeft)
  }

  test("rejects a trailing CR") {
    val result = RequestLineParser.parse("GET /hello HTTP/1.1\r")
    assert(result.isLeft)
  }

  test("rejects a trailing LF") {
    val result = RequestLineParser.parse("GET /hello HTTP/1.1\n")
    assert(result.isLeft)
  }

  test("rejects NUL inside the method") {
    val result = RequestLineParser.parse("GE\u0000T /hello HTTP/1.1")
    assert(result.isLeft)
  }

  test("rejects ASCII 0x1F inside the method") {
    val result = RequestLineParser.parse("GE\u001fT /hello HTTP/1.1")
    assert(result.isLeft)
  }

  test("rejects DEL inside the request target") {
    val result = RequestLineParser.parse("GET /h\u007fello HTTP/1.1")
    assert(result.isLeft)
  }
}
