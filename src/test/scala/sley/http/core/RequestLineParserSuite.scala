package sley.http.core

import sley.http.{RequestLine, RequestLineError}

class RequestLineParserSuite extends munit.FunSuite {
  test("parse(\"GET /hello HTTP/1.1\")") {
    assertEquals(
      RequestLineParser.parse("GET /hello HTTP/1.1"),
      Right(
        RequestLine("GET", "/hello", "HTTP/1.1")
      )
    )
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
}
