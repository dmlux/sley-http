package sley.http

import sley.http.syntax.CoreRules.isDigit
import sley.http.syntax.UriSyntax.{isEncodedSequenceOf, isPChar, isSubDelimiters, isUnreserved}

/** The authority component of a URI as defined in RFC 3986, section 3.2.
  *
  * The grammar is `authority = [ userinfo "@" ] host [ ":" port ]`.
  *
  * Every instance satisfies these invariants:
  *   - `userInfo`, if present, satisfies the `userinfo` rule of RFC 3986, section 3.2.1.
  *   - `host`, if present, is a [[Host]] as classified by [[Host.fromString]].
  *   - `port`, if present, consists only of ASCII digits.
  *
  * The generic URI syntax permits an empty host, an empty port, and a port of any length. This type
  * follows that syntax. It does not apply the rules of a specific scheme. For example, RFC 9110,
  * section 4.2.1, requires a non-empty host for `http` URIs, and RFC 9110, section 4.2.4, says that
  * userinfo in `http` and `https` URIs should be treated as an error.
  *
  * Instances can only be created through [[UriAuthority.fromString]]. All components keep their
  * original spelling.
  *
  * @param userInfo
  *   the userinfo without its trailing `@`, or `None` if the authority contains no `@`
  * @param host
  *   the classified host, or `None` if the host is empty
  * @param port
  *   the raw port without its leading `:`, or `None` if the authority contains no port separator.
  *   An authority that ends with `:` has an empty port.
  *
  * @author
  *   Denis-Michael Lux
  */
final case class UriAuthority private (
    userInfo: Option[String],
    host: Option[Host],
    port: Option[String]
)

/** Smart constructor for [[UriAuthority]].
  *
  * @author
  *   Denis-Michael Lux
  */
object UriAuthority {
  import HierPartError.*

  /** Tests a userinfo against `*( unreserved / pct-encoded / sub-delims / ":" )` from RFC 3986,
    * section 3.2.1.
    */
  private val isValidUserInfo =
    isEncodedSequenceOf(char => isUnreserved(char) || isSubDelimiters(char) || char == ':')

  /** Splits a raw authority at its first `@` into an optional userinfo and the remaining host and
    * port.
    *
    * A userinfo cannot contain `@`, so the first `@` ends it. A second `@` remains in the host part
    * and fails the host validation.
    *
    * @param authority
    *   the non-null raw authority
    * @return
    *   the userinfo without its `@`, or `None` if the authority contains no `@`, and the remaining
    *   host and port
    */
  private def extractUserInfo(authority: String): (Option[String], String) =
    authority.indexOf('@') match
      case -1    => (None, authority)
      case index =>
        val (userInfo, rest) = authority.splitAt(index)
        (Some(userInfo), rest.drop(1))

  /** Splits a raw host and port at the port separator `:` into a host and an optional port.
    *
    * For an IP literal in square brackets, the separator is the first `:` after the closing `]`,
    * because the literal itself contains colons. For every other host, it is the last `:`. An IP
    * literal without a closing `]` is returned unchanged as host, so that the host validation
    * reports it.
    *
    * @param authority
    *   the non-null raw host and port, without userinfo
    * @return
    *   the raw host and the raw port without its `:`, or `None` as port if there is no separator
    */
  private def extractPort(authority: String): (String, Option[String]) = {
    val splitIndex =
      if authority.startsWith("[") then
        val closingBracket = authority.indexOf(']')
        if closingBracket == -1 then -1
        else authority.indexOf(':', closingBracket + 1)
      else authority.lastIndexOf(':')

    splitIndex match
      case -1    => (authority, None)
      case index =>
        val (host, port) = authority.splitAt(index)
        (host, Some(port.drop(1)))
  }

