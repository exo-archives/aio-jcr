/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.util;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: StringConverter.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class StringConverter {

  private static String ILLEGAL_DNCHAR = "StringConverter: empty string for denormalization to char";

  private static Log    log            = ExoLogger.getLogger("jcr.StringConverter");

  private static class DNChar {
    private char dnChar;

    private int  dnLength;

    public DNChar(char dnChar, int dnLength) {
      this.dnChar = dnChar;
      this.dnLength = dnLength;
    }

    public char getDnChar() {
      return dnChar;
    }

    public int getDnLength() {
      return dnLength;
    }
  }

  /**
   * Normalizes and prints the given string.
   */
  public static String normalizeString(String s, boolean canonical) {

    StringBuffer strBuf = new StringBuffer();
    int len = (s != null) ? s.length() : 0;
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      String spart = s.substring(i);
      if (spart.startsWith("_") && !spart.startsWith("_x")) {
        strBuf.append(c);
      } else {
        strBuf.append(normalizeChar(c, canonical));
      }
    }
    return new String(strBuf);
  }

  public static String denormalizeString(String s) {

    StringBuffer strBuf = new StringBuffer();
    int len = (s != null) ? s.length() : 0;
    int i = 0;
    while (i < len) {
      try {
        DNChar dnc = denormalize(s.substring(i));
        strBuf.append(dnc.getDnChar());
        i += dnc.getDnLength();
      } catch (IllegalArgumentException e) {
        if (!e.getMessage().equals(ILLEGAL_DNCHAR)) {
          throw e;
        }
        char c = s.charAt(i);
        strBuf.append(c);
        i++;
      }
    }
    return new String(strBuf);
  }

  /**
   * Normalizes and print the given character.
   */
  public static String normalizeChar(char c, boolean canonical) {

    switch (c) {
    case '<':
      return "&lt;";
    case '>':
      return "&gt;";
    case '&':
      return "&amp;";
    case '"':
      return "&quot;";
    case '\'':
      return "&apos;";
    case '\r':
      if (canonical)
        return "_x000D_";
    case '\n':
      if (canonical)
        return "_x000A_";
    case '\t':
      if (canonical)
        return "_x0009_";
    case ' ':
      if (canonical)
        return "_x0020_";
    case '_':
      if (canonical)
        return "_x005f_";
      // else, default print char
    default:
      return "" + c;
    }
  }

  /**
   * Denormalizes and print the given character.
   */
  public static char denormalizeChar(String string) {
    return denormalize(string).getDnChar();
  }

  /**
   * Denormalizes and print the given character.
   */
  private static DNChar denormalize(String string) {

    if (string.startsWith("&lt;"))
      return new DNChar('<', 4);
    else if (string.startsWith("&gt;"))
      return new DNChar('>', 4);
    else if (string.startsWith("&amp;"))
      return new DNChar('&', 5);
    else if (string.startsWith("&quot;"))
      return new DNChar('"', 6);
    else if (string.startsWith("&apos;"))
      return new DNChar('\'', 6);
    else if (string.startsWith("_x000D_"))
      return new DNChar('\r', 7);
    else if (string.startsWith("_x000A_"))
      return new DNChar('\n', 7);
    /**
     * Denormalize of this value cause a 4 fails in TCK. If we don'n do it, it
     * text will be remain the "_x0009_" value instead of "\t" TCK tests fail
     * because the checkImportSimpleXMLTree method of DocumentViewImportTest
     * object have a small problem in this place
     *  // both possibilities In logic
     *                  if (!propVal.equals(encodedAttributeValue)
     *                   || !propVal.equals(encodedAttributeValue)) {
     *               fail("Value " + encodedAttributeValue + "  of attribute " +
     *                       decodedAttributeName + " is not correctly imported.");
     *
     * of test the propVal must be equal of encodedAttributeValue the encoded
     * version of value
     */
    else if (string.startsWith("_x0009_"))
      return new DNChar('\t', 7);
    else if (string.startsWith("_x0020_"))
      return new DNChar(' ', 7);
    else if (string.startsWith("_x005f_"))
      return new DNChar('_', 7);
    else
      throw new IllegalArgumentException(ILLEGAL_DNCHAR);
  }
}