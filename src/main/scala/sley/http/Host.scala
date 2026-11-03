package sley.http

import sley.http.HostError.*
import sley.http.syntax.IpSyntax.isDecOctet
import sley.http.syntax.UriSyntax.{isEncodedSequenceOf, isSubDelimiters, isUnreserved}

import scala.annotation.tailrec

/** A syntactically valid host as defined in RFC 3986, section 3.2.2.
  *
  * The grammar is `host = IP-literal / IPv4address / reg-name`. Every IPv4 address also matches
  * `reg-name`. RFC 3986 resolves this ambiguity by treating a host that matches `IPv4address` as an
  * IPv4 address. A host that only looks similar, such as `256.1.1.1` or `01.2.3.4`, is a registered
  * name.
  *
  * Every form keeps the original spelling of the host. Hosts are case-insensitive, but this type
  * does not normalize them.
  *
  * @author
  *   Denis-Michael Lux
  */
sealed trait Host

/** Syntax errors detected while creating a [[Host]] or one of its forms.
  *
  * @author
  *   Denis-Michael Lux
  */
enum HostError {

  /** The host is empty. */
  case Empty

  case UnterminatedIpLiteral

  /** An IPv4 address violates the `IPv4address` rule. `cause` names the violated rule. */
  case MalformedIPv4Address(cause: IPv4AddressError)

  case MalformedIPv6Address(cause: IPv6AddressError)

  case MalformedIPvFutureAddress(cause: IPvFutureError)

  /** The host is not an IPv4 address and violates the `reg-name` rule. `cause` names the violated
    * rule.
    */
  case MalformedRegName(cause: RegNameError)

  case Invalid
}

/** A registered name, usually a DNS name, as defined in RFC 3986, section 3.2.2.
  *
  * The rule is `reg-name = *( unreserved / pct-encoded / sub-delims )`. Instances are created
  * through [[RegName.fromString]].
  *
  * @param raw
  *   the registered name in its original spelling, with percent-encodings not decoded
  *
  * @author
  *   Denis-Michael Lux
  */
final case class RegName private (raw: String) extends Host

/** Syntax errors detected while creating a [[RegName]].
  *
  * @author
  *   Denis-Michael Lux
  */
enum RegNameError {

  /** The name is empty. */
  case Empty

  /** The name contains a character or percent-encoding that `reg-name` does not permit. */
  case InvalidCharacters
}

/** Smart constructor for [[RegName]].
  *
  * @author
  *   Denis-Michael Lux
  */
object RegName {

  /** Creates a [[RegName]] from a raw host.
    *
    * The ABNF permits an empty `reg-name`, but an empty name is rejected because a host needs a
    * destination. The name keeps its original spelling.
    *
    * @param rawHost
    *   the non-null raw host
    * @return
    *   a `Right` containing the registered name, or a `Left` containing [[RegNameError.Empty]] or
    *   [[RegNameError.InvalidCharacters]]
    */
  def fromString(rawHost: String): Either[RegNameError, RegName] =
    if rawHost.isEmpty then Left(RegNameError.Empty)
    else if !isEncodedSequenceOf(c => isUnreserved(c) || isSubDelimiters(c))(rawHost) then
      Left(RegNameError.InvalidCharacters)
    else Right(RegName(rawHost))
}

/** An IPv4 address as defined by the `IPv4address` rule of RFC 3986, section 3.2.2.
  *
  * The rule is defined as follows:
  * {{{
  * IPv4address = dec-octet "." dec-octet "." dec-octet "." dec-octet
  * }}}
  *
  * Each octet is a decimal number from 0 through 255 without leading zeros. Instances can only be
  * created through [[IPv4Address.fromString]].
  *
  * @param raw
  *   the address in dotted-decimal notation, exactly as received
  *
  * @author
  *   Denis-Michael Lux
  */
final case class IPv4Address private (raw: String) extends Host

/** Syntax errors detected while creating an [[IPv4Address]].
  *
  * @author
  *   Denis-Michael Lux
  */
enum IPv4AddressError {

  /** The address is empty. */
  case Empty

  /** The address is longer than 15 characters, the length of `255.255.255.255`. */
  case AddressTooLong

  /** The address is shorter than 7 characters, the length of `0.0.0.0`. */
  case AddressTooShort

  /** The address contains more than three dots and therefore more than four octets. */
  case TooManyOctets

  /** The address contains fewer than three dots and therefore fewer than four octets. */
  case TooFewOctets

  /** An octet is empty, has a leading zero, is greater than 255, or contains a character other than
    * an ASCII digit.
    */
  case InvalidOctet
}

/** Smart constructor for [[IPv4Address]].
  *
  * @author
  *   Denis-Michael Lux
  */
object IPv4Address {

  /** Rejects addresses whose length rules out a valid IPv4 address.
    *
    * A valid address has between 7 and 15 characters, from `0.0.0.0` to `255.255.255.255`. The
    * check also bounds the work of the following octet validation.
    *
    * @param rawHost
    *   the non-null raw address
    * @return
    *   `Right(())` if the length is plausible, or a `Left` containing [[IPv4AddressError.Empty]],
    *   [[IPv4AddressError.AddressTooShort]], or [[IPv4AddressError.AddressTooLong]] otherwise
    */
  private def validateLength(rawHost: String): Either[IPv4AddressError, Unit] = rawHost.length match
    case 0           => Left(IPv4AddressError.Empty)
    case l if l < 7  => Left(IPv4AddressError.AddressTooShort)
    case l if l > 15 => Left(IPv4AddressError.AddressTooLong)
    case _           => Right(())

