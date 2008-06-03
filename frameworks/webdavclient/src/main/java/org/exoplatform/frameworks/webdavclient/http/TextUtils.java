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

package org.exoplatform.frameworks.webdavclient.http;

import java.io.ByteArrayOutputStream;
import java.util.BitSet;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TextUtils {

  public static BitSet       URISave;

  public static BitSet       URISaveEx;

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
    URISave.set(':');
    
    URISave.set('?');
    URISave.set('=');

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

}
