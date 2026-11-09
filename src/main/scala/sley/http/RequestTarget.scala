package sley.http

import sley.http.RequestTargetError.{
  AsteriskFormNotAllowed,
  MalformedAbsoluteForm,
  MalformedAuthorityForm,
  MalformedOriginForm
}
import sley.http.RequestTargetValidationError.*
import sley.http.RequestTargetValidator.*
import sley.http.syntax.CoreRules.isDigit
import sley.http.syntax.UriSyntax.{isEncodedSequenceOf, isPChar}

/** Syntax errors detected while creating a [[RequestTarget]].
  *
  * @author
  *   Denis-Michael Lux
  */
enum RequestTargetError {

  /** The target starts with `/` and violates the syntax of the origin form. */
  case MalformedOriginForm(cause: RequestTargetValidationError)

  /** The target was parsed as absolute form and violates its syntax. */
  case MalformedAbsoluteForm(cause: RequestTargetValidationError)

  /** The method is `CONNECT`, which requires the authority form, and the target violates its
    * syntax.
    */
  case MalformedAuthorityForm(cause: RequestTargetValidationError)

  /** The target is `*`, but the method is not `OPTIONS`. RFC 9112, section 3.2.4, permits the
    * asterisk form only for `OPTIONS`.
    */
  case AsteriskFormNotAllowed
}

/** A syntactically valid request target as defined in RFC 9112, section 3.2.
  *
  * RFC 9112 defines four request-target forms: origin form, absolute form, authority form, and
  * asterisk form. [[RequestTarget.fromString]] selects the form based on the method and the target.
  *
  * All four request-target forms are implemented.
  *
  * @author
  *   Denis-Michael Lux
  */
sealed trait RequestTarget

/** A request target in origin form as defined in RFC 9112, section 3.2.1.
  *
  * Every instance satisfies these invariants:
  *   - `path` starts with `/` and contains only characters permitted in an absolute path.
  *   - `query`, if present, contains only characters permitted in a URI query.
  *   - Every `%` in either component starts a percent-encoding with two hexadecimal digits.
  *
  * Instances can only be created through [[OriginForm.fromString]]. Both components keep their
  * original spelling. Percent-encodings are not decoded, and empty or dot segments are not
  * normalized.
  *
  * @param path
  *   the absolute path, always starting with `/`
  * @param query
  *   the query without its leading `?`, or `None` if the target contains no `?`. A target that ends
  *   with `?` has an empty query.
  *
  * @author
  *   Denis-Michael Lux
  */
final case class OriginForm private (
    path: String,
    query: Option[String]
) extends RequestTarget

/** Smart constructor for [[OriginForm]].
  *
  * @author
  *   Denis-Michael Lux
  */
object OriginForm {

  /** Splits a raw request target at the first `?` into a path and an optional query.
    *
    * The query excludes the separating `?`. Any later `?` remains part of the query.
    *
    * @param rawRequestTarget
    *   the non-null raw request target
    * @return
    *   the raw path and the raw query, or `None` as the query if the target contains no `?`
    */
  private def separateIntoPathAndQuery(rawRequestTarget: String): (String, Option[String]) = {
    val splitIndex = rawRequestTarget.indexOf('?')

    if splitIndex == -1 then (rawRequestTarget, None)
    else
      val (path, queryWithQuestionMark) = rawRequestTarget.splitAt(splitIndex)
      (path, Some(queryWithQuestionMark.drop(1)))
  }

