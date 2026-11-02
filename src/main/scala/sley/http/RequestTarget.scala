package sley.http

import sley.http.RequestTargetValidator.{
  validatePathCharacters,
  validatePathStartWithSlash,
  validateQueryCharacters,
  validateTargetNotEmpty
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
    *   [[RequestTargetError.MalformedRequestTarget]] if the target is not a valid origin form
    */
  def fromString(rawRequestTarget: String): Either[RequestTargetError, OriginForm] =
    for {
      _ <- validateTargetNotEmpty(rawRequestTarget)
      (rawPath, rawQuery) = separateIntoPathAndQuery(rawRequestTarget)

      _ <- validatePathStartWithSlash(rawPath)
      _ <- validatePathCharacters(rawPath)

      _ <- rawQuery.fold[Either[RequestTargetError, Unit]](Right(()))(
        validateQueryCharacters
      )
    } yield OriginForm(rawPath, rawQuery)
}

/** Syntax errors detected while creating a [[RequestTarget]].
  *
  * @author
  *   Denis-Michael Lux
  */
enum RequestTargetError {

  /** The target is empty, is not in a supported form, or contains a character or percent-encoding
    * that is not permitted. The message describes the violated rule for diagnostics.
    */
  case MalformedRequestTarget(message: String)
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
    * @param rawRequestTarget
    *   the non-null raw request target
    * @return
    *   a `Right` containing the validated target, or a `Left` containing
    *   [[RequestTargetError.MalformedRequestTarget]] if the target is not in a supported form
    */
  def fromString(rawRequestTarget: String): Either[RequestTargetError, RequestTarget] =
    OriginForm.fromString(rawRequestTarget)
}
