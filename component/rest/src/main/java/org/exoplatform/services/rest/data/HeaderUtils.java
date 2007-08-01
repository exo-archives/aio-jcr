/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest.data;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This class has the set of utils for work with HTTP headers.
 * 
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class HeaderUtils {
  
  /*
   * Class has only static methods. 
   */
  private HeaderUtils() {}

  private static final Pattern SPACE_PATTERN = Pattern.compile("(\\s+)");

  /**
   * Parse the string in the form of "text/xml;q=0.9,text/plain;q=0.7" into
   * array of string and sort it by priority "q". Example string above can be
   * parse in array {"text/xml", "text/plain"}
   * @param s the string which should be parsed
   * @return resul array
   */
  public static String[] parse(String s) {
    if (s != null && s.length() != 0) {
      s = normalizeAccepString(s);
      String[] ss = s.split(",");
      sortByQvalue(ss, 0, ss.length - 1);
      return removeQvalues(ss);
    }
    return null;
  }

  /**
   * Remove white spase from string.
   * @param s the startinf strin
   * @return the resul string
   */
  public static String normalizeAccepString(String s) {
    Matcher m = SPACE_PATTERN.matcher(s);
    return m.replaceAll("");
  }

  /*
   * Check if the quality beetwen 0 an 1. Otherwise IllegalArgumentException. 
   */
  private static float parseQuality(String s) {
    float q = Float.valueOf(s);
    if (q >= 0f && q <= 1.0f) {
      return q;
    }
    throw new IllegalArgumentException("Invalid quality value " + q + ", must be between 0 and 1");
  }

  
  /*
   * sort array by quality. The strings with higher quality go first.
   */
  private static String[] sortByQvalue(String s[], int i0, int k0) {
    int i = i0;
    int k = k0;
    if (k0 > i0) {
      float middleQvalue = getQvalue(s[(i0 + k0) / 2]);
      while (i <= k) {
        while (i < k0 && getQvalue(s[i]) > middleQvalue) {
          i++;
        }
        while (k > i0 && getQvalue(s[k]) < middleQvalue) {
          k--;
        }
        if (i <= k) {
          swap(s, i, k);
          i++;
          k--;
        }
      }
      if (i0 < k) {
        sortByQvalue(s, i0, k);
      }
      if (i < k0) {
        sortByQvalue(s, i, k0);
      }
    }
    return s;
  }

  private static Float getQvalue(String s) {
    float q = 1.0f;
    String[] temp = s.split(";");
    for (String t : temp) {
      if (t.startsWith("q=")) {
        String[] qq = t.split("=");
        q = parseQuality(qq[1]);
      }
    }
    return q;
  }

  private static String[] removeQvalues(String[] s) {
    for (int i = 0; i < s.length; i++) {
      s[i] = s[i].split(";q=")[0];
    }
    return s;
  }

  private static void swap(String s[], int i, int j) {
    String t = s[i];
    s[i] = s[j];
    s[j] = t;
  }

}
