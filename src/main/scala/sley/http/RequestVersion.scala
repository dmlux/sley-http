package sley.http

import sley.http.syntax.CoreRules.isDigit

/** A syntactically valid HTTP version as defined in RFC 9112, section 2.3.
  *
  * The grammar is `HTTP-version = HTTP-name "/" DIGIT "." DIGIT` with `HTTP-name = %s"HTTP"`. The
  * name is case-sensitive.
  *
  * Every instance satisfies these invariants:
  *   - `rawName` is exactly `HTTP`.
  *   - `rawVersion` is one ASCII digit, a `.`, and one ASCII digit.
  *
  * Instances can only be created through [[RequestVersion.fromString]]. Syntactic validity does not
  * imply that a server supports the version, for example `HTTP/9.9`.
  *
  * @param rawName
  *   the protocol name, always `HTTP`
  * @param rawVersion
  *   the major and minor version separated by `.`, for example `1.1`
  *
  * @author
  *   Denis-Michael Lux
  */
final case class RequestVersion private (rawName: String, rawVersion: String)

/** Syntax errors detected while creating a [[RequestVersion]].
  *
  * @author
  *   Denis-Michael Lux
  */
enum RequestVersionError {

  /** The version is empty. */
  case Empty

  /** The version does not match `HTTP-name "/" DIGIT "." DIGIT`. */
  case InvalidFormat
}

/** Smart constructor for [[RequestVersion]].
  *
  * @author
  *   Denis-Michael Lux
  */
object RequestVersion {
  import RequestVersionError.*

  /** Creates a [[RequestVersion]] from a raw version.
    *
    * Accepts exactly `HTTP/` followed by one digit, `.`, and one digit. Lowercase names and
    * multi-digit versions such as `HTTP/1.10` are rejected.
    *
    * @param rawVersion
    *   the non-null raw version, for example `HTTP/1.1`
    * @return
    *   a `Right` containing the [[RequestVersion]], or a `Left` containing
    *   [[RequestVersionError.Empty]] if the version is empty, or
    *   [[RequestVersionError.InvalidFormat]] otherwise
    */
  def fromString(rawVersion: String): Either[RequestVersionError, RequestVersion] =
    rawVersion match {
      case "" => Left(Empty)

      case s"HTTP/$major.$minor"
          if major.length == 1 && minor.length == 1 && major
            .forall(isDigit) && minor.forall(isDigit) =>
        Right(RequestVersion("HTTP", s"$major.$minor"))

      case _ => Left(InvalidFormat)
    }
}
