/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.util;

/**
 * Created by The eXo Platform SARL
 *
 * 31.08.2006
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: StringNumberParser.java 12843 2007-02-16 09:11:18Z peterit $
 */
public class StringNumberParser {
  
  static public Number parseNumber(String integerText) throws NumberFormatException {
    String text = integerText.toLowerCase().toUpperCase();
    Number numb = null;
    if (text.endsWith("K")) {
      numb = new Double(text.substring(0, text.length() - 1)) * 1024;
    } else if (text.endsWith("KB")) {
      numb = new Double(text.substring(0, text.length() - 2)) * 1024;  
    } else if (text.endsWith("M")) {
      numb = new Double(text.substring(0, text.length() - 1)) * 1024 * 1024;
    } else if (text.endsWith("MB")) {
      numb = new Double(text.substring(0, text.length() - 2)) * 1024 * 1024;
    } else if (text.endsWith("G")) {
      numb = new Double(text.substring(0, text.length() - 1)) * 1024 * 1024 * 1024;
    } else if (text.endsWith("GB")) {
      numb = new Double(text.substring(0, text.length() - 2)) * 1024 * 1024 * 1024;
    } else if (text.endsWith("T")) {  
      numb = new Double(text.substring(0, text.length() - 1)) * 1024 * 1024 * 1024 * 1024;
    } else if (text.endsWith("TB")) {  
      numb = new Double(text.substring(0, text.length() - 2)) * 1024 * 1024 * 1024 * 1024;
    } else {
      numb = new Double(text);
    }
    return numb;
  }
}