  /** Creates an [[OriginForm]] from a raw request target.
    *
    * Checks that the target is not empty, that the path starts with `/`, and that path and query
    * contain only permitted characters and well-formed percent-encodings. Returns the first error
    * found.
    *
    * @param rawRequestTarget
    *   the non-null raw request target
    * @return
    *   a `Right` containing the [[OriginForm]], or a `Left` containing
    *   [[RequestTargetError.MalformedOriginForm]] if the target is not a valid origin form
    */
  def fromString(rawRequestTarget: String): Either[RequestTargetError, OriginForm] = {
    val validated = for {
      _ <- validateTargetNotEmpty(rawRequestTarget)
      (rawPath, rawQuery) = separateIntoPathAndQuery(rawRequestTarget)
      _ <- validatePathStartWithSlash(rawPath)
      _ <- validatePathCharacters(rawPath)
      _ <- rawQuery.fold[Either[RequestTargetValidationError, Unit]](Right(()))(
        validateQueryCharacters
      )
    } yield OriginForm(rawPath, rawQuery)

    validated.left.map(MalformedOriginForm.apply)
  }
}

/** A request target in authority form as defined in RFC 9112, section 3.2.3.
  *
  * The grammar is `authority-form = uri-host ":" port`. The authority form is only used with
  * `CONNECT` and names the destination of a tunnel.
  *
  * Every instance satisfies these invariants:
  *   - `host` is a [[Host]] as classified by [[Host.fromString]]. It is never empty.
  *   - `port` is a number from 0 through 65535.
  *
  * The ABNF permits an empty host and an empty port. RFC 9110, section 9.3.6, requires rejecting an
  * empty port. An empty host is rejected as well, because a tunnel needs a destination.
  *
  * Instances can only be created through [[AuthorityForm.fromString]]. The host keeps its original
  * spelling. The port is stored as a number, so leading zeros are not preserved.
  *
  * @param host
  *   the host of the tunnel destination in its original spelling
  * @param port
  *   the port number of the tunnel destination
  *
  * @author
  *   Denis-Michael Lux
  */
final case class AuthorityForm private (
    host: Host,
    port: Int
) extends RequestTarget

/** Smart constructor for [[AuthorityForm]].
  *
  * @author
  *   Denis-Michael Lux
  */
object AuthorityForm {

  /** Splits a raw authority at its last `:` into a host and a port.
    *
    * The port excludes the separating `:`. Splitting at the last colon keeps the port intact even
    * if the host contains colons, which only IP literals do.
    *
    * @param rawRequestTarget
    *   the non-null raw request target
    * @return
    *   a `Right` containing the raw host and the raw port, or a `Left` containing
    *   [[RequestTargetValidationError.MissingColon]] if the target contains no `:`
    */
  private def splitIntoUriHostAndPort(
      rawRequestTarget: String
  ): Either[RequestTargetValidationError, (String, String)] = {
    val splitIndex = rawRequestTarget.lastIndexOf(':')

    if splitIndex == -1 then Left(MissingColon)
    else
      val (rawUriHost, rawPort) = rawRequestTarget.splitAt(splitIndex)
      Right((rawUriHost, rawPort.drop(1)))
  }

  /** Creates an [[AuthorityForm]] from a raw request target.
    *
    * Checks, in this order, that the target is not empty, that it contains a `:`, that the port is
    * not empty and consists only of ASCII digits, that the port is at most 65535, and that the host
    * is valid. Returns the first error found.
    *
    * @param rawRequestTarget
    *   the non-null raw request target
    * @return
    *   a `Right` containing the [[AuthorityForm]], or a `Left` containing
    *   [[RequestTargetError.MalformedAuthorityForm]] with the violated rule as its cause
    */
  def fromString(rawRequestTarget: String): Either[RequestTargetError, AuthorityForm] = {
    val validated = for {
      _ <- validateTargetNotEmpty(rawRequestTarget)
      (rawUriHost, rawPort) <- splitIntoUriHostAndPort(rawRequestTarget)
      _ <- validatePort(rawPort)
      port <- rawPort.toIntOption.toRight(PortOutOfRange)
      _ <- validatePortRange(port)
      host <- validateHost(rawUriHost)
    } yield AuthorityForm(host, port)

    validated.left.map(MalformedAuthorityForm.apply)
  }
}

/** The asterisk form of a request target. It contains just `*` as its value.
  *
  * @author
  *   Denis-Michael Lux
  */
