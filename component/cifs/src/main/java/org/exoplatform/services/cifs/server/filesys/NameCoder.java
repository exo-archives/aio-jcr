/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cifs.server.filesys;

/**
 * Class performs transformation the name between jcr and smb modes.
 * <p>
 * Created by The eXo Platform SAS Author : Karpenko
 */
public class NameCoder {

  /**
   * Encode jcr-name to smb-valid name.
   */
  public static String EncodeName(String s) {
    StringBuilder newString = new StringBuilder();
    String mask = "\":+|<>=;,*?"; // []

    int i = 0;
    while (i < s.length()) {
      char c = s.charAt(i);
      if (mask.indexOf(c) > -1) {
        newString.append("'" + Integer.toHexString(c) + "'");
      } else {
        newString.append(c);
      }
      i++;
    }
    return newString.toString();
  }

  /**
   * Decode name form user (SMB valid names) to jcr.
   */
  public static String DecodeName(String s) {
    // TODO "\"[]:+|<>=;,*?" check each char?

    StringBuilder oldName = new StringBuilder(s);
    while (oldName.indexOf("'") != -1) {
      int i = oldName.indexOf("'");
      int i_next = oldName.indexOf("'", i + 1);
      String ss = oldName.substring(i + 1, i_next);
      if ((ss.length() < 2) || (ss.length() > 2)) {
        return null;
      } else {
        oldName.delete(i, i_next + 1);

        int b = Integer.parseInt(ss, 16);
        oldName.insert(i, (char) b);
      }
    }
    return oldName.toString();
  }

  /**
   * Encoding jcr-name to smb-valid name whith specific excepted character set.
   * 
   * @param s - jcr-name to encode
   * @param mask - set of excepted characters like "\"[]:+|<>=;,*?"
   */
  public static String EncodeName(String s, String mask) {
    // StringBuilder s_old = new StringBuilder(s);
    StringBuilder newName = new StringBuilder();

    int i = 0;
    while (i < s.length()) {
      char c = s.charAt(i);
      if (mask.indexOf(c) > -1) {
        newName.append("'" + Integer.toHexString(c) + "'");
      } else {
        newName.append(c);
      }
      i++;
    }
    return newName.toString();
  }

}
