package sley.http.syntax

/** Core rules of the ABNF notation defined in RFC 5234, appendix B.1.
  *
  * Other RFCs, including RFC 3986 and RFC 9110, build their grammars on these rules. Each predicate
  * tests a single character and knows nothing about the context in which the character appears.
  *
  * See [[https://www.rfc-editor.org/info/rfc5234/ RFC 5234]].
  *
  * @author
  *   Denis-Michael Lux
  */
private[http] object CoreRules {

  /** Tests the `ALPHA` rule, `%x41-5A / %x61-7A`, which matches an ASCII letter.
    *
    * @param char
    *   the character to test
    * @return
    *   `true` if the character is an ASCII letter, otherwise `false`
    */
  def isAlpha(char: Char): Boolean = char match
    case c if 'a' <= c && c <= 'z' => true
    case c if 'A' <= c && c <= 'Z' => true
    case _                         => false

  /** Tests the `DIGIT` rule, `%x30-39`, which matches an ASCII decimal digit.
    *
    * @param char
    *   the character to test
    * @return
    *   `true` if the character is a decimal digit, otherwise `false`
    */
  def isDigit(char: Char): Boolean = char match
    case c if '0' <= c && c <= '9' => true
    case _                         => false

  /** Tests the `HEXDIG` rule, which matches an ASCII hexadecimal digit.
    *
    * RFC 5234 defines the rule as `DIGIT / "A" / "B" / "C" / "D" / "E" / "F"`. String literals in
    * ABNF are case-insensitive, so the lowercase letters `a` through `f` match as well.
    *
    * @param char
    *   the character to test
    * @return
    *   `true` if the character is a hexadecimal digit, otherwise `false`
    */
  def isHexDigit(char: Char): Boolean = char match
    case c if isDigit(c)           => true
    case c if 'a' <= c && c <= 'f' => true
    case c if 'A' <= c && c <= 'F' => true
    case _                         => false

  /** Tests the `VCHAR` rule, `%x21-7E`, which matches a visible ASCII character.
    *
    * The range covers every printable ASCII character from `!` through `~`. It excludes the space
    * character, all control characters, and every character outside ASCII.
    *
    * @param char
    *   the character to test
    * @return
    *   `true` if the character is a visible ASCII character, otherwise `false`
    */
  def isVChar(char: Char): Boolean = char match
    case c if '!' <= c && c <= '~' => true
    case _                         => false

  /** Tests the `CTL` rule, `%x00-1F / %x7F`, which matches an ASCII control character.
    *
    * The rule covers the characters 0x00 through 0x1F, including horizontal tab, carriage return,
    * and line feed, and the delete character 0x7F. Characters outside ASCII are not matched.
    *
    * @param char
    *   the character to test
    * @return
    *   `true` if the character is an ASCII control character, otherwise `false`
    */
  def isControl(char: Char): Boolean = char match
    case c if c <= '\u001f' => true
    case c if c == '\u007f' => true
    case _                  => false
}
