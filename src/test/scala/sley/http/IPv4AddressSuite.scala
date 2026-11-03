package sley.http

import sley.http.IPv4AddressError.{InvalidOctet, TooFewOctets, TooManyOctets}

class IPv4AddressSuite extends munit.FunSuite {
  test("parse \"192.168.1.1\"") {
    IPv4Address.fromString("192.168.1.1") match {
      case Right(IPv4Address(value)) => assertEquals(value, "192.168.1.1")
      case Left(error)               => fail(s"Expected IPv4Address instance, got $error")
    }
  }

  test("parse \"0.0.0.0\"") {
    IPv4Address.fromString("0.0.0.0") match {
      case Right(IPv4Address(value)) => assertEquals(value, "0.0.0.0")
      case Left(error)               => fail(s"Expected IPv4Address instance, got $error")
    }
  }

  test("parse \"255.255.255.255\"") {
    IPv4Address.fromString("255.255.255.255") match {
      case Right(IPv4Address(value)) => assertEquals(value, "255.255.255.255")
      case Left(error)               => fail(s"Expected IPv4Address instance, got $error")
    }
  }

  test("parse \"192.168.1\"") {
    assertEquals(
      IPv4Address.fromString("192.168.1"),
      Left(TooFewOctets)
    )
  }

  test("parse \"192.168.1.1.5\"") {
    assertEquals(
      IPv4Address.fromString("192.168.1.1.5"),
      Left(TooManyOctets)
    )
  }

  test("parse \"192.168.1.1.\"") {
    assertEquals(
      IPv4Address.fromString("192.168.1.1."),
      Left(TooManyOctets)
    )
  }

  test("parse \".192.168.1.1\"") {
    assertEquals(
      IPv4Address.fromString(".192.168.1.1"),
      Left(InvalidOctet)
    )
  }

  test("parse \"192..168.1\"") {
    assertEquals(
      IPv4Address.fromString("192..168.1"),
      Left(InvalidOctet)
    )
  }

  test("parse \"192.168.001.1\"") {
    assertEquals(
      IPv4Address.fromString("192.168.001.1"),
      Left(InvalidOctet)
    )
  }

  test("parse \"256.168.1.1\"") {
    assertEquals(
      IPv4Address.fromString("256.168.1.1"),
      Left(InvalidOctet)
    )
  }

  test("parse \"255.168.1a.1\"") {
    assertEquals(
      IPv4Address.fromString("256.168.1.1"),
      Left(InvalidOctet)
    )
  }
}
