package sley.http

import sley.http.syntax.CoreRules.isHexDigit
import sley.http.syntax.IpSyntax.{isDecOctet, isH16}
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
  import RegNameError.*

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
    if rawHost.isEmpty then Left(Empty)
    else if !isEncodedSequenceOf(c => isUnreserved(c) || isSubDelimiters(c))(rawHost) then
      Left(InvalidCharacters)
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
  import IPv4AddressError.*

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
  private def validateLength(rawHost: String): Either[IPv4AddressError, Unit] =
    rawHost.length match
      case 0           => Left(Empty)
      case l if l < 7  => Left(AddressTooShort)
      case l if l > 15 => Left(AddressTooLong)
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
        case Nil if count < 3                                        => Left(TooFewOctets)
        case Nil                                                     => Left(InvalidOctet)
        case '.' :: _ if !isDecOctet(octet.reverse.mkString)         => Left(InvalidOctet)
        case '.' :: _ if count == 3                                  => Left(TooManyOctets)
        case '.' :: tail                                             => next(tail, Nil, count + 1)
        case c :: tail => next(tail, c :: octet, count)
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
  * The address consists of eight groups of one to four hexadecimal digits, separated by `:`. A
  * single `::` may replace one or more groups of zeros. The last two groups may be written as an
  * embedded IPv4 address, such as in `::ffff:192.0.2.1`.
  *
  * The square brackets belong to the `IP-literal` rule of the host, not to the address. Instances
  * can only be created through [[IPv6Address.fromString]].
  *
  * @param raw
  *   the address without square brackets, in its original spelling
  *
  * @author
  *   Denis-Michael Lux
  */
final case class IPv6Address private (raw: String) extends Host

/** Syntax errors detected while creating an [[IPv6Address]].
  *
  * @author
  *   Denis-Michael Lux
  */
enum IPv6AddressError {

  /** The address is empty. */
  case Empty

  /** A group between two separators is missing, for example because of a single `:` at the start or
    * end, or because of `:::`.
    */
  case EmptyGroup

  /** The address has fewer than eight groups and does not contain `::`. An embedded IPv4 address
    * counts as two groups.
    */
  case TooFewGroups

  /** The address has more than eight groups, or eight or more groups in addition to `::`. An
    * embedded IPv4 address counts as two groups.
    */
  case TooManyGroups

  /** The address contains `::` more than once. */
  case DoublePlaceholder

  /** A group has more than four characters. */
  case TooLongGroup

  /** A group contains a character that is not a hexadecimal digit. */
  case InvalidGroup

  /** The last group contains a `.` but is not a valid IPv4 address. `cause` names the violated
    * rule.
    */
  case InvalidIPv4Group(cause: IPv4AddressError)
}

/** Smart constructor for [[IPv6Address]].
  *
  * @author
  *   Denis-Michael Lux
  */
object IPv6Address {
  import IPv6AddressError.*

  /** Validates that an address is not empty.
    *
    * @param rawHost
    *   the non-null raw address
    * @return
    *   `Right(())` if the address is not empty, or a `Left` containing [[IPv6AddressError.Empty]]
    *   otherwise
    */
  private def validateEmpty(rawHost: String): Either[IPv6AddressError, Unit] =
    if rawHost.isEmpty then Left(Empty)
    else Right(())

