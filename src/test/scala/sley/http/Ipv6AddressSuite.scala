package sley.http

import sley.http.IPv6AddressError.{
  DoublePlaceholder,
  Empty,
  EmptyGroup,
  InvalidGroup,
  InvalidIPv4Group,
  TooFewGroups,
  TooLongGroup,
  TooManyGroups
}

class Ipv6AddressSuite extends munit.FunSuite {

  test("Parse \"1:2:3:4:5:6:7:8\"") {
    IPv6Address.fromString("1:2:3:4:5:6:7:8") match {
      case Right(IPv6Address(value)) => assertEquals(value, "1:2:3:4:5:6:7:8")
      case Left(error)               => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"2001:db8:85a3:0:0:8a2e:370:7334\"") {
    IPv6Address.fromString("2001:db8:85a3:0:0:8a2e:370:7334") match {
      case Right(IPv6Address(value)) => assertEquals(value, "2001:db8:85a3:0:0:8a2e:370:7334")
      case Left(error)               => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"2001:0db8:0000:0000:0000:0000:0000:0001\"") {
    IPv6Address.fromString("2001:0db8:0000:0000:0000:0000:0000:0001") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "2001:0db8:0000:0000:0000:0000:0000:0001")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"ABCD:EF01:2345:6789:ABCD:EF01:2345:6789\"") {
    IPv6Address.fromString("ABCD:EF01:2345:6789:ABCD:EF01:2345:6789") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "ABCD:EF01:2345:6789:ABCD:EF01:2345:6789")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"2001:DB8::1\"") {
    IPv6Address.fromString("2001:DB8::1") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "2001:DB8::1")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"0:0:0:0:0:0:0:0\"") {
    IPv6Address.fromString("0:0:0:0:0:0:0:0") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "0:0:0:0:0:0:0:0")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"::\"") {
    IPv6Address.fromString("::") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "::")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"::1\"") {
    IPv6Address.fromString("::1") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "::1")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"1::\"") {
    IPv6Address.fromString("1::") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "1::")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"1::8\"") {
    IPv6Address.fromString("1::8") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "1::8")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"1:2:3::6:7:8\"") {
    IPv6Address.fromString("1:2:3::6:7:8") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "1:2:3::6:7:8")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"1:2:3:4:5:6:7::\"") {
    IPv6Address.fromString("1:2:3:4:5:6:7::") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "1:2:3:4:5:6:7::")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"::2:3:4:5:6:7:8\"") {
    IPv6Address.fromString("::2:3:4:5:6:7:8") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "::2:3:4:5:6:7:8")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"fe80::1\"") {
    IPv6Address.fromString("fe80::1") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "fe80::1")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"::ffff:192.0.2.1\"") {
    IPv6Address.fromString("::ffff:192.0.2.1") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "::ffff:192.0.2.1")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"::192.0.2.11\"") {
    IPv6Address.fromString("::192.0.2.11") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "::192.0.2.11")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"1:2:3:4:5:6:192.0.2.1\"") {
    IPv6Address.fromString("1:2:3:4:5:6:192.0.2.1") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "1:2:3:4:5:6:192.0.2.1")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"1::5:6:192.0.2.1\"") {
    IPv6Address.fromString("1::5:6:192.0.2.1") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "1::5:6:192.0.2.1")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"64:ff9b::192.0.2.33\"") {
    IPv6Address.fromString("64:ff9b::192.0.2.33") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "64:ff9b::192.0.2.33")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"1:2:3:4:5:6:7:8\"") {
    IPv6Address.fromString("1:2:3:4:5:6:7:8") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "1:2:3:4:5:6:7:8")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"2001:db8::192.0.2.1\"") {
    IPv6Address.fromString("2001:db8::192.0.2.1") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "2001:db8::192.0.2.1")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"1:2:3:4:5:6:192.0.2.1\"") {
    IPv6Address.fromString("1:2:3:4:5:6:192.0.2.1") match {
      case Right(IPv6Address(value)) =>
        assertEquals(value, "1:2:3:4:5:6:192.0.2.1")
      case Left(error) => fail(s"Expected IPv6 address, got $error")
    }
  }

  test("Parse \"\"") {
    assertEquals(
      IPv6Address.fromString(""),
      Left(Empty)
    )
  }

  test("Parse \":\"") {
    assertEquals(
      IPv6Address.fromString(":"),
      Left(EmptyGroup)
    )
  }

  test("Parse \":::\"") {
    assertEquals(
      IPv6Address.fromString(":::"),
      Left(EmptyGroup)
    )
  }

  test("Parse \"1:2:3:4:5:6:7\"") {
    assertEquals(
      IPv6Address.fromString("1:2:3:4:5:6:7"),
      Left(TooFewGroups)
    )
  }

  test("Parse \"1:2:3:4:5:6:7:8:9\"") {
    assertEquals(
      IPv6Address.fromString("1:2:3:4:5:6:7:8:9"),
      Left(TooManyGroups)
    )
  }

  test("Parse \"1::2:3:4:5:6:7:8\"") {
    assertEquals(
      IPv6Address.fromString("1::2:3:4:5:6:7:8"),
      Left(TooManyGroups)
    )
  }

  test("Parse \"1:2:3:4:5:6:7:8::\"") {
    assertEquals(
      IPv6Address.fromString("1:2:3:4:5:6:7:8::"),
      Left(TooManyGroups)
    )
  }

  test("Parse \"1::2::3\"") {
    assertEquals(
      IPv6Address.fromString("1::2::3"),
      Left(DoublePlaceholder)
    )
  }

  test("Parse \"1:::2\"") {
    assertEquals(
      IPv6Address.fromString("1:::2"),
      Left(EmptyGroup)
    )
  }

  test("Parse \":1:2:3:4:5:6:7:8\"") {
    assertEquals(
      IPv6Address.fromString(":1:2:3:4:5:6:7:8"),
      Left(EmptyGroup)
    )
  }

  test("Parse \"1:2:3:4:5:6:7:8:\"") {
    assertEquals(
      IPv6Address.fromString("1:2:3:4:5:6:7:8:"),
      Left(EmptyGroup)
    )
  }

  test("Parse \":1::\"") {
    assertEquals(
      IPv6Address.fromString(":1::"),
      Left(EmptyGroup)
    )
  }

  test("Parse \"1::2:\"") {
    assertEquals(
      IPv6Address.fromString("1::2:"),
      Left(EmptyGroup)
    )
  }

  test("Parse \"12345::\"") {
    assertEquals(
      IPv6Address.fromString("12345::"),
      Left(TooLongGroup)
    )
  }

  test("Parse \"1:2:3:4:5:6:7:g\"") {
    assertEquals(
      IPv6Address.fromString("1:2:3:4:5:6:7:g"),
      Left(InvalidGroup)
    )
  }

  test("Parse \"::ffff:256.0.0.1\"") {
    assertEquals(
      IPv6Address.fromString("::ffff:256.0.0.1"),
      Left(InvalidIPv4Group(IPv4AddressError.InvalidOctet))
    )
  }

  test("Parse \"::ffff:01.2.3.4\"") {
    assertEquals(
      IPv6Address.fromString("::ffff:01.2.3.4"),
      Left(InvalidIPv4Group(IPv4AddressError.InvalidOctet))
    )
  }

  test("Parse \"::ffff:1.2.3\"") {
    assertEquals(
      IPv6Address.fromString("::ffff:1.2.3"),
      Left(InvalidIPv4Group(IPv4AddressError.AddressTooShort))
    )
  }

  test("Parse \"::ffff:1.2.3.4.5\"") {
    assertEquals(
      IPv6Address.fromString("::ffff:1.2.3.4.5"),
      Left(InvalidIPv4Group(IPv4AddressError.TooManyOctets))
    )
  }

  test("Parse \"1.2.3.4::\"") {
    assertEquals(
      IPv6Address.fromString("1.2.3.4::"),
      Left(TooLongGroup)
    )
  }

  test("Parse \"::1.2.3.4:1\"") {
    assertEquals(
      IPv6Address.fromString("::1.2.3.4:1"),
      Left(TooLongGroup)
    )
  }

  test("Parse \"1:2:3:4:5:6:7:192.0.2.1\"") {
    assertEquals(
      IPv6Address.fromString("1:2:3:4:5:6:7:192.0.2.1"),
      Left(TooManyGroups)
    )
  }

  test("Parse \"1:2:3:4:5:192.0.2.1\"") {
    assertEquals(
      IPv6Address.fromString("1:2:3:4:5:192.0.2.1"),
      Left(TooFewGroups)
    )
  }

  test("Parse \"fe80::1%eth0\"") {
    assertEquals(
      IPv6Address.fromString("fe80::1%eth0"),
      Left(TooLongGroup)
    )
  }

  test("Parse \"fe80::1%25eth0\"") {
    assertEquals(
      IPv6Address.fromString("fe80::1%25eth0"),
      Left(TooLongGroup)
    )
  }

  test("Parse \"[::1]\"") {
    assertEquals(
      IPv6Address.fromString("[::1]"),
      Left(InvalidGroup)
    )
  }

  test("Parse \" ::1\"") {
    assertEquals(
      IPv6Address.fromString(" ::1"),
      Left(InvalidGroup)
    )
  }

  test("Parse \"1:2:3:4:5:6:7:8::\"") {
    assertEquals(
      IPv6Address.fromString("1:2:3:4:5:6:7:8::"),
      Left(TooManyGroups)
    )
  }

  test("Parse \"1::2::3\"") {
    assertEquals(
      IPv6Address.fromString("1::2::3"),
      Left(DoublePlaceholder)
    )
  }

  test("Parse \"1:2:3:4:5:6:7:00000\"") {
    assertEquals(
      IPv6Address.fromString("1:2:3:4:5:6:7:00000"),
      Left(TooLongGroup)
    )
  }

  test("Parse \"1:2:3:4:5:6:7:192.0.2.1\"") {
    assertEquals(
      IPv6Address.fromString("1:2:3:4:5:6:7:192.0.2.1"),
      Left(TooManyGroups)
    )
  }

  test("Reject single colon after compression") {
    assertEquals(
      IPv6Address.fromString("1::2:3:"),
      Left(EmptyGroup)
    )
  }
}
