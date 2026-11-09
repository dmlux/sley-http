package sley.http

import sley.http.HierPartError.InvalidCharacters

class HierPartSuite extends munit.FunSuite {

  test("Parse \"/\"") {
    HierPart.fromString("/") match {
      case Right(part: HierPart.Absolute) =>
        assertEquals(part.path, "/")

      case Right(part) => fail(s"Expected Absolute path, got $part")

      case Left(error) => fail(s"Expected Absolute path, got $error")
    }
  }

  test("Parse \"/users/42\"") {
    HierPart.fromString("/users/42") match {
      case Right(part: HierPart.Absolute) =>
        assertEquals(part.path, "/users/42")

      case Right(part) => fail(s"Expected Absolute path, got $part")

      case Left(error) => fail(s"Expected Absolute path, got $error")
    }
  }

  test("Parse \"/a//b/\"") {
    HierPart.fromString("/a//b/") match {
      case Right(part: HierPart.Absolute) =>
        assertEquals(part.path, "/a//b/")

      case Right(part) => fail(s"Expected Absolute path, got $part")

      case Left(error) => fail(s"Expected Absolute path, got $error")
    }
  }

  test("Parse \"/a%20b\"") {
    HierPart.fromString("/a%20b") match {
      case Right(part: HierPart.Absolute) =>
        assertEquals(part.path, "/a%20b")

      case Right(part) => fail(s"Expected Absolute path, got $part")

      case Left(error) => fail(s"Expected Absolute path, got $error")
    }
  }

  test("Parse \"urn:value\"") {
    HierPart.fromString("urn:value") match {
      case Right(part: HierPart.Rootless) =>
        assertEquals(part.path, "urn:value")

      case Right(part) => fail(s"Expected Rootless path, got $part")

      case Left(error) => fail(s"Expected Rootless path, got $error")
    }
  }

  test("Parse \"a/b\"") {
    HierPart.fromString("a/b") match {
      case Right(part: HierPart.Rootless) =>
        assertEquals(part.path, "a/b")

      case Right(part) => fail(s"Expected Rootless path, got $part")

      case Left(error) => fail(s"Expected Rootless path, got $error")
    }
  }

  test("Parse \"a%20b\"") {
    HierPart.fromString("a%20b") match {
      case Right(part: HierPart.Rootless) =>
        assertEquals(part.path, "a%20b")

      case Right(part) => fail(s"Expected Rootless path, got $part")

      case Left(error) => fail(s"Expected Rootless path, got $error")
    }
  }

  test("Parse \"\"") {
    assertEquals(
      HierPart.fromString(""),
      Right(HierPart.Empty)
    )
  }

  test("Parse \"//example.com\"") {
    HierPart.fromString("//example.com") match {
      case Right(HierPart.Authority(authority, path)) =>
        assertEquals(authority.userInfo, None)
        assertEquals(path, "")
        assertEquals(authority.port, None)
        authority.host match {
          case Some(RegName(raw)) => assertEquals(raw, "example.com")
          case host               => fail(s"Expected RegName host, got $host")
        }

      case result => fail(s"Expected authority, got $result")
    }
  }

  test("Parse \"//user@[::1]:8080/path\"") {
    HierPart.fromString("//user@[::1]:8080/path") match {
      case Right(HierPart.Authority(authority, path)) =>
        assertEquals(authority.userInfo, Some("user"))
        assertEquals(path, "/path")
        assertEquals(authority.port, Some("8080"))
        authority.host match {
          case Some(IPv6Address(raw)) => assertEquals(raw, "::1")
          case host                   => fail(s"Expected IPv6 host, got $host")
        }

      case result => fail(s"Expected authority, got $result")
    }
  }

  test("Parse \"///path\"") {
    HierPart.fromString("///path") match {
      case Right(HierPart.Authority(authority, path)) =>
        assertEquals(authority.host, None)
        assertEquals(path, "/path")

      case result => fail(s"Expected authority, got $result")
    }
  }

  test("Reject invalid authority components") {
    assertEquals(HierPart.fromString("//user name@example.com/path"), Left(InvalidCharacters))
    assertEquals(HierPart.fromString("//example.com:abc/path"), Left(InvalidCharacters))
  }

  test("Parse \"/a b\"") {
    assertEquals(
      HierPart.fromString("/a b"),
      Left(InvalidCharacters)
    )
  }

  test("Parse \"/%GG\"") {
    assertEquals(
      HierPart.fromString("/%GG"),
      Left(InvalidCharacters)
    )
  }

  test("Parse \"a b\"") {
    assertEquals(
      HierPart.fromString("a b"),
      Left(InvalidCharacters)
    )
  }
}