  /** Validates the groups of an address against the `IPv6address` rule of RFC 3986.
    *
    * Walks the address once from left to right. Collects the characters of the current group,
    * counts the completed groups, and tracks whether `::` has occurred. Each `:` or `::` completes
    * a group, which must match `h16`. At the end of the input, the last group may also be an
    * embedded IPv4 address, which counts as two groups. Without `::` the address needs exactly
    * eight groups, with `::` at most seven.
    *
    * @param rawHost
    *   the non-null, non-empty raw address without square brackets
    * @return
    *   `Right(())` if the address is valid, or a `Left` containing the [[IPv6AddressError]] of the
    *   first violated rule
    */
  private def validateGroups(rawHost: String): Either[IPv6AddressError, Unit] = {
    enum Compression { case Absent, Present }
    import Compression.*

    @tailrec
    def next(
        remaining: List[Char],
        group: List[Char],
        groupCount: Int,
        compression: Compression
    ): Either[IPv6AddressError, Unit] = {
      import IPv6AddressError.*

      remaining match {
        // double colon cases
        case ':' :: ':' :: ':' :: _                    => Left(EmptyGroup)
        case ':' :: ':' :: _ if compression == Present => Left(DoublePlaceholder)
        case ':' :: ':' :: tail if group.isEmpty       => next(tail, Nil, groupCount, Present)
        case ':' :: ':' :: tail if group.length > 4    => Left(TooLongGroup)
        case ':' :: ':' :: tail if isH16(group)        => next(tail, Nil, groupCount + 1, Present)
        case ':' :: ':' :: _                           => Left(InvalidGroup)

        // single colon cases
        case ':' :: _ if group.isEmpty    => Left(EmptyGroup)
        case ':' :: _ if group.length > 4 => Left(TooLongGroup)
        case ':' :: tail if isH16(group)  => next(tail, Nil, groupCount + 1, compression)
        case ':' :: _                     => Left(InvalidGroup)

        // character cases
        case c :: tail => next(tail, c :: group, groupCount, compression)

        // terminal cases
        case Nil if group.isEmpty && !rawHost.endsWith("::") => Left(EmptyGroup)
        case Nil if group.isEmpty && compression == Absent   => Left(EmptyGroup)
        case Nil if group.isEmpty && groupCount >= 8         => Left(TooManyGroups)
        case Nil if group.isEmpty                            => Right(())
        case Nil if group.contains('.') && groupCount <= 6   =>
          IPv4Address.fromString(group.reverse.mkString) match {
            case Left(error)                        => Left(InvalidIPv4Group(error))
            case Right(_) if compression == Present =>
              if groupCount + 2 < 8 then Right(())
              else Left(TooManyGroups)
            case Right(_) if groupCount + 2 == 8 => Right(())
            case Right(_) if groupCount + 2 < 8  => Left(TooFewGroups)
            case Right(_)                        => Left(TooManyGroups)
          }
        case Nil if group.contains('.')                      => Left(TooManyGroups)
        case Nil if group.length > 4                         => Left(TooLongGroup)
        case Nil if !isH16(group)                            => Left(InvalidGroup)
        case Nil if compression == Present && groupCount < 7 => Right(())
        case Nil if compression == Present                   => Left(TooManyGroups)
        case Nil if groupCount == 7                          => Right(())
        case Nil if groupCount < 7                           => Left(TooFewGroups)
        case Nil                                             => Left(TooManyGroups)
      }
    }

    next(rawHost.toList, Nil, 0, Absent)
  }

  /** Creates an [[IPv6Address]] from its textual representation.
    *
    * The address must not contain square brackets or a zone identifier such as `%eth0`. It keeps
    * its original spelling, so compressed and uncompressed forms of the same address are not equal.
    *
    * @param rawHost
    *   the non-null raw address without square brackets
    * @return
    *   a `Right` containing the address, or a `Left` containing the [[IPv6AddressError]] of the
    *   first violated rule
    */
  def fromString(rawHost: String): Either[IPv6AddressError, IPv6Address] = for {
    _ <- validateEmpty(rawHost)
    _ <- validateGroups(rawHost)
  } yield IPv6Address(rawHost)
}

/** An IP literal in a future address format, as defined in RFC 3986, section 3.2.2.
  *
  * The rule is `IPvFuture = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )`. The leading `v`
  * is case-insensitive. The hexadecimal digits after the `v` name the version of the address
  * format. Instances can only be created through [[IPvFuture.fromString]].
  *
  * @param raw
  *   the literal without square brackets, including the leading `v`, in its original spelling
  *
  * @author
  *   Denis-Michael Lux
  */
final case class IPvFuture private (raw: String) extends Host

/** Syntax errors detected while creating an [[IPvFuture]].
  *
  * @author
  *   Denis-Michael Lux
  */
enum IPvFutureError {

  /** The literal is empty. */
  case Empty

  /** No hexadecimal digit stands between the leading `v` and the `.`. */
  case MissingVersion

  /** The version contains a character that is not a hexadecimal digit. */
  case InvalidVersion

