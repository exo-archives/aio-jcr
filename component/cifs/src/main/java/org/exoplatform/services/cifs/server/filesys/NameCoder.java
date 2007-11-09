/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cifs.server.filesys;

/**
 * Class performs transformationthe name between jcr and smb modes
 * 
 * Created by The eXo Platform SARL Author : Karpenko
 * 
 */

public class NameCoder {
  /**
   * Encoding jcr-name to smb-valid name
   */

  public static String EncodeName(String s) {
    StringBuilder s_new = new StringBuilder();
    String mask = "\":+|<>=;,*?"; //[]

    int i = 0;
    while (i < s.length()) {
      char c = s.charAt(i);
      if (mask.indexOf(c) > -1) {
        s_new.append("'" + Integer.toHexString(c) + "'");
      } else {
        s_new.append(c);
      }
      i++;
    }
    return s_new.toString();
  }

  /**
   * Decoding name form user (SMB valid names) to jcr
   * 
   */
  public static String DecodeName(String s) {
    // TODO "\"[]:+|<>=;,*?" check each char?

    StringBuilder s_old = new StringBuilder(s);
    while (s_old.indexOf("'") != -1) {
      int i = s_old.indexOf("'");
      int i_next = s_old.indexOf("'", i + 1);
      String ss = s_old.substring(i + 1, i_next);
      if ((ss.length() < 2) || (ss.length() > 2)) {
        return null;
      } else {
        s_old.delete(i, i_next + 1);

        int b = Integer.parseInt(ss, 16);
        s_old.insert(i, (char) b);
      }
    }
    return s_old.toString();
  }

  /**
   * Encoding jcr-name to smb-valid name whith specific excepted character set
   * 
   * @param s -
   *          jcr-name to encode
   * @param mask -
   *          set of excepted characters like "\"[]:+|<>=;,*?"
   */
  public static String EncodeName(String s, String mask) {
  //  StringBuilder s_old = new StringBuilder(s);
    StringBuilder s_new = new StringBuilder();

    int i = 0;
    while (i < s.length()) {
      char c = s.charAt(i);
      if (mask.indexOf(c) > -1) {
        s_new.append("'" + Integer.toHexString(c) + "'");
      } else {
        s_new.append(c);
      }
      i++;
    }
    return s_new.toString();
  }

}
