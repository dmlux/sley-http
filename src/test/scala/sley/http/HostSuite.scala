package sley.http

import sley.http.HostError.{MalformedIPv6Address, UnterminatedIpLiteral}
import sley.http.IPv4AddressError.InvalidOctet
import sley.http.IPv6AddressError.{
  DoublePlaceholder,
  InvalidIPv4Group,
  TooFewGroups,
  TooLongGroup,
  TooManyGroups
}

class HostSuite extends munit.FunSuite {

  test("Parse \"192.0.2.1\"") {
    val host = Host.fromString("192.0.2.1")
    val hostInstance = host.map(identity).getOrElse(fail(s"Expected a IPv4Address, got $host"))
    assert(hostInstance.isInstanceOf[IPv4Address])
  }

  test("Parse \"256.1.1.1\"") {
    val host = Host.fromString("256.1.1.1")
    val hostInstance = host.map(identity).getOrElse(fail(s"Expected a RegName, got $host"))
    assert(hostInstance.isInstanceOf[RegName])
  }

  test("Parse \"01.2.3.4\"") {
    val host = Host.fromString("01.2.3.4")
    val hostInstance = host.map(identity).getOrElse(fail(s"Expected a RegName, got $host"))
    assert(hostInstance.isInstanceOf[RegName])
  }

  test("Parse \"[::1]\"") {
    val host = Host.fromString("[::1]")
    val hostInstance = host.map(identity).getOrElse(fail(s"Expected a IPv6Address, got $host"))
    assert(hostInstance.isInstanceOf[IPv6Address])
  }

  test("Parse \"[::]\"") {
    val host = Host.fromString("[::]")
    val hostInstance = host.map(identity).getOrElse(fail(s"Expected a IPv6Address, got $host"))
    assert(hostInstance.isInstanceOf[IPv6Address])
  }

  test("Parse \"[1:2:3:4:5:6:7:8]\"") {
    val host = Host.fromString("[1:2:3:4:5:6:7:8]")
    val hostInstance = host.map(identity).getOrElse(fail(s"Expected a IPv6Address, got $host"))
    assert(hostInstance.isInstanceOf[IPv6Address])
  }

  test("Parse \"[1:2:3:4:5:6:7::]\"") {
    val host = Host.fromString("[1:2:3:4:5:6:7::]")
    val hostInstance = host.map(identity).getOrElse(fail(s"Expected a IPv6Address, got $host"))
    assert(hostInstance.isInstanceOf[IPv6Address])
  }

  test("Parse \"[::ffff:192.0.2.1]\"") {
    val host = Host.fromString("[::ffff:192.0.2.1]")
    val hostInstance = host.map(identity).getOrElse(fail(s"Expected a IPv6Address, got $host"))
    assert(hostInstance.isInstanceOf[IPv6Address])
  }

  test("Parse \"[v1.x]\"") {
    val host = Host.fromString("[v1.x]")
    val hostInstance = host.map(identity).getOrElse(fail(s"Expected a IPvFuture, got $host"))
    assert(hostInstance.isInstanceOf[IPvFuture])
  }

  test("Parse \"[1::2:3:4:5:6:7:8]\"") {
    assertEquals(
      Host.fromString("[1::2:3:4:5:6:7:8]"),
      Left(MalformedIPv6Address(TooManyGroups))
    )
  }

  test("Parse \"[1:2:3:4:5:6:7]\"") {
    assertEquals(
      Host.fromString("[1:2:3:4:5:6:7]"),
      Left(MalformedIPv6Address(TooFewGroups))
    )
  }

  test("Parse \"[1::2::3]\"") {
    assertEquals(
      Host.fromString("[1::2::3]"),
      Left(MalformedIPv6Address(DoublePlaceholder))
    )
  }

  test("Parse \"[12345::]\"") {
    assertEquals(
      Host.fromString("[12345::]"),
      Left(MalformedIPv6Address(TooLongGroup))
    )
  }

  test("Parse \"[::ffff:256.0.0.1]\"") {
    assertEquals(
      Host.fromString("[::ffff:256.0.0.1]"),
      Left(MalformedIPv6Address(InvalidIPv4Group(InvalidOctet)))
    )
  }

  test("Parse \"[1.2.3.4::]\"") {
    assertEquals(
      Host.fromString("[1.2.3.4::]"),
      Left(MalformedIPv6Address(TooLongGroup))
    )
  }

  test("Parse \"[fe80::1%25eth0]\"") {
    assertEquals(
      Host.fromString("[fe80::1%25eth0]"),
      Left(MalformedIPv6Address(TooLongGroup))
    )
  }

  test("Parse \"[::1:443\"") {
    assertEquals(
      Host.fromString("[::1:443"),
      Left(UnterminatedIpLiteral)
    )
  }
}
