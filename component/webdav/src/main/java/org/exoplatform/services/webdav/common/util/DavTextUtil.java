/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.util;

import java.io.ByteArrayOutputStream;
import java.util.BitSet;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class DavTextUtil {
  
  public static String UnEscape(String string, char escape) {
    ByteArrayOutputStream out = new ByteArrayOutputStream(string.length());
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if (c == escape) {
        try {
            out.write(Integer.parseInt(string.substring(i + 1, i + 3), 16));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
        i += 2;
      } else {
          out.write(c);
      }
    }

    try {
        return new String(out.toByteArray(), "utf-8");
    } catch (Exception exc) {
        throw new InternalError(exc.toString());
    }
  }
  
  public static BitSet URISave;
  
  public static BitSet URISaveEx;

  static {
      URISave = new BitSet(256);
      int i;
      for (i = 'a'; i <= 'z'; i++) {
          URISave.set(i);
      }
      for (i = 'A'; i <= 'Z'; i++) {
          URISave.set(i);
      }
      for (i = '0'; i <= '9'; i++) {
          URISave.set(i);
      }
      URISave.set('-');
      URISave.set('_');
      URISave.set('.');
      URISave.set('!');
      URISave.set('~');
      URISave.set('*');
      URISave.set('\'');
      URISave.set('(');
      URISave.set(')');

      URISaveEx = (BitSet) URISave.clone();
      URISaveEx.set('/');
  }
  
  public static final char[] hexTable = "0123456789abcdef".toCharArray();
  
  public static String Escape(String string, char escape, boolean isPath) {
    try {
      BitSet validChars = isPath ? URISaveEx : URISave;
      byte[] bytes = string.getBytes("utf-8");
      StringBuffer out = new StringBuffer(bytes.length);
      for (int i = 0; i < bytes.length; i++) {
        int c = bytes[i] & 0xff;
        if (validChars.get(c) && c != escape) {
          out.append((char) c);
        } else {
          out.append(escape);
          out.append(hexTable[(c >> 4) & 0x0f]);
          out.append(hexTable[(c) & 0x0f]);
        }
      }
      return out.toString();
    } catch (Exception exc) {
        throw new InternalError(exc.toString());
    }
  }
  
  
}
