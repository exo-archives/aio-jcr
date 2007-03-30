/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient;

import java.util.BitSet;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class TextUtils {

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
