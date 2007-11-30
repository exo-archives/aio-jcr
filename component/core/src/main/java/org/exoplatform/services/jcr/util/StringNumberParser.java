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

package org.exoplatform.services.jcr.util;

/**
 * Created by The eXo Platform SAS
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
