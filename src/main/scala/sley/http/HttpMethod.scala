package sley.http

/** A syntactically valid, case-sensitive HTTP method name.
  *
  * Instances are created through [[HttpMethod.fromString]]. The name is a non-empty HTTP token
  * as defined in RFC 9110, sections 5.6.2 and 9.1. Syntactic validity does not imply that a server
  * recognizes or supports the method.
  *
  * @param value
  *   the validated method name, preserving its original spelling
  *
  * @author
  *   Denis-Michael Lux
  */
final class HttpMethod private (val value: String) {

  /** Compares method names using case-sensitive value equality.
    *
    * @param obj
    *   the object to compare with this instance
    * @return
    *   `true` if the object is an [[HttpMethod]] with the same name, otherwise `false`
    */
  override def equals(obj: Any): Boolean = obj match
    case other: HttpMethod => this.value == other.value
    case _                 => false

  /** Returns the method name's hash code, consistent with value equality. */
  override def hashCode(): Int = value.hashCode

  /** Returns a diagnostic representation such as `HttpMethod(GET)`, not the wire representation. */
  override def toString: String = s"HttpMethod($value)"
}

/** Syntax errors encountered when creating an [[HttpMethod]].
  *
  * @author
  *   Denis-Michael Lux
  */
enum HttpMethodError:
  /** The name is empty or contains a character that is not permitted in an HTTP token. */
  case InvalidMethodSyntax

/** Validates HTTP method names and provides an extractor for existing [[HttpMethod]] instances.
  *
  * @author
  *   Denis-Michael Lux
  */
object HttpMethod {
  private val specialCharacters: Set[Char] = "!#$%&'*+-.^_`|~".toSet

  /** Extracts the method name for pattern matching without creating a new instance.
    *
    * @param method
    *   a non-null method instance
    * @return
    *   `Some` containing the unchanged method name
    */
  def unapply(method: HttpMethod): Option[String] = Some(method.value)

  /** Tests the ASCII `tchar` rule from RFC 9110, section 5.6.2. */
  private def isTokenCharacter(char: Char): Boolean =
    (char >= 'a' && char <= 'z') ||
      (char >= 'A' && char <= 'Z') ||
      (char >= '0' && char <= '9') ||
      specialCharacters.contains(char)

  /** Creates a method from a non-empty sequence of HTTP token characters.
    *
    * Accepts ASCII letters, digits, and the punctuation allowed by the HTTP `tchar` rule.
    * Preserves case and performs no trimming or normalization. Unknown method names are accepted
    * if their syntax is valid; server support is checked separately.
    *
    * @param value
    *   the non-null method name to validate
    * @return
    *   a `Right` containing the validated method, or a `Left` containing
    *   [[HttpMethodError.InvalidMethodSyntax]] if the name is empty or contains invalid characters
    */
  def fromString(value: String): Either[HttpMethodError, HttpMethod] = {
    if value.nonEmpty && value.forall(isTokenCharacter) then Right(HttpMethod(value))
    else Left(HttpMethodError.InvalidMethodSyntax)
  }

}
