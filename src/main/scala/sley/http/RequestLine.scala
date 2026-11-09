package sley.http

import sley.http.syntax.CoreRules.isControl
import sley.http.RequestLineError.{
  InvalidRequestMethod,
  InvalidRequestTarget,
  InvalidRequestVersion,
  MalformedRequestLine
}

/** The components of an HTTP/1.1 request line as defined in RFC 9112, section 3.
  *
  * The method, the request target, and the protocol version are syntactically validated domain
  * types.
  *
  * Instances can only be created through [[RequestLine.fromString]]. The method selects the
  * request-target form, so the form always fits the method. For example, `CONNECT` always has an
  * [[AuthorityForm]], and only `OPTIONS` can have the [[AsteriskForm]]. Whether the server supports
  * the method, the target, or the version is not checked.
  *
  * @param method
  *   the syntactically valid, case-sensitive HTTP method
  * @param target
  *   the syntactically valid request target
  * @param version
  *   the syntactically valid protocol version, for example `HTTP/1.1`
  * @author
  *   Denis-Michael Lux
  */
final case class RequestLine private (
    method: RequestMethod,
    target: RequestTarget,
    version: RequestVersion
)

/** Syntax errors detected while parsing a request line.
  *
  * Each case identifies the part of the request line that is invalid. The cases describe protocol
  * syntax only and do not prescribe an HTTP status code. Mapping errors to responses is the
  * responsibility of a later layer.
  *
  * @author
  *   Denis-Michael Lux
  */
enum RequestLineError {

  /** The line contains an ASCII control character, or it does not consist of exactly three
    * non-empty components separated by single spaces.
    */
  case MalformedRequestLine(message: String)

  /** The method violates the HTTP token syntax. This case does not indicate server support. */
  case InvalidRequestMethod(error: RequestMethodError)

  /** The request target violates the syntax of every supported request-target form. */
  case InvalidRequestTarget(error: RequestTargetError)

  /** The protocol version violates the `HTTP-version` syntax. This case does not indicate server
    * support.
    */
  case InvalidRequestVersion(error: RequestVersionError)
}

/** Smart constructor for [[RequestLine]].
  *
  * Parsing is a pure function without network I/O. It works on the syntactic level only. It reports
  * which rule a request line violates but does not decide on an HTTP response.
  *
  * @author
  *   Denis-Michael Lux
  */
object RequestLine {

  /** Rejects ASCII control characters from 0x00 through 0x1F and 0x7F, including horizontal tab.
    *
    * @param requestLine
    *   the non-null line to check
    * @return
    *   `Right(())` if no ASCII control character is present, or a `Left` containing
    *   [[RequestLineError.MalformedRequestLine]] otherwise
    */
  private def validateNoControlCharacter(requestLine: String): Either[RequestLineError, Unit] =
    if requestLine.exists(isControl) then
      Left(MalformedRequestLine("Request line contains illegal control characters."))
    else Right(())

  /** Splits a line into three non-empty components separated by single ASCII spaces.
    *
    * Does not validate the contents of the individual components.
    *
    * @param requestLine
    *   the non-null line to split
    * @return
    *   a `Right` containing the raw method, request target, and version in that order, or a `Left`
    *   containing [[RequestLineError.MalformedRequestLine]] if the structure is invalid
    */
  private def splitLine(requestLine: String): Either[RequestLineError, (String, String, String)] =
    requestLine.split(" ", -1) match {
      case Array(method, target, version)
          if method.nonEmpty && target.nonEmpty && version.nonEmpty =>
        Right((method, target, version))

      case _ =>
        Left(
          MalformedRequestLine("Expected three non-empty components separated by single spaces.")
        )
    }

  /** Validates method syntax and translates method errors into request-line errors.
    *
    * @param rawMethod
    *   the non-null method name, preserving its original spelling
    * @return
    *   a `Right` containing the validated [[RequestMethod]], or a `Left` containing
    *   [[RequestLineError.InvalidRequestMethod]] if the method syntax is invalid
    */
  private def parseRequestMethod(rawMethod: String): Either[RequestLineError, RequestMethod] =
    RequestMethod
      .fromString(rawMethod)
      .left
      .map(InvalidRequestMethod.apply)

  /** Validates request-target syntax and translates request-target errors into request-line errors.
    *
    * @param method
    *   the HTTP method that selects the request-target form
    * @param rawRequestTarget
    *   the non-null request target, preserving its original spelling
    * @return
    *   a `Right` containing the validated [[RequestTarget]], or a `Left` containing
    *   [[RequestLineError.InvalidRequestTarget]] if the request-target syntax is invalid
    */
  private def parseRequestTarget(
      method: RequestMethod,
      rawRequestTarget: String
  ): Either[RequestLineError, RequestTarget] = RequestTarget
    .fromString(method, rawRequestTarget)
    .left
    .map(InvalidRequestTarget.apply)

  /** Validates protocol-version syntax and translates version errors into request-line errors.
    *
    * @param rawRequestVersion
    *   the non-null protocol version, preserving its original spelling
    * @return
    *   a `Right` containing the validated [[RequestVersion]], or a `Left` containing
    *   [[RequestLineError.InvalidRequestVersion]] if the version syntax is invalid
    */
  private def parseRequestVersion(
      rawRequestVersion: String
  ): Either[RequestLineError, RequestVersion] = RequestVersion
    .fromString(rawRequestVersion)
    .left
    .map(InvalidRequestVersion.apply)

  /** Parses a request line into its components.
    *
    * Applies these checks in order and returns the first error found:
    *   1. The line contains no ASCII control characters.
    *   1. The line consists of three non-empty components separated by single ASCII spaces.
    *   1. The method is a valid HTTP token, see [[RequestMethod.fromString]].
    *   1. The request target is valid in the form selected by the method, see
    *      [[RequestTarget.fromString]].
    *   1. The protocol version is valid, see [[RequestVersion.fromString]].
    *
    * All components keep their original spelling. Whether the server supports the method or the
    * version is not checked.
    *
    * @param requestLine
    *   a non-null, complete request line with its terminating CRLF already removed
    * @return
    *   a `Right` containing the [[RequestLine]], or a `Left` containing
    *   [[RequestLineError.MalformedRequestLine]] for control characters or an invalid structure,
    *   [[RequestLineError.InvalidRequestMethod]] for invalid method syntax, or
    *   [[RequestLineError.InvalidRequestTarget]] for invalid request-target syntax, or
    *   [[RequestLineError.InvalidRequestVersion]] for invalid version syntax
    */
  def fromString(requestLine: String): Either[RequestLineError, RequestLine] =
    for {
      _ <- validateNoControlCharacter(requestLine)
      (rawRequestMethod, rawRequestTarget, rawVersion) <- splitLine(requestLine)
      requestMethod <- parseRequestMethod(rawRequestMethod)
      requestTarget <- parseRequestTarget(requestMethod, rawRequestTarget)
      requestVersion <- parseRequestVersion(rawVersion)
    } yield RequestLine(requestMethod, requestTarget, requestVersion)
}
