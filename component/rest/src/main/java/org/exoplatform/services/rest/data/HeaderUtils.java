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

  private static Pattern spacePattern = Pattern.compile("(\\s+)");

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
    Matcher m = spacePattern.matcher(s);
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
  private static String[] sortByQvalue(String s[], int lo0, int hi0) {
    int lo = lo0;
    int hi = hi0;
    if (hi0 > lo0) {
      while (lo <= hi) {
        if (getQvalue(s[lo]) <= getQvalue(s[hi])) {
          swap(s, lo, hi);
        }
        lo++;
        hi--;
        if (lo0 < hi) {
          sortByQvalue(s, lo0, hi);
        }
        if (lo < hi0) {
          sortByQvalue(s, lo, hi0);
        }
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

  private static void swap(String a[], int i, int j) {
    String t = a[i];
    a[i] = a[j];
    a[j] = t;
  }

}
