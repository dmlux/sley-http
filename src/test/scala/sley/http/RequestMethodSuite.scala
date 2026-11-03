package sley.http

class RequestMethodSuite extends munit.FunSuite {

  test("HTTP method for \"GET\"") {
    assertEquals(
      RequestMethod.fromString("GET").map(_.value),
      Right("GET")
    )
  }

  test("HTTP method for \"CUSTOM\"") {
    assertEquals(
      RequestMethod.fromString("CUSTOM").map(_.value),
      Right("CUSTOM")
    )
  }

  test("HTTP method for \"get\"") {
    assertEquals(
      RequestMethod.fromString("get").map(_.value),
      Right("get")
    )
  }

  test("HTTP method for \"\"") {
    assertEquals(
      RequestMethod.fromString(""),
      Left(RequestMethodError.Empty)
    )
  }

  test("HTTP method for \"GE T\"") {
    assertEquals(
      RequestMethod.fromString("GE T"),
      Left(RequestMethodError.InvalidMethodCharacters)
    )
  }

  test("HTTP method for \"GÄT\"") {
    assertEquals(
      RequestMethod.fromString("GÄT"),
      Left(RequestMethodError.InvalidMethodCharacters)
    )
  }
}