case object AsteriskForm extends RequestTarget {

  /** The wire representation of the asterisk form. */
  val value: String = "*"
}

/** A request target in absolute form as defined in RFC 9112, section 3.2.2.
  *
  * The grammar is `absolute-form = absolute-URI` with `absolute-URI = scheme ":" hier-part [ "?"
  * query ]` from RFC 3986, section 4.3. An absolute URI has no fragment.
  *
  * Every instance satisfies these invariants:
  *   - `scheme` is a valid [[Scheme]].
  *   - `part` is a valid [[HierPart]].
  *   - `query`, if present, contains only characters permitted in a URI query.
  *
  * The form follows the generic URI syntax for every scheme. Rules of a specific scheme, such as
  * the requirements for `http` URIs in RFC 9110, section 4.2, are not applied.
  *
  * Instances can only be created through [[AbsoluteForm.fromString]]. All components keep their
  * original spelling.
  *
  * @param scheme
  *   the URI scheme
  * @param part
  *   the hierarchical part of the URI
  * @param query
  *   the query without its leading `?`, or `None` if the target contains no `?`
  *
  * @author
  *   Denis-Michael Lux
  */
final case class AbsoluteForm private (
    scheme: Scheme,
    part: HierPart,
    query: Option[String]
) extends RequestTarget

/** Smart constructor for [[AbsoluteForm]].
  *
  * @author
  *   Denis-Michael Lux
  */
object AbsoluteForm {

  /** Splits a raw request target at its first `:` into a scheme and the rest.
    *
    * A scheme cannot contain `:`, so the first `:` ends it.
    *
    * @param rawRequestTarget
    *   the non-null raw request target
    * @return
    *   a `Right` containing the raw scheme and the raw hierarchical part with the query, or a
    *   `Left` containing [[RequestTargetValidationError.MissingSchemeSeparator]] if the target
    *   contains no `:`
    */
  private def extractScheme(
      rawRequestTarget: String
  ): Either[RequestTargetValidationError, (String, String)] =
    rawRequestTarget.indexOf(':') match
      case -1    => Left(MissingSchemeSeparator)
      case index =>
        Right(
          (rawRequestTarget.substring(0, index), rawRequestTarget.substring(index + 1))
        )

  /** Splits a raw hierarchical part with query at its first `?` into a hierarchical part and an
    * optional query.
    *
    * A hierarchical part cannot contain `?`, so the first `?` starts the query.
    *
    * @param rawHierPartAndQuery
    *   the non-null part of the target after the scheme separator
    * @return
    *   the raw hierarchical part and the raw query without its `?`, or `None` as the query if there
    *   is no `?`
    */
  private def extractQuery(rawHierPartAndQuery: String): (String, Option[String]) =
    rawHierPartAndQuery.indexOf('?') match
      case -1    => (rawHierPartAndQuery, None)
      case index =>
        (rawHierPartAndQuery.substring(0, index), Some(rawHierPartAndQuery.substring(index + 1)))

  /** Creates an [[AbsoluteForm]] from a raw request target.
    *
    * Checks that the target is not empty and contains a `:`. Then splits off the query and checks,
    * in this order, the scheme, the hierarchical part, and the query. Returns the first error
    * found.
    *
    * @param rawRequestTarget
    *   the non-null raw request target
    * @return
    *   a `Right` containing the [[AbsoluteForm]], or a `Left` containing
    *   [[RequestTargetError.MalformedAbsoluteForm]] if the target is not a valid absolute form
    */
  def fromString(rawRequestTarget: String): Either[RequestTargetError, AbsoluteForm] = {
    val validated = for {
      _ <- validateTargetNotEmpty(rawRequestTarget)
      (rawScheme, rawHierPartAndQuery) <- extractScheme(rawRequestTarget)
      (rawHierPart, rawQuery) = extractQuery(rawHierPartAndQuery)
      scheme <- Scheme.fromString(rawScheme).left.map(InvalidScheme.apply)
      hierPart <- HierPart.fromString(rawHierPart).left.map(InvalidHierPart.apply)
      _ <- rawQuery.fold[Either[RequestTargetValidationError, Unit]](Right(()))(
        validateQueryCharacters
      )
    } yield AbsoluteForm(scheme, hierPart, rawQuery)

    validated.left.map(MalformedAbsoluteForm.apply)
  }
}