  /** Validates that an address consists of exactly four `dec-octet` values separated by dots.
    *
    * Walks the address once from left to right and collects the characters of the current octet.
    * Each dot and the end of the input complete an octet, which must then match `dec-octet`.
    *
    * @param rawHost
    *   the non-null raw address
    * @return
    *   `Right(())` if the address has four valid octets, or a `Left` containing
    *   [[IPv4AddressError.TooFewOctets]], [[IPv4AddressError.TooManyOctets]], or
    *   [[IPv4AddressError.InvalidOctet]] otherwise
    */
  private def validateGroups(
      rawHost: String
  ): Either[IPv4AddressError, Unit] = {
    @tailrec
    def next(
        remaining: List[Char],
        octet: List[Char],
        count: Int
    ): Either[IPv4AddressError, Unit] = {
      remaining match
        case Nil if count == 3 && isDecOctet(octet.reverse.mkString) => Right(())
        case Nil if count < 3                                => Left(IPv4AddressError.TooFewOctets)
        case Nil                                             => Left(IPv4AddressError.InvalidOctet)
        case '.' :: _ if !isDecOctet(octet.reverse.mkString) =>
          Left(IPv4AddressError.InvalidOctet)
        case '.' :: _ if count == 3 => Left(IPv4AddressError.TooManyOctets)
        case '.' :: tail            => next(tail, Nil, count + 1)
        case c :: tail              => next(tail, c :: octet, count)
    }

    next(rawHost.toList, Nil, 0)
  }

  /** Creates an [[IPv4Address]] from its dotted-decimal representation.
    *
    * Checks the length first and then the octets. Returns the first error found. The address keeps
    * its original spelling.
    *
    * @param rawAddress
    *   the non-null raw address
    * @return
    *   a `Right` containing the address, or a `Left` containing the [[IPv4AddressError]] of the
    *   first violated rule
    */
  def fromString(rawAddress: String): Either[IPv4AddressError, IPv4Address] = for {
    _ <- validateLength(rawAddress)
    _ <- validateGroups(rawAddress)
  } yield IPv4Address(rawAddress)
}

/** An IPv6 address inside an IP literal, as defined in RFC 3986, section 3.2.2.
  *
  * The address may compress groups of zeros with `::` and may end with an embedded IPv4 address.
  * There is no smart constructor yet, so instances cannot be created.
  *
  * @param raw
  *   the address in its original spelling
  *
  * @author
  *   Denis-Michael Lux
  */
final case class IPv6Address private (raw: String) extends Host

enum IPv6AddressError {
  case Empty
  case EmptyGroup
  case TooFewGroups
  case TooManyGroups
  case DoublePlaceholder
  case TooLongGroup
  case InvalidGroup
  case InvalidIPv4Group(cause: IPv4AddressError)
}

object IPv6Address {
  private def validateEmpty(rawHost: String): Either[IPv6AddressError, Unit] =
    if rawHost.isEmpty then Left(IPv6AddressError.Empty)
    else Right(())

  def fromString(rawHost: String): Either[IPv6AddressError, IPv6Address] = for {
    _ <- validateEmpty(rawHost)
  } yield IPv6Address(rawHost)
}

/** An IP literal in a future address format, as defined in RFC 3986, section 3.2.2.
  *
  * The rule is `IPvFuture = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )`. The leading `v`
  * is case-insensitive. There is no smart constructor yet, so instances cannot be created.
  *
  * @param raw
  *   the literal in its original spelling
  *
  * @author
  *   Denis-Michael Lux
  */
final case class IPvFuture private (raw: String) extends Host

enum IPvFutureError {
  case Invalid
}

object IPvFuture {
  def fromString(rawHost: String): Either[IPvFutureError, IPvFuture] = ???
}

/** Smart constructor for [[Host]].
  *
  * @author
  *   Denis-Michael Lux
  */
object Host {

  /** Creates a [[Host]] from its raw representation.
    *
    * Follows RFC 3986, section 3.2.2: a host that matches `IPv4address` is an [[IPv4Address]], and
    * every other host is a [[RegName]]. A host that fails the IPv4 check is therefore not an error.
    *
    * IP literals in square brackets are not recognized yet. They fail as a [[RegName]], because
    * `reg-name` does not permit `[` and `]`.
    *
    * @param rawHost
    *   the non-null raw host
    * @return
    *   a `Right` containing the classified host, or a `Left` containing the [[HostError]] of the
    *   first violated rule
    */
  def fromString(rawHost: String): Either[HostError, Host] = {
    rawHost match {
      case host if host.isEmpty => Left(Empty)

      case host if host.startsWith("[") && !host.endsWith("]") => Left(UnterminatedIpLiteral)

      case host if host.startsWith("[v") || host.startsWith("[V") =>
        IPvFuture
          .fromString(host.substring(1, host.length - 1))
          .left
          .map(MalformedIPvFutureAddress.apply)

      case host if host.startsWith("[") =>
        IPv6Address
          .fromString(host.substring(1, host.length - 1))
          .left
          .map(MalformedIPv6Address.apply)

      case host =>
        IPv4Address
          .fromString(host)
          .fold(
            _ => RegName.fromString(host).left.map(MalformedRegName.apply),
            address => Right(address)
          )
    }
  }
}