  /** Classifies a raw host as defined by [[Host.fromString]].
    *
    * An empty host is valid in the generic URI syntax and yields `None`. The cause of an invalid
    * host is not preserved.
    *
    * @param rawHost
    *   the non-null raw host
    * @return
    *   a `Right` containing the classified host, or `None` if the host is empty, or a `Left`
    *   containing [[HierPartError.InvalidCharacters]] if the host is invalid
    */
  private def parseHost(rawHost: String): Either[HierPartError, Option[Host]] =
    if rawHost.isEmpty then Right(None)
    else
      Host
        .fromString(rawHost)
        .left
        .map(_ => InvalidCharacters)
        .map(Some(_))

  /** Creates a [[UriAuthority]] from a raw authority.
    *
    * Splits off the userinfo first and the port second. Then checks, in this order, the userinfo,
    * the host, and that the port consists only of ASCII digits. Returns the first error found.
    *
    * @param authority
    *   the non-null raw authority without the leading `//`
    * @return
    *   a `Right` containing the [[UriAuthority]], or a `Left` containing
    *   [[HierPartError.InvalidCharacters]] if a component is invalid
    */
  def fromString(authority: String): Either[HierPartError, UriAuthority] = {
    val (userInfo, hostAndPort) = extractUserInfo(authority)
    val (rawHost, port) = extractPort(hostAndPort)

    for {
      _ <- Either.cond(userInfo.forall(isValidUserInfo), (), InvalidCharacters)
      host <- parseHost(rawHost)
      _ <- Either.cond(port.forall(_.forall(isDigit)), (), InvalidCharacters)
    } yield UriAuthority(userInfo, host, port)
  }
}

/** The hierarchical part of an absolute URI as defined in RFC 3986, section 3.
  *
  * The grammar is:
  * {{{
  * hier-part = "//" authority path-abempty
  *           / path-absolute
  *           / path-rootless
  *           / path-empty
  * }}}
  *
  * Each alternative is a separate subtype. [[HierPart.fromString]] selects the alternative by the
  * leading characters.
  *
  * @author
  *   Denis-Michael Lux
  */
sealed trait HierPart

/** Syntax errors detected while creating a [[HierPart]] or a [[UriAuthority]].
  *
  * @author
  *   Denis-Michael Lux
  */
enum HierPartError {

  /** A component of the hierarchical part violates its syntax. The violated rule is not reported.
    */
  case InvalidCharacters
}

/** Subtypes and smart constructors for [[HierPart]].
  *
  * @author
  *   Denis-Michael Lux
  */
object HierPart {
  import HierPartError.*

  /** Tests the characters of a path against `*( "/" / pchar )`, with well-formed percent-encodings.
    *
    * The structure of the path, for example its first characters, is checked by the individual
    * alternatives.
    */
  private val isValidPath = isEncodedSequenceOf(char => char == '/' || isPChar(char))

  /** The alternative `"//" authority path-abempty` of RFC 3986, section 3.
    *
    * Every instance satisfies these invariants:
    *   - `authority` is a valid [[UriAuthority]].
    *   - `path` is empty or starts with `/`, and contains only characters permitted in a path.
    *
    * @param authority
    *   the authority between `//` and the path
    * @param path
    *   the path in its original spelling. It is empty or starts with `/`.
    *
    * @author
    *   Denis-Michael Lux
    */
  final case class Authority private (
      authority: UriAuthority,
      path: String
  ) extends HierPart

  /** Smart constructor for [[Authority]].
    *
    * @author
    *   Denis-Michael Lux
    */
  object Authority {

    /** Creates an [[Authority]] from a raw hierarchical part.
      *
      * The authority starts after the leading `//` and ends before the next `/`. The rest is the
      * path, which may be empty.
      *
      * @param path
      *   the non-null raw hierarchical part, including the leading `//`
      * @return
      *   a `Right` containing the [[Authority]], or a `Left` containing
      *   [[HierPartError.InvalidCharacters]] if the part does not start with `//`, or the authority
      *   or the path is invalid
      */
    def fromString(path: String): Either[HierPartError, Authority] = {
      if !path.startsWith("//") then Left(InvalidCharacters)
      else {
        val relevantPath = path.substring(2)
        val splitIndex = relevantPath.indexOf('/')
        val (authorityPart, pathAbempty) =
          if splitIndex == -1 then (relevantPath, "")
          else relevantPath.splitAt(splitIndex)

        for {
          authority <- UriAuthority.fromString(authorityPart)
          _ <- Either.cond(isValidPath(pathAbempty), (), InvalidCharacters)
        } yield Authority(authority, pathAbempty)
      }
    }
  }

