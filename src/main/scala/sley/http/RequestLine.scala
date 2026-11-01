package sley.http

/** A data container holding the separate positions of the HTTP request line according to RFC 9112
  * section 3.
  *
  * @param method
  *   The http method of the request
  * @param requestTarget
  *   The raw request target, including any query component
  * @param version
  *   The protocol version e.g. "HTTP/1.1"
  * @author
  *   Denis-Michael Lux
  */
case class RequestLine(method: String, requestTarget: String, version: String)

/** An error describing parser errors during HTTP request parsing
  *
  * @author
  *   Denis-Michael Lux
  */
enum RequestLineError:
  case MalformedRequestLine(message: String)
