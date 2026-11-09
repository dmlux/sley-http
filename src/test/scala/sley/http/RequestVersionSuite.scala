package sley.http

import sley.http.RequestVersionError.{Empty, InvalidFormat}

class RequestVersionSuite extends munit.FunSuite {

  test("Parse \"HTTP/1.1\"") {
    RequestVersion.fromString("HTTP/1.1") match {
      case Right(version) =>
        assertEquals(version.rawName, "HTTP")
        assertEquals(version.rawVersion, "1.1")

      case Left(error) => fail(s"Expected RequestVersion, got $error")
    }
  }

  test("Parse \"HTTP/1.0\"") {
    RequestVersion.fromString("HTTP/1.0") match {
      case Right(version) =>
        assertEquals(version.rawName, "HTTP")
        assertEquals(version.rawVersion, "1.0")

      case Left(error) => fail(s"Expected RequestVersion, got $error")
    }
  }

  test("Parse \"HTTP/1.9\"") {
    RequestVersion.fromString("HTTP/1.9") match {
      case Right(version) =>
        assertEquals(version.rawName, "HTTP")
        assertEquals(version.rawVersion, "1.9")

      case Left(error) => fail(s"Expected RequestVersion, got $error")
    }
  }

  test("Parse \"HTTP/2.0\"") {
    RequestVersion.fromString("HTTP/2.0") match {
      case Right(version) =>
        assertEquals(version.rawName, "HTTP")
        assertEquals(version.rawVersion, "2.0")

      case Left(error) => fail(s"Expected RequestVersion, got $error")
    }
  }

  test("Parse \"\"") {
    assertEquals(
      RequestVersion.fromString(""),
      Left(Empty)
    )
  }

  test("Parse \"http/1.1\"") {
    assertEquals(
      RequestVersion.fromString("http/1.1"),
      Left(InvalidFormat)
    )
  }

  test("Parse \"HTTP/01.1\"") {
    assertEquals(
      RequestVersion.fromString("HTTP/01.1"),
      Left(InvalidFormat)
    )
  }

  test("Parse \"HTTP/1.10\"") {
    assertEquals(
      RequestVersion.fromString("HTTP/1.10"),
      Left(InvalidFormat)
    )
  }

  test("Parse \"HTTP/1\"") {
    assertEquals(
      RequestVersion.fromString("HTTP/1"),
      Left(InvalidFormat)
    )
  }

  test("Parse \"HTTP/１.１\"") {
    assertEquals(
      RequestVersion.fromString("HTTP/１.１"),
      Left(InvalidFormat)
    )
  }

  test("Parse \"HTTP1.1\"") {
    assertEquals(
      RequestVersion.fromString("HTTP1.1"),
      Left(InvalidFormat)
    )
  }

  test("Parse \"HTT/1.1\"") {
    assertEquals(
      RequestVersion.fromString("HTT/1.1"),
      Left(InvalidFormat)
    )
  }
}
