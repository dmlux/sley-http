package sley.http.syntax

import sley.http.syntax.CoreRules
import sley.http.syntax.CoreRules.isHexDigit

import scala.annotation.tailrec
import scala.collection.immutable.HashSet

/** Character rules of the generic URI syntax defined in RFC 3986.
  *
  * The rules build on the ABNF core rules in [[CoreRules]]. Percent-encodings span three characters
  * and are therefore validated by [[isEncodedSequenceOf]] instead of a character predicate.
  *
  * See [[https://www.rfc-editor.org/info/rfc3986/ RFC 3986]].
  *
  * @author
  *   Denis-Michael Lux
  */
private[http] object UriSyntax {

  /** Punctuation permitted by the `unreserved` rule. */
  private val unreservedCharacters = HashSet('-', '.', '_', '~')

  /** Tests the `unreserved` rule from RFC 3986, section 2.3.
    *
    * The rule is defined as `unreserved = ALPHA / DIGIT / "-" / "." / "_" / "~"`.
    *
    * @param char
    *   the character to test
    * @return
    *   `true` if the character is unreserved, otherwise `false`
    */
  def isUnreserved(char: Char): Boolean = char match
    case c if CoreRules.isAlpha(c)                => true
    case c if CoreRules.isDigit(c)                => true
    case c if unreservedCharacters.contains(char) => true
    case _                                        => false

  /** Characters permitted by the `sub-delims` rule. */
  private val subDelimitersCharacters =
    HashSet('!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=')

  /** Tests the `sub-delims` rule from RFC 3986, section 2.2.
    *
    * The rule is defined as follows:
    * {{{
    * sub-delims = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
    * }}}
    *
    * @param char
    *   the character to test
    * @return
    *   `true` if the character is a sub-delimiter, otherwise `false`
    */
  def isSubDelimiters(char: Char): Boolean = char match
    case c if subDelimitersCharacters.contains(char) => true
    case _                                           => false

  /** Tests the single-character alternatives of the `pchar` rule from RFC 3986, section 3.3.
    *
    * The rule is defined as `pchar = unreserved / pct-encoded / sub-delims / ":" / "@"`. This
    * predicate covers every alternative except `pct-encoded`, which [[isEncodedSequenceOf]]
    * validates.
    *
    * @param char
    *   the character to test
    * @return
    *   `true` if the character is permitted unencoded in a path segment, otherwise `false`
    */
  def isPChar(char: Char): Boolean = char match
    case c if isUnreserved(c)    => true
    case c if isSubDelimiters(c) => true
    case ':' | '@'               => true
    case _                       => false

  /** Tests whether a sequence consists only of permitted characters and percent-encodings.
    *
    * The sequence must match `*( allowed / pct-encoded )`, where `allowed` is given by `predicate`
    * and `pct-encoded = "%" HEXDIG HEXDIG` is defined in RFC 3986, section 2.1. Every `%` starts a
    * percent-encoding and must be followed by exactly two hexadecimal digits. A sequence without
    * any `%` is valid if every character satisfies `predicate`. Percent-encodings are validated,
    * never decoded.
    *
    * A small state machine walks the sequence once from left to right and stops at the first
    * invalid character.
    *
    * Applying only the first parameter list yields a reusable validator:
    * {{{
    * val isValidPath = isEncodedSequenceOf(char => char == '/' || isPChar(char))
    * isValidPath("/test%20path/subroute") // true
    * }}}
    *
    * @param predicate
    *   the predicate for characters permitted unencoded. It is never applied to `%`.
    * @param sequence
    *   the non-null character sequence to test
    * @return
    *   `true` if the sequence is valid, otherwise `false`
    */
  def isEncodedSequenceOf(predicate: Char => Boolean)(sequence: String): Boolean = {
    enum State { case Normal, FirstHexDigit, SecondHexDigit }

    @tailrec
    def next(i: Int, state: State): Boolean = {
      if i >= sequence.length && state == State.Normal then true
      else if i >= sequence.length then false
      else
        (sequence.charAt(i), state) match
          case ('%', State.Normal)                              => next(i + 1, State.FirstHexDigit)
          case (char, State.Normal) if predicate(char)          => next(i + 1, State.Normal)
          case (char, State.FirstHexDigit) if isHexDigit(char)  => next(i + 1, State.SecondHexDigit)
          case (char, State.SecondHexDigit) if isHexDigit(char) => next(i + 1, State.Normal)
          case (_, _)                                           => false
    }

    next(0, State.Normal)
  }
}
