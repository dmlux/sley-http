package sley.http

import sley.http.IPvFutureError.{
  Empty,
  EmptyAddress,
  InvalidAddress,
  InvalidPrefix,
  InvalidVersion,
  MissingSeparator,
  MissingVersion
}

class IpvFutureSuite extends munit.FunSuite {
  test("Parse \"v1.a\"") {
    IPvFuture.fromString("v1.a") match
      case Right(address) => assertEquals(address.raw, "v1.a")
      case Left(error)    => fail(s"Expected IPvFuture instance, got $error")
  }

  test("Parse \"v0.a\"") {
    IPvFuture.fromString("v0.a") match
      case Right(address) => assertEquals(address.raw, "v0.a")
      case Left(error)    => fail(s"Expected IPvFuture instance, got $error")
  }

  test("Parse \"vF.a\"") {
    IPvFuture.fromString("vF.a") match
      case Right(address) => assertEquals(address.raw, "vF.a")
      case Left(error)    => fail(s"Expected IPvFuture instance, got $error")
  }

  test("Parse \"V1.a\"") {
    IPvFuture.fromString("V1.a") match
      case Right(address) => assertEquals(address.raw, "V1.a")
      case Left(error)    => fail(s"Expected IPvFuture instance, got $error")
  }

  test("Parse \"vAbCd.example\"") {
    IPvFuture.fromString("vAbCd.example") match
      case Right(address) => assertEquals(address.raw, "vAbCd.example")
      case Left(error)    => fail(s"Expected IPvFuture instance, got $error")
  }

  test("Parse \"v123456789ABCDEF.test\"") {
    IPvFuture.fromString("v123456789ABCDEF.test") match
      case Right(address) => assertEquals(address.raw, "v123456789ABCDEF.test")
      case Left(error)    => fail(s"Expected IPvFuture instance, got $error")
  }

  test("Parse \"v1.12345\"") {
    IPvFuture.fromString("v1.12345") match
      case Right(address) => assertEquals(address.raw, "v1.12345")
      case Left(error)    => fail(s"Expected IPvFuture instance, got $error")
  }

  test("Parse \"v1.a:b:c\"") {
    IPvFuture.fromString("v1.a:b:c") match
      case Right(address) => assertEquals(address.raw, "v1.a:b:c")
      case Left(error)    => fail(s"Expected IPvFuture instance, got $error")
  }

  test("Parse \"v1.a.b.c\"") {
    IPvFuture.fromString("v1.a.b.c") match
      case Right(address) => assertEquals(address.raw, "v1.a.b.c")
      case Left(error)    => fail(s"Expected IPvFuture instance, got $error")
  }

  test("Parse \"v1.-._~\"") {
    IPvFuture.fromString("v1.-._~") match
      case Right(address) => assertEquals(address.raw, "v1.-._~")
      case Left(error)    => fail(s"Expected IPvFuture instance, got $error")
  }

  test("Parse \"v1.!$&'()*+,;=\"") {
    IPvFuture.fromString("v1.!$&'()*+,;=") match
      case Right(address) => assertEquals(address.raw, "v1.!$&'()*+,;=")
      case Left(error)    => fail(s"Expected IPvFuture instance, got $error")
  }

  test("Parse \"v1.ABCxyz123\"") {
    IPvFuture.fromString("v1.ABCxyz123") match
      case Right(address) => assertEquals(address.raw, "v1.ABCxyz123")
      case Left(error)    => fail(s"Expected IPvFuture instance, got $error")
  }

  test("Parse \"\"") {
    assertEquals(
      IPvFuture.fromString(""),
      Left(Empty)
    )
  }

  test("Parse \"v\"") {
    assertEquals(
      IPvFuture.fromString("v"),
      Left(MissingVersion)
    )
  }

  test("Parse \"v.a\"") {
    assertEquals(
      IPvFuture.fromString("v.a"),
      Left(MissingVersion)
    )
  }

  test("Parse \"vG.a\"") {
    assertEquals(
      IPvFuture.fromString("vG.a"),
      Left(InvalidVersion)
    )
  }

  test("Parse \"v1G.a\"") {
    assertEquals(
      IPvFuture.fromString("v1G.a"),
      Left(InvalidVersion)
    )
  }

  test("Parse \"x1.a\"") {
    assertEquals(
      IPvFuture.fromString("x1.a"),
      Left(InvalidPrefix)
    )
  }

  test("Parse \"1.a\"") {
    assertEquals(
      IPvFuture.fromString("1.a"),
      Left(InvalidPrefix)
    )
  }

  test("Parse \"v1\"") {
    assertEquals(
      IPvFuture.fromString("v1"),
      Left(MissingSeparator)
    )
  }

  test("Parse \"v123abc\"") {
    assertEquals(
      IPvFuture.fromString("v123abc"),
      Left(MissingSeparator)
    )
  }

  test("Parse \"v1.\"") {
    assertEquals(
      IPvFuture.fromString("v1."),
      Left(EmptyAddress)
    )
  }

  test("Parse \"v1.a@b\"") {
    assertEquals(
      IPvFuture.fromString("v1.a@b"),
      Left(InvalidAddress)
    )
  }

  test("Parse \"v1.a/b\"") {
    assertEquals(
      IPvFuture.fromString("v1.a/b"),
      Left(InvalidAddress)
    )
  }

  test("Parse \"v1.a?b\"") {
    assertEquals(
      IPvFuture.fromString("v1.a?b"),
      Left(InvalidAddress)
    )
  }

  test("Parse \"v1.a#b\"") {
    assertEquals(
      IPvFuture.fromString("v1.a#b"),
      Left(InvalidAddress)
    )
  }

  test("Parse \"v1.a%b\"") {
    assertEquals(
      IPvFuture.fromString("v1.a%b"),
      Left(InvalidAddress)
    )
  }

  test("Parse \"v1.a%20b\"") {
    assertEquals(
      IPvFuture.fromString("v1.a%20b"),
      Left(InvalidAddress)
    )
  }

  test("Parse \"v1.a b\"") {
    assertEquals(
      IPvFuture.fromString("v1.a b"),
      Left(InvalidAddress)
    )
  }

  test("Parse \"v1.ä\"") {
    assertEquals(
      IPvFuture.fromString("v1.ä"),
      Left(InvalidAddress)
    )
  }

  test("Parse \"v1.[abc]\"") {
    assertEquals(
      IPvFuture.fromString("v1.[abc]"),
      Left(InvalidAddress)
    )
  }

  test("Parse \"v1.a\\|b\"") {
    assertEquals(
      IPvFuture.fromString("v1.a\\|b"),
      Left(InvalidAddress)
    )
  }
}
