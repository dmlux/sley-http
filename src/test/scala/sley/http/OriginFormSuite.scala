package sley.http

class OriginFormSuite extends munit.FunSuite {
  test("OriginForm parse \"/\"") {
    OriginForm.fromString("/") match {
      case Right(originForm) =>
        assertEquals(originForm.path, "/")
        assertEquals(originForm.query, None)

      case Left(error) =>
        fail(s"Expected OriginForm instance, got $error")
    }
  }

  test("OriginForm parse \"/users\"") {
    OriginForm.fromString("/users") match {
      case Right(originForm) =>
        assertEquals(originForm.path, "/users")
        assertEquals(originForm.query, None)

      case Left(error) =>
        fail(s"Expected OriginForm instance, got $error")
    }
  }

  test("OriginForm parse \"/a//b\"") {
    OriginForm.fromString("/a//b") match {
      case Right(originForm) =>
        assertEquals(originForm.path, "/a//b")
        assertEquals(originForm.query, None)

      case Left(error) =>
        fail(s"Expected OriginForm instance, got $error")
    }
  }

  test("OriginForm parse \"/a%2F\"") {
    OriginForm.fromString("/a%2F") match {
      case Right(originForm) =>
        assertEquals(originForm.path, "/a%2F")
        assertEquals(originForm.query, None)

      case Left(error) =>
        fail(s"Expected OriginForm instance, got $error")
    }
  }

  test("OriginForm parse \"/a%2f\"") {
    OriginForm.fromString("/a%2f") match {
      case Right(originForm) =>
        assertEquals(originForm.path, "/a%2f")
        assertEquals(originForm.query, None)

      case Left(error) =>
        fail(s"Expected OriginForm instance, got $error")
    }
  }

  test("OriginForm parse \"/a%2Fb\"") {
    OriginForm.fromString("/a%2Fb") match {
      case Right(originForm) =>
        assertEquals(originForm.path, "/a%2Fb")
        assertEquals(originForm.query, None)

      case Left(error) =>
        fail(s"Expected OriginForm instance, got $error")
    }
  }

  test("OriginForm parse \"/search?\"") {
    OriginForm.fromString("/search?") match {
      case Right(originForm) =>
        assertEquals(originForm.path, "/search")
        assertEquals(originForm.query, Some(""))

      case Left(error) =>
        fail(s"Expected OriginForm instance, got $error")
    }
  }

  test("OriginForm parse \"/search?q=a/b?c\"") {
    OriginForm.fromString("/search?q=a/b?c") match {
      case Right(originForm) =>
        assertEquals(originForm.path, "/search")
        assertEquals(originForm.query, Some("q=a/b?c"))

      case Left(error) =>
        fail(s"Expected OriginForm instance, got $error")
    }
  }

  test("OriginForm parse \"/a%20b\"") {
    OriginForm.fromString("/a%20b") match {
      case Right(originForm) =>
        assertEquals(originForm.path, "/a%20b")
        assertEquals(originForm.query, None)

      case Left(error) =>
        fail(s"Expected OriginForm instance, got $error")
    }
  }

  test("OriginForm parse \"/search?q=%23\"") {
    OriginForm.fromString("/search?q=%23") match {
      case Right(originForm) =>
        assertEquals(originForm.path, "/search")
        assertEquals(originForm.query, Some("q=%23"))

      case Left(error) =>
        fail(s"Expected OriginForm instance, got $error")
    }
  }

  test("OriginForm parse \"\"") {
    assertEquals(
      OriginForm.fromString(""),
      Left(RequestTargetError.MalformedRequestTarget("Request target is empty."))
    )
  }

  test("OriginForm parse \"users\"") {
    assertEquals(
      OriginForm.fromString("users"),
      Left(RequestTargetError.MalformedRequestTarget("Request target must start with '/'."))
    )
  }

  test("OriginForm parse \"?q=x\"") {
    assertEquals(
      OriginForm.fromString("?q=x"),
      Left(RequestTargetError.MalformedRequestTarget("Request target must start with '/'."))
    )
  }

  test("OriginForm parse \"/a%\"") {
    assertEquals(
      OriginForm.fromString("/a%"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/a%2\"") {
    assertEquals(
      OriginForm.fromString("/a%2"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/a%GG\"") {
    assertEquals(
      OriginForm.fromString("/a%GG"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/a%2G\"") {
    assertEquals(
      OriginForm.fromString("/a%2G"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/a%2F#\"") {
    assertEquals(
      OriginForm.fromString("/a%2F#"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/search?q=%\"") {
    assertEquals(
      OriginForm.fromString("/search?q=%"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/search?q=%GG\"") {
    assertEquals(
      OriginForm.fromString("/search?q=%GG"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/us ers\"") {
    assertEquals(
      OriginForm.fromString("/us ers"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/#\"") {
    assertEquals(
      OriginForm.fromString("/#"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/usärs\"") {
    assertEquals(
      OriginForm.fromString("/usärs"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/search?q=%2\"") {
    assertEquals(
      OriginForm.fromString("/search?q=%2"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/search?q=%2G\"") {
    assertEquals(
      OriginForm.fromString("/search?q=%2G"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/search?q=a b\"") {
    assertEquals(
      OriginForm.fromString("/search?q=a b"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/search?q=ä\"") {
    assertEquals(
      OriginForm.fromString("/search?q=ä"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  test("OriginForm parse \"/search?q=#\"") {
    assertEquals(
      OriginForm.fromString("/search?q=#"),
      Left(
        RequestTargetError.MalformedRequestTarget(
          "Request target contains illegal characters."
        )
      )
    )
  }

  ((0 to 31) ++ Seq(127))
    .map(_.toChar)
    .foreach(char => {
      test(f"OriginForm rejects control character U+${char.toInt}%04X in path") {
        assertEquals(
          OriginForm.fromString(s"/$char"),
          Left(
            RequestTargetError.MalformedRequestTarget(
              "Request target contains illegal characters."
            )
          )
        )
      }

      test(f"OriginForm rejects control character U+${char.toInt}%04X in query") {
        assertEquals(
          OriginForm.fromString(s"/users?$char"),
          Left(
            RequestTargetError.MalformedRequestTarget(
              "Request target contains illegal characters."
            )
          )
        )
      }
    })
}
