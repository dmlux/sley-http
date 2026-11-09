package sley.http

import sley.http.syntax.CoreRules
import sley.http.syntax.CoreRules.{isAlpha, isDigit}

/** A syntactically valid URI scheme as defined in RFC 3986, section 3.1.
  *
  * The grammar is `scheme = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )`.
  *
  * Every instance satisfies these invariants:
  *   - `raw` is not empty and starts with an ASCII letter.
  *   - `raw` contains only ASCII letters, digits, `+`, `-`, and `.`.
  *
  * Instances can only be created through [[Scheme.fromString]]. The scheme keeps its original
  * spelling. RFC 3986 defines schemes as case-insensitive, but this type does not normalize the
  * case. `HTTP` and `http` are therefore not equal.
  *
  * @param raw
  *   the scheme without its trailing `:`, in its original spelling
  *
  * @author
  *   Denis-Michael Lux
  */
final case class Scheme private (raw: String)

/** Syntax errors detected while creating a [[Scheme]].
  *
  * @author
  *   Denis-Michael Lux
  */
enum SchemeError {

  /** The scheme is empty. */
  case Empty

  /** The scheme does not start with an ASCII letter or contains a character that is not permitted.
    */
  case InvalidCharacters
}

/** Smart constructor for [[Scheme]].
  *
  * @author
  *   Denis-Michael Lux
  */
object Scheme {
  import SchemeError.*

  /** Tests whether a character may follow the first letter of a scheme. */
  private def isSchemeCharacter(char: Char): Boolean =
    isAlpha(char) || isDigit(char) || char == '+' || char == '-' || char == '.'

  /** Validates that a scheme is not empty.
    *
    * @param rawScheme
    *   the non-null raw scheme
    * @return
    *   `Right(())` if the scheme is not empty, or a `Left` containing [[SchemeError.Empty]]
    *   otherwise
    */
  private def validateEmpty(rawScheme: String): Either[SchemeError, Unit] =
    if rawScheme.isEmpty then Left(Empty)
    else Right(())

  /** Validates the characters of a scheme.
    *
    * Requires a non-empty scheme. [[validateEmpty]] must run first.
    *
    * @param rawString
    *   the non-null, non-empty raw scheme
    * @return
    *   `Right(())` if the scheme is valid, or a `Left` containing [[SchemeError.InvalidCharacters]]
    *   otherwise
    */
  private def validateCharacters(rawString: String): Either[SchemeError, Unit] =
    if CoreRules.isAlpha(rawString.head) && rawString.drop(1).forall(isSchemeCharacter)
    then Right(())
    else Left(InvalidCharacters)

  /** Creates a [[Scheme]] from a raw scheme.
    *
    * Checks that the scheme is not empty and then its characters. Returns the first error found.
    *
    * @param rawScheme
    *   the non-null raw scheme without its trailing `:`
    * @return
    *   a `Right` containing the [[Scheme]], or a `Left` containing [[SchemeError]]
    */
  def fromString(rawScheme: String): Either[SchemeError, Scheme] =
    for {
      _ <- validateEmpty(rawScheme)
      _ <- validateCharacters(rawScheme)
    } yield Scheme(rawScheme)
}