  /** The literal does not start with `v` or `V`. */
  case InvalidPrefix

  /** The literal contains no `.` after the version. */
  case MissingSeparator

  /** Nothing follows the `.` after the version. */
  case EmptyAddress

  /** The part after the version contains a character other than `unreserved`, `sub-delims`, or `:`.
    * Percent-encodings are not permitted.
    */
  case InvalidAddress
}

/** Smart constructor for [[IPvFuture]].
  *
  * @author
  *   Denis-Michael Lux
  */
object IPvFuture {
  import IPvFutureError.*

  /** Creates an [[IPvFuture]] from its textual representation.
    *
    * Checks, in this order, that the literal is not empty, that it starts with `v` or `V`, that a
    * non-empty version follows before the first `.`, that the version consists of hexadecimal
    * digits, and that a non-empty address with permitted characters follows the `.`. Returns the
    * first error found.
    *
    * @param rawHost
    *   the non-null raw literal without square brackets
    * @return
    *   a `Right` containing the literal, or a `Left` containing the [[IPvFutureError]] of the first
    *   violated rule
    */
  def fromString(rawHost: String): Either[IPvFutureError, IPvFuture] = {
    val separator = rawHost.indexOf('.')

    for {
      // empty string and starting with v indicating it's a IPvFuture
      _ <- rawHost match {
        case ""                                  => Left(Empty)
        case s if s.head != 'v' && s.head != 'V' => Left(InvalidPrefix)
        case _                                   => Right(())
      }

      // checking if a version prefix is present
      _ <- separator match {
        case -1 if rawHost.length == 1 => Left(MissingVersion)
        case -1                        => Left(MissingSeparator)
        case 1                         => Left(MissingVersion)
        case _                         => Right(())
      }

      // check if version consists only of hex digits
      _ <- rawHost.substring(1, separator) match {
        case s if !s.forall(isHexDigit) => Left(InvalidVersion)
        case _                          => Right(())
      }

      _ <- rawHost.substring(separator + 1) match {
        case "" => Left(EmptyAddress)
        case s if !s.forall(char => isUnreserved(char) || isSubDelimiters(char) || char == ':') =>
          Left(InvalidAddress)
        case _ => Right(())
      }
    } yield IPvFuture(rawHost)
  }
}

/** Syntax errors detected while creating a [[Host]] or one of its forms.
  *
  * @author
  *   Denis-Michael Lux
  */
enum HostError {

  /** The host is empty. */
  case Empty

  /** The host starts with `[` but does not end with `]`. */
  case UnterminatedIpLiteral

  /** An IPv4 address violates the `IPv4address` rule. `cause` names the violated rule. */
  case MalformedIPv4Address(cause: IPv4AddressError)

  /** The IP literal violates the `IPv6address` rule. `cause` names the violated rule. */
  case MalformedIPv6Address(cause: IPv6AddressError)

  /** The IP literal starts with `v` and violates the `IPvFuture` rule. `cause` names the violated
    * rule.
    */
  case MalformedIPvFutureAddress(cause: IPvFutureError)

  /** The host is not an IPv4 address and violates the `reg-name` rule. `cause` names the violated
    * rule.
    */
  case MalformedRegName(cause: RegNameError)
}

/** Smart constructor for [[Host]].
  *
  * @author
  *   Denis-Michael Lux
  */
object Host {

  /** Creates a [[Host]] from its raw representation.
    *
    * Follows RFC 3986, section 3.2.2, and classifies the host in this order:
    *   1. A host in square brackets is an IP literal. The brackets are removed. A literal that
    *      starts with `v` or `V` is an [[IPvFuture]], every other literal is an [[IPv6Address]].
    *   1. A host that matches `IPv4address` is an [[IPv4Address]].
    *   1. Every other host is a [[RegName]].
    *
    * A host that fails the IPv4 check is not an error but falls back to a registered name. An empty
    * host and a `[` without a closing `]` are rejected before the classification.
    *
    * @param rawHost
    *   the non-null raw host
    * @return
    *   a `Right` containing the classified host, or a `Left` containing the [[HostError]] of the
    *   first violated rule
    */
  def fromString(rawHost: String): Either[HostError, Host] = {
    import HostError.*

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