/** Smart constructor for [[RequestTarget]].
  *
  * @author
  *   Denis-Michael Lux
  */
object RequestTarget {

  /** Creates a [[RequestTarget]] from a raw request target and the method of the request.
    *
    * The method and the target select the request-target form:
    *   1. `CONNECT` requires the authority form.
    *   1. `*` is the asterisk form, which is only permitted for `OPTIONS`.
    *   1. A target that starts with `/` is in origin form.
    *   1. Every other target is parsed as absolute form.
    *
    * The selection resolves ambiguous targets such as `example.com:443`, which matches both the
    * authority form and the absolute form.
    *
    * @param method
    *   the method of the request, which selects the permitted form
    * @param rawRequestTarget
    *   the non-null raw request target
    * @return
    *   a `Right` containing the validated target, or a `Left` containing
    *   [[RequestTargetError.AsteriskFormNotAllowed]] or the error of the selected form
    */
  def fromString(
      method: RequestMethod,
      rawRequestTarget: String
  ): Either[RequestTargetError, RequestTarget] = method match
    case RequestMethod("CONNECT") => AuthorityForm.fromString(rawRequestTarget)
    case RequestMethod("OPTIONS") if rawRequestTarget == "*" => Right(AsteriskForm)
    case _ if rawRequestTarget == "*"                        => Left(AsteriskFormNotAllowed)
    case _ if rawRequestTarget.startsWith("/") => OriginForm.fromString(rawRequestTarget)
    case _                                     => AbsoluteForm.fromString(rawRequestTarget)
}

/** Violations of the rules for individual request-target components.
  *
  * The cases are shared by all request-target forms. A [[RequestTargetError]] wraps them and adds
  * which form was being parsed.
  *
  * @author
  *   Denis-Michael Lux
  */
private enum RequestTargetValidationError {

  /** The request target is empty. */
  case EmptyTarget

  /** The absolute URI contains no `:` separating its scheme from its hierarchical part. */
  case MissingSchemeSeparator

  /** The scheme of an absolute URI violates the generic URI syntax. */
  case InvalidScheme(cause: SchemeError)

  /** The hierarchical part of an absolute URI violates the generic URI syntax. */
  case InvalidHierPart(cause: HierPartError)

  /** The path does not start with `/`. */
  case MissingLeadingSlash

  /** The path contains a character or percent-encoding that is not permitted. */
  case InvalidPathCharacters

  /** The query contains a character or percent-encoding that is not permitted. */
  case InvalidQueryCharacters

  /** The port is empty. RFC 9110, section 9.3.6, requires rejecting an empty port for `CONNECT`. */
  case EmptyPort

  /** The port contains a character other than an ASCII digit. */
  case InvalidPortCharacters

  /** The port is greater than 65535, the largest TCP port. */
  case PortOutOfRange

  /** The authority contains no `:` between host and port. */
  case MissingColon

  /** The host violates the host syntax. `cause` names the violated rule. */
  case InvalidHost(cause: HostError)
}

/** Syntactic validation rules for the components of a request target.
  *
  * The rules follow RFC 9112, section 3.2, which builds on the URI syntax of RFC 3986, sections
  * 3.2.2 through 3.4.
  *
  * @author
  *   Denis-Michael Lux
  */
private object RequestTargetValidator {

  /** Tests the characters of a path against `1*( "/" segment )` with `segment = *pchar`.
    *
    * The rule is the `absolute-path` rule from RFC 9110, section 4.1, built on RFC 3986, section
    * 3.3. This validator checks only the permitted characters. That the path starts with `/` is
    * checked by [[validatePathStartWithSlash]].
    */
  private val isValidPath = isEncodedSequenceOf(c => c == '/' || isPChar(c))

