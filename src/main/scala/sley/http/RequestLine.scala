package sley.http

/** Request-line components with a validated method name and raw target and version strings.
  *
  * Construction does not validate the request target or version, or enforce their consistency with
  * the method. This type alone does not guarantee a fully valid HTTP request line.
  *
  * @param method
  *   the syntactically validated, case-sensitive HTTP method
  * @param requestTarget
  *   the raw request target, including any query component
  * @param version
  *   the raw protocol version, for example `HTTP/1.1`
  * @author
  *   Denis-Michael Lux
  */
case class RequestLine(method: HttpMethod, requestTarget: String, version: String)

/** Errors encountered while validating request-line structure and method syntax.
  *
  * @author
  *   Denis-Michael Lux
  */
enum RequestLineError {

  /** The line contains ASCII control characters or violates the required component structure. */
  case MalformedRequestLine(message: String)

  /** The method name violates HTTP token syntax; this does not indicate server support. */
  case InvalidMethod(error: HttpMethodError)
}
