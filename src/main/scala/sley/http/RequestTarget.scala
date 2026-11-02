package sley.http

import sley.http.core.RequestTargetValidator.{
  validatePathCharacters,
  validateTargetNotEmpty,
  validatePathStartWithSlash,
  validateQueryCharacters
}

/** A validated instance of a request target
  *
  * @author
  *   Denis-Michael Lux
  */
sealed trait RequestTarget

/** The origin form of the request target
  * @param path
  *   The path component of the request target. It is always present
  * @param query
  *   The query component of the request target. It is not always present
  *
  * @author
  *   Denis-Michael Lux
  */
final class OriginForm private (
    val path: String,
    val query: Option[String]
) extends RequestTarget {
  override def toString: String = s"OriginForm($path, $query)"
}

/** A companion object for [[OriginForm]] that encapsulates parsing and validation functions.
  *
  * @author
  *   Denis-Michael Lux
  */
object OriginForm {

  /** Splits the given request target into two parts, the first part always contains the path part
    * of the line and the second value is an optional query part of the request target.
    * @param rawRequestTarget
    *   the raw String value of the request target
    * @return
    */
  private def splitIntoPathAndQuery(rawRequestTarget: String): (String, Option[String]) = {
    val splitIndex = rawRequestTarget.indexOf('?')

    if splitIndex == -1 then (rawRequestTarget, None)
    else
      val (path, queryWithQuestionMark) = rawRequestTarget.splitAt(splitIndex)
      (path, Some(queryWithQuestionMark.drop(1)))
  }

  /** Creates a [[RequestTarget]] instance for the given [[rawRequestTarget]].
    * @param rawRequestTarget
    *   the raw request target String that should be converted
    * @return
    *   either the [[RequestTarget]] instance containing a valid request line or a
    *   [[RequestTargetError]] otherwise.
    */
  def fromString(rawRequestTarget: String): Either[RequestTargetError, OriginForm] =
    for {
      _ <- validateTargetNotEmpty(rawRequestTarget)
      (rawPath, rawQuery) = splitIntoPathAndQuery(rawRequestTarget)

      _ <- validatePathStartWithSlash(rawPath)
      _ <- validatePathCharacters(rawPath)

      _ <- rawQuery.fold[Either[RequestTargetError, Unit]](Right(()))(
        validateQueryCharacters
      )
    } yield OriginForm(rawPath, rawQuery)
}

/** An error occurred during parsing of the request target component
  *
  * @author
  *   Denis-Michael Lux
  */
enum RequestTargetError {
  case MalformedRequestTarget(message: String)
}

/** the companion object for the ADT [[RequestTarget]]. This companion object converts raw values
  * into a [[RequestTarget]] instance and also validates its parts syntactically.
  *
  * @author
  *   Denis-Michael Lux
  */
object RequestTarget {
  def fromString(rawRequestTarget: String): Either[RequestTargetError, RequestTarget] =
    OriginForm.fromString(rawRequestTarget)
}