  /** Tests a query against `query = *( pchar / "/" / "?" )` from RFC 3986, section 3.4. */
  private val isValidQuery = isEncodedSequenceOf(c => c == '/' || c == '?' || isPChar(c))

  /** Validates the characters and percent-encodings of a path.
    *
    * A path permits `pchar`, percent-encodings, and the segment separator `/`.
    *
    * @param rawPath
    *   the raw path of a request target
    * @return
    *   `Right(())` if the path is valid, or a `Left` containing [[RequestTargetValidationError]]
    *   otherwise
    */
  def validatePathCharacters(rawPath: String): Either[RequestTargetValidationError, Unit] =
    if isValidPath(rawPath) then Right(())
    else Left(InvalidPathCharacters)

  /** Validates the characters and percent-encodings of a query.
    *
    * A query permits the same characters as a path plus `?`, as defined in RFC 3986, section 3.4.
    *
    * @param rawQuery
    *   the raw query without its leading `?`
    * @return
    *   `Right(())` if the query is valid, or a `Left` containing [[RequestTargetValidationError]]
    *   otherwise
    */
  def validateQueryCharacters(rawQuery: String): Either[RequestTargetValidationError, Unit] =
    if isValidQuery(rawQuery) then Right(())
    else Left(InvalidQueryCharacters)

  /** Validates that a request target is not empty.
    *
    * @param rawRequestTarget
    *   the raw request target
    * @return
    *   `Right(())` if the target is not empty, or a `Left` containing
    *   [[RequestTargetValidationError]] otherwise
    */
  def validateTargetNotEmpty(rawRequestTarget: String): Either[RequestTargetValidationError, Unit] =
    if rawRequestTarget.nonEmpty then Right(())
    else Left(EmptyTarget)

  /** Validates that a path starts with `/`.
    *
    * @param rawPath
    *   the raw path of a request target
    * @return
    *   `Right(())` if the path starts with `/`, or a `Left` containing
    *   [[RequestTargetValidationError]] otherwise
    */
  def validatePathStartWithSlash(rawPath: String): Either[RequestTargetValidationError, Unit] =
    if rawPath.startsWith("/") then Right(())
    else Left(MissingLeadingSlash)

  /** Validates that a port is not empty and consists only of ASCII digits.
    *
    * RFC 3986 permits an empty port, but RFC 9110, section 9.3.6, requires rejecting it for
    * `CONNECT`.
    *
    * @param rawPort
    *   the raw port of a request target, without its leading `:`
    * @return
    *   `Right(())` if the port is valid, or a `Left` containing [[RequestTargetValidationError]]
    *   otherwise
    */
  def validatePort(rawPort: String): Either[RequestTargetValidationError, Unit] =
    if rawPort.isEmpty then Left(EmptyPort)
    else if !rawPort.forall(isDigit) then Left(InvalidPortCharacters)
    else Right(())

  /** Validates that a port number is at most 65535, the largest TCP port.
    *
    * @param port
    *   the port number
    * @return
    *   `Right(())` if the port is in range, or a `Left` containing
    *   [[RequestTargetValidationError.PortOutOfRange]] otherwise
    */
  def validatePortRange(port: Int): Either[RequestTargetValidationError, Unit] =
    if port < 0 || port > 65535 then Left(PortOutOfRange)
    else Right(())

  /** Classifies a host as defined by [[Host.fromString]].
    *
    * @param rawHost
    *   the raw host of a request target
    * @return
    *   a `Right` containing the classified [[Host]], or a `Left` containing
    *   [[RequestTargetValidationError.InvalidHost]] otherwise
    */
  def validateHost(rawHost: String): Either[RequestTargetValidationError, Host] =
    Host.fromString(rawHost).left.map(InvalidHost.apply)
}
