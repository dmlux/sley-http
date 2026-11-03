package sley.http

import sley.http.syntax.CoreRules.isDigit
import sley.http.RequestTargetError.{
  AsteriskFormNotAllowed,
  MalformedAuthorityForm,
  MalformedOriginForm
}
import sley.http.RequestTargetValidationError.{
  EmptyHost,
  EmptyPort,
  EmptyTarget,
  InvalidPathCharacters,
  InvalidPortCharacters,
  InvalidQueryCharacters,
  InvalidUriHostCharacters,
  MissingColon,
  MissingLeadingSlash,
  PortOutOfRange
}
import sley.http.RequestTargetValidator.*
import sley.http.syntax.UriSyntax.{isEncodedSequenceOf, isPChar, isSubDelimiters, isUnreserved}

/** Syntax errors detected while creating a [[RequestTarget]].
  *
  * @author
  *   Denis-Michael Lux
  */
enum RequestTargetError {
  case MalformedOriginForm(cause: RequestTargetValidationError)
  case MalformedAbsoluteForm(cause: RequestTargetValidationError)
  case MalformedAuthorityForm(cause: RequestTargetValidationError)
  case AsteriskFormNotAllowed
}

/** A syntactically valid request target as defined in RFC 9112, section 3.2.
  *
  * RFC 9112 defines four request-target forms: origin form, absolute form, authority form, and
  * asterisk form. Sley currently supports only the origin form, represented by [[OriginForm]].
  * Targets in any other form are rejected as malformed.
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

final case class AuthorityForm private (
    host: String,
    port: Int
) extends RequestTarget

object AuthorityForm {
  private def splitIntoUriHostAndPort(
      rawRequestTarget: String
  ): Either[RequestTargetValidationError, (String, String)] = {
    val splitIndex = rawRequestTarget.lastIndexOf(':')

    if splitIndex == -1 then Left(MissingColon)
    else
      val (rawUriHost, rawPort) = rawRequestTarget.splitAt(splitIndex)
      Right((rawUriHost, rawPort.drop(1)))
  }

  def fromString(rawRequestTarget: String): Either[RequestTargetError, AuthorityForm] = {
    val validated = for {
      _ <- validateTargetNotEmpty(rawRequestTarget)
      (rawUriHost, rawPort) <- splitIntoUriHostAndPort(rawRequestTarget)
      _ <- validateUriHost(rawUriHost)
      _ <- validatePort(rawPort)
      port <- rawPort.toIntOption.toRight(PortOutOfRange)
      _ <- validatePortRange(port)
    } yield AuthorityForm(rawUriHost, port)

    validated.left.map(MalformedAuthorityForm.apply)
  }
}

/** The asterisk form of a request target. It contains just `*` as its value.
  *
  * @author
  *   Denis-Michael Lux
  */
case object AsteriskForm extends RequestTarget {
  val value: String = "*"
}

final case class AbsoluteForm private (
    scheme: String,
    part: String,
    query: String
) extends RequestTarget

object AbsoluteForm {
  def fromString(requestTarget: String): Either[RequestTargetError, AbsoluteForm] = ???
}

/** Smart constructor for [[RequestTarget]].
  *
  * @author
  *   Denis-Michael Lux
  */
object RequestTarget {

  /** Creates a [[RequestTarget]] from a raw request target.
    *
    * Only the origin form is supported, so this delegates to [[OriginForm.fromString]].
    *
    * @param method
    *   the method used to decide which form to parse
    * @param rawRequestTarget
    *   the non-null raw request target
    * @return
    *   a `Right` containing the validated target, or a `Left` containing
    *   [[RequestTargetError.MalformedOriginForm]] if the target is not in a supported form
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

/** errors that occur during request target validation
  *
  * @author
  *   Denis-Michael Lux
  */
enum RequestTargetValidationError {
  case EmptyTarget
  case MissingLeadingSlash
  case InvalidPathCharacters
  case InvalidQueryCharacters
  case EmptyHost
  case InvalidUriHostCharacters
  case EmptyPort
  case InvalidPortCharacters
  case PortOutOfRange
  case MissingColon
}

/** Syntactic validation rules for the components of a request target.
  *
  * The rules follow RFC 9112, section 3.2, which builds on the URI syntax of RFC 3986, sections 3.3
  * and 3.4.
  *
  * @author
  *   Denis-Michael Lux
  */
private[http] object RequestTargetValidator {

  /** Tests the characters of a path against `1*( "/" segment )` with `segment = *pchar`.
    *
    * The rule is the `absolute-path` rule from RFC 9110, section 4.1, built on RFC 3986, section
    * 3.3. This validator checks only the permitted characters. That the path starts with `/` is
    * checked by [[validatePathStartWithSlash]].
    */
  private val isValidPath = isEncodedSequenceOf(c => c == '/' || isPChar(c))

  /** Tests a query against `query = *( pchar / "/" / "?" )` from RFC 3986, section 3.4. */
  private val isValidQuery = isEncodedSequenceOf(c => c == '/' || c == '?' || isPChar(c))

  private val isRegularName = isEncodedSequenceOf(c => isUnreserved(c) || isSubDelimiters(c))

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

  def validateUriHost(rawUriHost: String): Either[RequestTargetValidationError, Unit] =
    if rawUriHost.isEmpty then Left(EmptyHost)
    else if !isRegularName(rawUriHost) then Left(InvalidUriHostCharacters)
    else Right(())

  def validatePort(rawPort: String): Either[RequestTargetValidationError, Unit] =
    if rawPort.isEmpty then Left(EmptyPort)
    else if !rawPort.forall(isDigit) then Left(InvalidPortCharacters)
    else Right(())

  def validatePortRange(port: Int): Either[RequestTargetValidationError, Unit] =
    if port < 0 || port > 65535 then Left(PortOutOfRange)
    else Right(())
}
