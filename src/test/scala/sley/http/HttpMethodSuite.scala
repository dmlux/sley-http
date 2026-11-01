package sley.http

class HttpMethodSuite extends munit.FunSuite {

  test("HTTP method for \"GET\"") {
    assertEquals(
      HttpMethod.fromString("GET").map(_.value),
      Right("GET")
    )
  }

  test("HTTP method for \"CUSTOM\"") {
    assertEquals(
      HttpMethod.fromString("CUSTOM").map(_.value),
      Right("CUSTOM")
    )
  }

  test("HTTP method for \"get\"") {
    assertEquals(
      HttpMethod.fromString("get").map(_.value),
      Right("get")
    )
  }

  test("HTTP method for \"\"") {
    assertEquals(
      HttpMethod.fromString(""),
      Left(HttpMethodError.InvalidMethodSyntax)
    )
  }

  test("HTTP method for \"GE T\"") {
    assertEquals(
      HttpMethod.fromString("GE T"),
      Left(HttpMethodError.InvalidMethodSyntax)
    )
  }

  test("HTTP method for \"GÄT\"") {
    assertEquals(
      HttpMethod.fromString("GÄT"),
      Left(HttpMethodError.InvalidMethodSyntax)
    )
  }
}