  /** The alternative `path-absolute = "/" [ segment-nz *( "/" segment ) ]` of RFC 3986, section
    * 3.3.
    *
    * Every instance satisfies these invariants:
    *   - `path` starts with `/` but not with `//`.
    *   - `path` contains only characters permitted in a path.
    *
    * A path that starts with `//` would be read as an authority, so it is excluded.
    *
    * @param path
    *   the path in its original spelling
    *
    * @author
    *   Denis-Michael Lux
    */
  final case class Absolute private (
      path: String
  ) extends HierPart

  /** Smart constructor for [[Absolute]].
    *
    * @author
    *   Denis-Michael Lux
    */
  object Absolute {

    /** Creates an [[Absolute]] from a raw path.
      *
      * @param path
      *   the non-null raw path
      * @return
      *   a `Right` containing the [[Absolute]], or a `Left` containing
      *   [[HierPartError.InvalidCharacters]] if the path does not start with exactly one `/` or
      *   contains invalid characters
      */
    def fromString(path: String): Either[HierPartError, Absolute] =
      if !path.startsWith("/") || path.startsWith("//") || !isValidPath(path) then
        Left(InvalidCharacters)
      else Right(Absolute(path))
  }

  /** The alternative `path-rootless = segment-nz *( "/" segment )` of RFC 3986, section 3.3.
    *
    * Every instance satisfies these invariants:
    *   - `path` is not empty and does not start with `/`.
    *   - `path` contains only characters permitted in a path.
    *
    * An example is the `isbn:123` in `urn:isbn:123`.
    *
    * @param path
    *   the path in its original spelling
    *
    * @author
    *   Denis-Michael Lux
    */
  final case class Rootless private (
      path: String
  ) extends HierPart

  /** Smart constructor for [[Rootless]].
    *
    * @author
    *   Denis-Michael Lux
    */
  object Rootless {

    /** Creates a [[Rootless]] from a raw path.
      *
      * @param path
      *   the non-null raw path
      * @return
      *   a `Right` containing the [[Rootless]], or a `Left` containing
      *   [[HierPartError.InvalidCharacters]] if the path is empty, starts with `/`, or contains
      *   invalid characters
      */
    def fromString(path: String): Either[HierPartError, Rootless] =
      if path.isEmpty || path.startsWith("/") || !isValidPath(path) then Left(InvalidCharacters)
      else Right(Rootless(path))
  }

  /** The alternative `path-empty = 0<pchar>` of RFC 3986, section 3.3. The hierarchical part is
    * empty, as in `about:` or `about:?query`.
    */
  case object Empty extends HierPart

  /** Creates a [[HierPart]] from a raw hierarchical part.
    *
    * Selects the alternative by the leading characters:
    *   1. An empty part is [[Empty]].
    *   1. A part that starts with `//` is [[Authority]].
    *   1. A part that starts with `/` is [[Absolute]].
    *   1. Every other part is [[Rootless]].
    *
    * @param path
    *   the non-null raw hierarchical part, without scheme and query
    * @return
    *   a `Right` containing the selected alternative, or a `Left` containing
    *   [[HierPartError.InvalidCharacters]] if it is invalid
    */
  def fromString(path: String): Either[HierPartError, HierPart] =
    path match {
      case "" => Right(Empty)

      case s"//$remaining" => Authority.fromString(path)

      case s"/$remaining" => Absolute.fromString(path)

      case value => Rootless.fromString(value)
    }
}
