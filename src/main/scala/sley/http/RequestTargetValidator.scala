package sley.http

import sley.http.RequestTargetError

import scala.annotation.tailrec

/** Syntactic validation rules for the components of a request target.
  *
  * The rules follow RFC 9112, section 3.2, which builds on the URI syntax of RFC 3986, sections 3.3
  * and 3.4.
  *
  * @author
  *   Denis-Michael Lux
  */
private[http] object RequestTargetValidator {

  /** Punctuation permitted unencoded in a path.
    *
    * Contains the punctuation of the `unreserved` and `sub-delims` rules, `:` and `@` from the
    * `pchar` rule of RFC 3986, section 3.3, and the segment separator `/`.
    */
  private val allowedPathCharacters: Set[Char] = "-._~!$&'()*+,;=:@/".toSet

  /** Tests whether a character is permitted unencoded in a path.
    *
    * Percent-encodings are validated separately.
    *
    * @param char
    *   the character to test
    * @return
    *   `true` if the character is permitted, otherwise `false`
    */
  private def isValidPathCharacter(char: Char): Boolean = char match
    case c if '0' <= c && c <= '9'              => true
    case c if 'a' <= c && c <= 'z'              => true
    case c if 'A' <= c && c <= 'Z'              => true
    case c if allowedPathCharacters.contains(c) => true
    case _                                      => false

  /** Tests whether a character is an ASCII hexadecimal digit.
    *
    * @param char
    *   the character to test
    * @return
    *   `true` if the character is a hexadecimal digit, otherwise `false`
    */
  private def isValidHex(char: Char): Boolean = char match
    case c if '0' <= c && c <= '9' => true
    case c if 'a' <= c && c <= 'f' => true
    case c if 'A' <= c && c <= 'F' => true
    case _                         => false

  /** Validates every character of a request-target component.
    *
    * Each character must either satisfy `isValidCharacter` or belong to a percent-encoding, which
    * is a `%` followed by exactly two hexadecimal digits. A small state machine walks the component
    * once from left to right.
    *
    * @param rawComponentString
    *   the raw path or query
    * @param isValidCharacter
    *   the predicate for characters permitted unencoded in this component
    * @return
    *   `Right(())` if the component is valid, or a `Left` containing
    *   [[RequestTargetError.MalformedRequestTarget]] otherwise
    */
  private def validateCharacters(
      rawComponentString: String,
      isValidCharacter: Char => Boolean
  ): Either[RequestTargetError, Unit] = {
    enum State {
      case Normal, FirstHexDigit, SecondHexDigit
    }

    val error = RequestTargetError.MalformedRequestTarget(
      "Request target contains illegal characters."
    )

    @tailrec
    def checkCharacter(index: Int, state: State): Either[RequestTargetError, Unit] = {
      if index >= rawComponentString.length && state == State.Normal then Right(())
      else if index >= rawComponentString.length then Left(error)
      else
        (rawComponentString.charAt(index), state) match
          case ('%', State.Normal) =>
            checkCharacter(index + 1, State.FirstHexDigit)
          case (char, State.Normal) if isValidCharacter(char) =>
            checkCharacter(index + 1, State.Normal)
          case (char, State.FirstHexDigit) if isValidHex(char) =>
            checkCharacter(index + 1, State.SecondHexDigit)
          case (char, State.SecondHexDigit) if isValidHex(char) =>
            checkCharacter(index + 1, State.Normal)
          case (_, _) => Left(error)
    }

    checkCharacter(0, State.Normal)
  }

  /** Validates the characters and percent-encodings of a path.
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
    validateCharacters(rawOriginPath, isValidPathCharacter)

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
    validateCharacters(rawQuery, char => char == '?' || isValidPathCharacter(char))

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
    else Left(RequestTargetError.MalformedRequestTarget("Request target is empty."))

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
    else Left(RequestTargetError.MalformedRequestTarget("Request target must start with '/'."))
}
