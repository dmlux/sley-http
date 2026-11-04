package sley.http.syntax

import sley.http.syntax.CoreRules.{isDigit, isHexDigit}

/** Rules for IP addresses defined in RFC 3986, section 3.2.2.
  *
  * The rules build on the ABNF core rules in [[CoreRules]].
  *
  * See [[https://www.rfc-editor.org/info/rfc3986/ RFC 3986]].
  *
  * @author
  *   Denis-Michael Lux
  */
private[http] object IpSyntax {

  /** Tests the `dec-octet` rule, which matches a decimal number from 0 through 255.
    *
    * The rule is defined as follows:
    * {{{
    * dec-octet = DIGIT                 ; 0-9
    *           / %x31-39 DIGIT         ; 10-99
    *           / "1" 2DIGIT            ; 100-199
    *           / "2" %x30-34 DIGIT     ; 200-249
    *           / "25" %x30-35          ; 250-255
    * }}}
    *
    * Leading zeros are not permitted, so `01` and `001` do not match.
    *
    * @param sequence
    *   the non-null sequence to test
    * @return
    *   `true` if the sequence is a `dec-octet`, otherwise `false`
    */
  def isDecOctet(sequence: String): Boolean =
    sequence.length match
      case 1 => isDigit(sequence.head)
      case 2 => sequence.head != '0' && sequence.forall(isDigit)
      case 3 => sequence.head != '0' && sequence.forall(isDigit) && sequence.toInt <= 255
      case _ => false

  /** Tests the `h16` rule, `h16 = 1*4HEXDIG`, which matches one group of an IPv6 address.
    *
    * A group encodes 16 bits as one to four hexadecimal digits. Leading zeros are permitted.
    *
    * @param sequence
    *   the characters of the group
    * @return
    *   `true` if the group has one to four hexadecimal digits, otherwise `false`
    */
  def isH16(sequence: List[Char]): Boolean = sequence.nonEmpty
    && sequence.length <= 4
    && sequence.forall(isHexDigit)
}
