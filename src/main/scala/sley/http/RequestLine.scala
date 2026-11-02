package sley.http

/** The components of an HTTP/1.1 request line as defined in RFC 9112, section 3.
  *
  * The method and the request target are syntactically validated domain types. The protocol version
  * is still the raw string from the request line and is not validated yet.
  *
  * A `RequestLine` does not check whether its components fit together, for example whether the
  * request-target form is allowed for the method. An instance therefore does not guarantee a fully
  * valid request line.
  *
  * @param method
  *   the syntactically valid, case-sensitive HTTP method
  * @param requestTarget
  *   the syntactically valid request target
  * @param version
  *   the raw, unvalidated protocol version, for example `HTTP/1.1`
  * @author
  *   Denis-Michael Lux
  */
case class RequestLine(method: HttpMethod, requestTarget: RequestTarget, version: String)

/** Syntax errors detected while parsing a request line.
  *
  * Each case identifies the part of the request line that is invalid. The cases describe protocol
  * syntax only and do not prescribe an HTTP status code. Mapping errors to responses is the
  * responsibility of a later layer.
  *
  * @author
  *   Denis-Michael Lux
  */
enum RequestLineError {

  /** The line contains an ASCII control character, or it does not consist of exactly three
    * non-empty components separated by single spaces.
    */
  case MalformedRequestLine(message: String)

  /** The method violates the HTTP token syntax. This case does not indicate server support. */
  case InvalidMethod(error: HttpMethodError)

  /** The request target violates the syntax of every supported request-target form. */
  case InvalidRequestTarget(error: RequestTargetError)
}
