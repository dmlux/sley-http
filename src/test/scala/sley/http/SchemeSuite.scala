package sley.http

import sley.http.SchemeError.{Empty, InvalidCharacters}

class SchemeSuite extends munit.FunSuite {

  test("Parse \"http\"") {
    Scheme.fromString("http") match {
      case Right(scheme) =>
        assertEquals(scheme.raw, "http")

      case Left(error) =>
        fail(s"Expected Scheme got, $error")
    }
  }

  test("Parse \"HTTP\"") {
    Scheme.fromString("HTTP") match {
      case Right(scheme) =>
        assertEquals(scheme.raw, "HTTP")

      case Left(error) =>
        fail(s"Expected Scheme got, $error")
    }
  }

  test("Parse \"git+ssh\"") {
    Scheme.fromString("git+ssh") match {
      case Right(scheme) =>
        assertEquals(scheme.raw, "git+ssh")

      case Left(error) =>
        fail(s"Expected Scheme got, $error")
    }
  }

  test("Parse \"a\"") {
    Scheme.fromString("a") match {
      case Right(scheme) =>
        assertEquals(scheme.raw, "a")

      case Left(error) =>
        fail(s"Expected Scheme got, $error")
    }
  }

  test("Parse \"a1+.-\"") {
    Scheme.fromString("a1+.-") match {
      case Right(scheme) =>
        assertEquals(scheme.raw, "a1+.-")

      case Left(error) =>
        fail(s"Expected Scheme got, $error")
    }
  }

  test("Parse \"1http\"") {
    assertEquals(
      Scheme.fromString("1http"),
      Left(InvalidCharacters)
    )
  }

  test("Parse \"http_foo\"") {
    assertEquals(
      Scheme.fromString("http_foo"),
      Left(InvalidCharacters)
    )
  }

  test("Parse \"\"") {
    assertEquals(
      Scheme.fromString(""),
      Left(Empty)
    )
  }
}
