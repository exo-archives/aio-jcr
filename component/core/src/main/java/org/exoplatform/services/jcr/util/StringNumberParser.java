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
 * @version $Id: StringNumberParser.java 15951 2008-06-18 13:11:53Z pnedonosko $
 */
public class StringNumberParser {
  
  /**
   * Parse given text as long. <br/>
   * 
   * <br/>E.g. '2k' will be returned as 2048 number.
   * 
   * <br/>Next formats are supported (case insensitive):
   * <br/>kilobytes - k,kb 
   * <br/>megabytes - m,mb
   * <br/>gigabytes - g,gb
   * <br/>terabytes - t,tb
   * 
   * @param numberText
   * @return
   * @throws NumberFormatException
   */
  static public long parseLong(final String longText) throws NumberFormatException {
    return parseNumber(longText).longValue();
  }
  
  /**
   * Parse given text as int. <br/>
   * 
   * <br/>E.g. '2k' will be returned as 2048 number.
   * 
   * <br/>Next formats are supported (case insensitive):
   * <br/>kilobytes - k,kb 
   * <br/>megabytes - m,mb
   * <br/>gigabytes - g,gb
   * <br/>terabytes - t,tb
   * 
   * @param numberText
   * @return
   * @throws NumberFormatException
   */
  static public int parseInt(final String integerText) throws NumberFormatException {
    return parseNumber(integerText).intValue();
  }

  /**
   * Parse given text as double. <br/>
   * 
   * <br/>E.g. '2k' will be returned as 2048 number.
   * 
   * <br/>Next formats are supported (case insensitive):
   * <br/>kilobytes - k,kb 
   * <br/>megabytes - m,mb
   * <br/>gigabytes - g,gb
   * <br/>terabytes - t,tb
   * 
   * <br/>NOTE: floating point supported, e.g. 1.5m = 1.5 * 1024 * 1024
   * 
   * @param doubleText
   * @return
   * @throws NumberFormatException
   */
  static public double parseDouble(final String doubleText) throws NumberFormatException {
    return parseNumber(doubleText).doubleValue();
  }
  
  /**
   * Parse given text as number representation. <br/>
   * 
   * <br/>E.g. '2k' will be returned as 2048 number.
   * 
   * <br/>Next formats are supported (case insensitive):
   * <br/>kilobytes - k,kb 
   * <br/>megabytes - m,mb
   * <br/>gigabytes - g,gb
   * <br/>terabytes - t,tb
   * 
   * <br/>NOTE: floating point supported, e.g. 1.5m = 1.5 * 1024 * 1024,
   * <br/>WARN: floating point delimiter depends on OS settings
   * 
   * @param numberText
   * @return
   * @throws NumberFormatException
   */
  static public Number parseNumber(final String numberText) throws NumberFormatException {
    final String text = numberText.toLowerCase().toUpperCase();
    if (text.endsWith("K")) {
      return Double.valueOf(text.substring(0, text.length() - 1)) * 1024d;
    } else if (text.endsWith("KB")) {
      return Double.valueOf(text.substring(0, text.length() - 2)) * 1024d;  
    } else if (text.endsWith("M")) {
      return Double.valueOf(text.substring(0, text.length() - 1)) * 1048576d; // 1024 * 1024
    } else if (text.endsWith("MB")) {
      return Double.valueOf(text.substring(0, text.length() - 2)) * 1048576d; // 1024 * 1024
    } else if (text.endsWith("G")) {
      return Double.valueOf(text.substring(0, text.length() - 1)) * 1073741824d; // 1024 * 1024 * 1024
    } else if (text.endsWith("GB")) {
      return Double.valueOf(text.substring(0, text.length() - 2)) * 1073741824d; // 1024 * 1024 * 1024
    } else if (text.endsWith("T")) {  
      return Double.valueOf(text.substring(0, text.length() - 1)) * 1099511627776d; // 1024 * 1024 * 1024 * 1024
    } else if (text.endsWith("TB")) {  
      return Double.valueOf(text.substring(0, text.length() - 2)) * 1099511627776d; // 1024 * 1024 * 1024 * 1024
    } else {
      return Double.valueOf(text);
    }
  }
  
  /**
   * Parse given text as formated time and return a time in milliseconds. <br/>
   * <br/>Formats supported:
   * <br/>milliseconds - ms 
   * <br/>seconds - without sufix
   * <br/>minutes - m
   * <br/>hours - h
   * <br/>days - d
   * <br/>weeks - w
   * 
   * <br/>TODO handle strings like 2d+4h, 2h+30m+15s+500 etc.
   * 
   * @param timeText - String 
   * @return time in milliseconds
   * @throws NumberFormatException
   */
  static public long parseTime(final String text) throws NumberFormatException {
    if (text.endsWith("ms")) {
      return Long.valueOf(text.substring(0, text.length() - 2)); 
    } else if (text.endsWith("m")) {
      return Long.valueOf(text.substring(0, text.length() - 1)) * 60000;  // 1000 * 60
    } else if (text.endsWith("h")) {
      return Long.valueOf(text.substring(0, text.length() - 1)) * 3600000; // 1000 * 60 * 60
    } else if (text.endsWith("d")) {
      return Long.valueOf(text.substring(0, text.length() - 1)) * 86400000; // 1000 * 60 * 60 * 24
    } else if (text.endsWith("w")) {
      return Long.valueOf(text.substring(0, text.length() - 1)) * 604800000; // 1000 * 60 * 60 * 24 * 7
    } else { // seconds by default
      return Long.valueOf(text) * 1000;
    }
  }
}
