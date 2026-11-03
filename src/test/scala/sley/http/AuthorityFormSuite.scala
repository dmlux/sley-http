package sley.http

import sley.http.HostError.MalformedRegName
import sley.http.RegNameError.InvalidCharacters
import sley.http.RequestTargetError.MalformedAuthorityForm
import sley.http.RequestTargetValidationError.*

class AuthorityFormSuite extends munit.FunSuite {
  test("AuthorityForm parse \"example.com:443\"") {
    AuthorityForm.fromString("example.com:443") match {
      case Right(authorityForm) =>
        authorityForm.host match
          case RegName(raw) => assertEquals(raw, "example.com")
          case other        => fail(s"Expected RegName, got $other")
        assertEquals(authorityForm.port, 443)

      case Left(error) =>
        fail(s"Expected AuthorityForm instance, got $error")
    }
  }

  test("AuthorityForm parse \"127.0.0.1:8080\"") {
    AuthorityForm.fromString("127.0.0.1:8080") match {
      case Right(authorityForm) =>
        authorityForm.host match
          case IPv4Address(raw) => assertEquals(raw, "127.0.0.1")
          case other            => fail(s"Expected IPv4Address, got $other")
        assertEquals(authorityForm.port, 8080)

      case Left(error) =>
        fail(s"Expected AuthorityForm instance, got $error")
    }
  }

  test("AuthorityForm parse \"ex%41mple.com:443\"") {
    AuthorityForm.fromString("ex%41mple.com:443") match {
      case Right(authorityForm) =>
        authorityForm.host match
          case RegName(raw) => assertEquals(raw, "ex%41mple.com")
          case other        => fail(s"Expected RegName, got $other")
        assertEquals(authorityForm.port, 443)

      case Left(error) =>
        fail(s"Expected AuthorityForm instance, got $error")
    }
  }

  test("AuthorityForm parse \"example.com\"") {
    assertEquals(
      AuthorityForm.fromString("example.com"),
      Left(MalformedAuthorityForm(MissingColon))
    )
  }

  test("AuthorityForm parse \"example.com:\"") {
    assertEquals(
      AuthorityForm.fromString("example.com:"),
      Left(MalformedAuthorityForm(EmptyPort))
    )
  }

  test("AuthorityForm parse \":443\"") {
    assertEquals(
      AuthorityForm.fromString(":443"),
      Left(MalformedAuthorityForm(InvalidHost(HostError.Empty)))
    )
  }

  test("AuthorityForm parse \"example.com:+443\"") {
    assertEquals(
      AuthorityForm.fromString("example.com:+443"),
      Left(MalformedAuthorityForm(InvalidPortCharacters))
    )
  }

  test("AuthorityForm parse \"example.com:65536\"") {
    assertEquals(
      AuthorityForm.fromString("example.com:65536"),
      Left(MalformedAuthorityForm(PortOutOfRange))
    )
  }

  test("AuthorityForm parse \"example.com:99999999999\"") {
    assertEquals(
      AuthorityForm.fromString("example.com:99999999999"),
      Left(MalformedAuthorityForm(PortOutOfRange))
    )
  }

  test("AuthorityForm parse \"user@example.com:443\"") {
    assertEquals(
      AuthorityForm.fromString("user@example.com:443"),
      Left(MalformedAuthorityForm(InvalidHost(MalformedRegName(InvalidCharacters))))
    )
  }

  test("AuthorityForm parse \"example.com:443/path\"") {
    assertEquals(
      AuthorityForm.fromString("example.com:443/path"),
      Left(MalformedAuthorityForm(InvalidPortCharacters))
    )
  }
}
