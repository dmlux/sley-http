package sley.http

class AbsoluteFormSuite extends munit.FunSuite {

  private def parse(requestTarget: String): AbsoluteForm =
    AbsoluteForm.fromString(requestTarget) match
      case Right(form) => form
      case Left(error) => fail(s"Expected AbsoluteForm, got $error")

  private def assertMalformed(requestTarget: String): Unit =
    AbsoluteForm.fromString(requestTarget) match
      case Left(RequestTargetError.MalformedAbsoluteForm(_)) => ()
      case Left(error) => fail(s"Expected MalformedAbsoluteForm, got $error")
      case Right(form) => fail(s"Expected malformed absolute-form, got $form")

  test("Parse authority, path, and query") {
    val form = parse("http://example.com:8080/users/42?active=true")

    assertEquals(form.scheme.raw, "http")
    assertEquals(form.query, Some("active=true"))

    form.part match {
      case HierPart.Authority(authority, path) =>
        assertEquals(path, "/users/42")
        assertEquals(authority.userInfo, None)
        assertEquals(authority.port, Some("8080"))
        authority.host match {
          case Some(RegName(raw)) => assertEquals(raw, "example.com")
          case host               => fail(s"Expected RegName host, got $host")
        }

      case part => fail(s"Expected authority hier-part, got $part")
    }
  }

  test("Parse IPv6 authority") {
    val form = parse("http://[::1]:8080/status")

    form.part match {
      case HierPart.Authority(authority, path) =>
        assertEquals(path, "/status")
        assertEquals(authority.port, Some("8080"))
        authority.host match {
          case Some(IPv6Address(raw)) => assertEquals(raw, "::1")
          case host                   => fail(s"Expected IPv6 host, got $host")
        }

      case part => fail(s"Expected authority hier-part, got $part")
    }
  }

  test("Parse authority with empty path") {
    val form = parse("http://example.com?name=ferret")

    assertEquals(form.query, Some("name=ferret"))
    form.part match {
      case HierPart.Authority(_, path) => assertEquals(path, "")
      case part                        => fail(s"Expected authority hier-part, got $part")
    }
  }

  test("Parse absolute path") {
    val form = parse("file:/var/data")

    assertEquals(form.scheme.raw, "file")
    form.part match {
      case HierPart.Absolute(path) => assertEquals(path, "/var/data")
      case part                    => fail(s"Expected absolute hier-part, got $part")
    }
    assertEquals(form.query, None)
  }

  test("Parse rootless path") {
    val form = parse("urn:example:animal:ferret:nose")

    assertEquals(form.scheme.raw, "urn")
    form.part match {
      case HierPart.Rootless(path) => assertEquals(path, "example:animal:ferret:nose")
      case part                    => fail(s"Expected rootless hier-part, got $part")
    }
    assertEquals(form.query, None)
  }

  test("Parse empty path and empty query") {
    val form = parse("example:?")

    assertEquals(form.scheme.raw, "example")
    assertEquals(form.part, HierPart.Empty)
    assertEquals(form.query, Some(""))
  }

  test("Keep later question marks in query") {
    val form = parse("http://example.com/search?q=what?where")

    assertEquals(form.query, Some("q=what?where"))
  }

  List(
    "",
    "example.com/path",
    "1http://example.com",
    "http://example.com/a b",
    "http://example.com/%GG",
    "http://example.com/path?query with space",
    "http://example.com/path?%GG",
    "http://example.com/path#fragment"
  ).foreach { requestTarget =>
    test(s"Reject malformed absolute-form: $requestTarget") {
      assertMalformed(requestTarget)
    }
  }
}
