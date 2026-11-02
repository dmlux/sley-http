package sley.http

import sley.http.RequestTargetError
import sley.http.RequestTargetError.MalformedRequestTarget
import sley.http.UriSyntax.{isEncodedSequenceOf, isPChar}

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
  private val isValidPath = isEncodedSequenceOf(char => char == '/' || isPChar(char))

  /** Tests a query against `query = *( pchar / "/" / "?" )` from RFC 3986, section 3.4. */
  private val isValidQuery =
    isEncodedSequenceOf(char => char == '/' || char == '?' || isPChar(char))

  /** Validates the characters and percent-encodings of a path.
    *
    * A path permits `pchar`, percent-encodings, and the segment separator `/`.
    *
    * @param rawOriginPath
    *   the raw path of an origin-form target
    * @return
    *   `Right(())` if the path is valid, or a `Left` containing
    *   [[RequestTargetError.MalformedRequestTarget]] otherwise
    */
  def validatePathCharacters(
      rawOriginPath: String
  ): Either[RequestTargetError, Unit] =
    if isValidPath(rawOriginPath) then Right(())
    else Left(MalformedRequestTarget("Request target contains illegal characters."))

  /** Validates the characters and percent-encodings of a query.
    *
    * A query permits the same characters as a path plus `?`, as defined in RFC 3986, section 3.4.
    *
    * @param rawQuery
    *   the raw query without its leading `?`
    * @return
    *   `Right(())` if the query is valid, or a `Left` containing
    *   [[RequestTargetError.MalformedRequestTarget]] otherwise
    */
  def validateQueryCharacters(
      rawQuery: String
  ): Either[RequestTargetError, Unit] =
    if isValidQuery(rawQuery) then Right(())
    else Left(MalformedRequestTarget("Request target contains illegal characters."))

  /** Validates that a request target is not empty.
    *
    * @param rawRequestTarget
    *   the raw request target
    * @return
    *   `Right(())` if the target is not empty, or a `Left` containing
    *   [[RequestTargetError.MalformedRequestTarget]] otherwise
    */
  def validateTargetNotEmpty(
      rawRequestTarget: String
  ): Either[RequestTargetError, Unit] =
    if rawRequestTarget.nonEmpty then Right(())
    else Left(MalformedRequestTarget("Request target is empty."))

  /** Validates that a path starts with `/`.
    *
    * @param rawOriginPath
    *   the raw path of an origin-form target
    * @return
    *   `Right(())` if the path starts with `/`, or a `Left` containing
    *   [[RequestTargetError.MalformedRequestTarget]] otherwise
    */
  def validatePathStartWithSlash(
      rawOriginPath: String
  ): Either[RequestTargetError, Unit] =
    if rawOriginPath.startsWith("/") then Right(())
    else Left(MalformedRequestTarget("Request target must start with '/'."))
}
