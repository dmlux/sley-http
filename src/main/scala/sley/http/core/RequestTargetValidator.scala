package sley.http.core

import sley.http.RequestTargetError

import scala.annotation.tailrec

/** A singleton object providing validation methods for the request target for the different target
  * forms.
  *
  * @author
  *   Denis-Michael Lux
  */
private[http] object RequestTargetValidator {

  /** A set of allowed characters of the request line path
    */
  private val allowedPathCharacters: Set[Char] = "-._~!$&'()*+,;=:@/".toSet

  /** Tests whether the given character is valid in the context of the path within a request line.
    * @param char
    *   the character to test
    * @return
    *   `true` if the character is valid or `false` otherwise
    */
  private def isValidPathCharacter(char: Char): Boolean = char match
    case c if '0' <= c && c <= '9'              => true
    case c if 'a' <= c && c <= 'z'              => true
    case c if 'A' <= c && c <= 'Z'              => true
    case c if allowedPathCharacters.contains(c) => true
    case _                                      => false

  /** Tests whether the given character is valid hex
    * @param char
    *   the character to test.
    * @return
    *   `true` if the character is a valid hex value or `false` otherwise
    */
  private def isValidHex(char: Char): Boolean = char match
    case c if '0' <= c && c <= '9' => true
    case c if 'a' <= c && c <= 'f' => true
    case c if 'A' <= c && c <= 'F' => true
    case _                         => false

  /** Validates if the given character is a valid request target character independent of the fact
    * if it is for the path or for the query part of the target. It uses a special validation
    * function for the character to match the context.
    *
    * @param rawComponentString
    *   the raw path of the target
    * @param isValidCharacter
    *   the validation function for the character based on the context this function is used
    * @return
    *   either nothing if the character is valid according to the validation function and
    *   '%'-encoding or a [[RequestTargetError]] otherwise.
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

  /** Validates each character in the path part of the request line. The Check ensures that each
    * character on its own is valid as well as the `%`-enconding in the path part also is
    * syntactically correct.
    *
    * @param rawOriginPath
    *   the raw path part of the request line.
    * @return
    *   either nothing if the path part is syntactically valid or a [[RequestTargetError]]
    *   otherwise.
    */
  def validatePathCharacters(
      rawOriginPath: String
  ): Either[RequestTargetError, Unit] =
    validateCharacters(rawOriginPath, isValidPathCharacter)

  /** Validates each character in the query part of the request line. The check ensures that each
    * character on its own is valid as well as the '%'-encoding in the query path also is
    * syntactically correct.
    *
    * @param rawQuery
    *   the raw query part of the request line.
    * @return
    *   either nothing if the query part is syntactically valid or a [[RequestTargetError]]
    *   otherwise.
    */
  def validateQueryCharacters(
      rawQuery: String
  ): Either[RequestTargetError, Unit] =
    validateCharacters(rawQuery, char => char == '?' || isValidPathCharacter(char))

  /** Validates whether the request target is not empty.
    * @param rawRequestTarget
    *   the raw request target value as a String.
    * @return
    *   Either nothing if the target is not empty or [[RequestTargetError]] otherwise.
    */
  def validateTargetNotEmpty(
      rawRequestTarget: String
  ): Either[RequestTargetError, Unit] =
    if rawRequestTarget.nonEmpty then Right(())
    else Left(RequestTargetError.MalformedRequestTarget("Request target is empty."))

  /** Validates whether the path part of the request target starts with a leading slash
    * @param rawOriginPath
    *   the raw request target as a String
    * @return
    *   Either nothing if the path starts with `/` or an [[RequestTargetError]] otherwise.
    */
  def validatePathStartWithSlash(
      rawOriginPath: String
  ): Either[RequestTargetError, Unit] =
    if rawOriginPath.startsWith("/") then Right(())
    else Left(RequestTargetError.MalformedRequestTarget("Request target must start with '/'."))
}
