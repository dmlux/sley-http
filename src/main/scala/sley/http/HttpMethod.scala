package sley.http

import sley.http.HttpMethodError.InvalidMethodSyntax
import sley.http.HttpSyntax.isTChar

/** A syntactically valid HTTP method name.
  *
  * `HttpMethod` is a value object. Its invariant is that `value` is a non-empty HTTP token as
  * defined in RFC 9110, sections 5.6.2 and 9.1. Instances can only be created through
  * [[HttpMethod.fromString]], so every instance satisfies this invariant.
  *
  * Method names are case-sensitive and keep their original spelling. Syntactic validity does not
  * imply that a server recognizes or supports the method.
  *
  * @param value
  *   the validated method name, preserving its original spelling
  * @author
  *   Denis-Michael Lux
  */
final case class HttpMethod private (value: String)

/** Syntax errors detected while creating an [[HttpMethod]].
  *
  * @author
  *   Denis-Michael Lux
  */
enum HttpMethodError {

  /** The name is empty or contains a character that is not permitted in an HTTP token. */
  case InvalidMethodSyntax
}

/** Smart constructor and extractor for [[HttpMethod]].
  *
  * @author
  *   Denis-Michael Lux
  */
object HttpMethod {

  /** Creates a method from a non-empty sequence of HTTP token characters.
    *
    * Accepts ASCII letters, digits, and the punctuation allowed by the `tchar` rule. Preserves case
    * and performs no trimming or normalization. Unknown method names are accepted if their syntax
    * is valid. Whether a server supports the method is decided elsewhere.
    *
    * @param value
    *   the non-null method name to validate
    * @return
    *   a `Right` containing the validated method, or a `Left` containing
    *   [[HttpMethodError.InvalidMethodSyntax]] if the name is empty or contains invalid characters
    */
  def fromString(value: String): Either[HttpMethodError, HttpMethod] =
    if value.nonEmpty && value.forall(isTChar) then Right(HttpMethod(value))
    else Left(InvalidMethodSyntax)
}
