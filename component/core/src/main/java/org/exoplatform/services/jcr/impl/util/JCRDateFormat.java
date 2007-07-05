/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 23.08.2006
 * 
 * ISO 8601

   Year:
      YYYY (eg 1997)
   Year and month:
      YYYY-MM (eg 1997-07)
   Complete date:
      YYYY-MM-DD (eg 1997-07-16)
   Complete date plus hours and minutes:
      YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
   Complete date plus hours, minutes and seconds:
      YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
   Complete date plus hours, minutes, seconds and a decimal fraction of a second
      YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)

   where:

     YYYY = four-digit year
     MM   = two-digit month (01=January, etc.)
     DD   = two-digit day of month (01 through 31)
     hh   = two digits of hour (00 through 23) (am/pm NOT allowed)
     mm   = two digits of minute (00 through 59)
     ss   = two digits of second (00 through 59)
     s    = one or more digits representing a decimal fraction of a second
     TZD  = time zone designator (Z or +hh:mm or -hh:mm)
     a RFC 822 time zone is also accepted:  For formatting, the RFC 822 4-digit time zone format is used:
       RFC822TimeZone:
             Sign TwoDigitHours Minutes
       TwoDigitHours:
             Digit Digit
       like -8000       
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: DateFormatHelper.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class JCRDateFormat {
  
  public static Log log = ExoLogger.getLogger("jcr.JCRDateFormat");
  
  /**
   * ISO 8601, RFC822 formats for JCR datas deserialization in order of priority of parse
   */
  protected static final String[] JCR_FORMATS = {
    ISO8601.COMPLETE_DATETIMEMSZ_FORMAT,
    ISO8601.COMPLETE_DATETIMEMSZRFC822_FORMAT,
    };
  
  protected static final String CALENDAR_FIELDS_DELIMITER = ";"; // hope it's unique for any time zone ID etc.
  protected static final String CALENDAR_FIELDS_SEPARATOR = "--";
  
  /**
   * Format date using complete date plus hours, minutes, seconds and a decimal fraction of a second format.
   * I.e. format to JCR date value string representation. 
   * 
   * @param date
   * @return
   */
  public static String format(Calendar date) {
    return ISO8601.format(date);
  }
  
  /**
   * Parse string using possible formats list.
   * 
   * @param dateString - date string
   * @return - calendar
   * @throws ValueFormatException
   */  
  public static Calendar parse(String dateString) throws ValueFormatException {
    try {
      return ISO8601.parseEx(dateString);
    } catch (ParseException e){
      throw new ValueFormatException("Can not parse date from [" + dateString + "]", e);
    } catch(NumberFormatException e) {
      throw new ValueFormatException("Can not parse date from [" + dateString + "]", e);
    }
  }
  
  /**
   * Deserialize string (of JCR Value) to the date.
   * 
   * @param serString
   * @return
   * @throws ValueFormatException
   */
  public Calendar deserialize(String serString) throws ValueFormatException {
    final String[] parts = serString.split(CALENDAR_FIELDS_SEPARATOR);
    if (parts.length == 2) {
      
      // try parse serialized string with two formats
      // 1. Complete ISO 8610 compliant
      // 2. Complete ISO 8610 + RFC822 (time zone) compliant (JCR 1.6 and prior)
      Calendar isoCalendar = null;
      try {
        isoCalendar = ISO8601.parse(parts[0], JCR_FORMATS);
        
        String[] calendarFields = parts[1].split(CALENDAR_FIELDS_DELIMITER);
        if (calendarFields.length == 4) {
          try {
            isoCalendar.setLenient(Boolean.parseBoolean(calendarFields[0]));
            isoCalendar.setFirstDayOfWeek(Integer.parseInt(calendarFields[1]));
            isoCalendar.setMinimalDaysInFirstWeek(Integer.parseInt(calendarFields[2]));
            
            isoCalendar.setTimeZone(TimeZone.getTimeZone(calendarFields[3]));
          } catch(Exception e) {
            log.warn("Can't parse serialized fields for the calendar [" 
                + parts[1] + "] but calendar has [" + isoCalendar.getTime() + "]", e);
          }
        } else {
          log.warn("Fields of the calendar is not serialized properly [" 
              + parts[1] + "] but calendar has [" + isoCalendar.getTime() + "]");
        }
      } catch (ParseException e) {
        throw new ValueFormatException(e);
      }
      
      return isoCalendar;
    }
    throw new ValueFormatException("Can't deserialize calendar string [" + serString + "]");
  }
  
  /**
   * Serialize date (of JCR Value) to the string.
   * 
   * @param date
   * @return
   */
  public byte[] serialize(Calendar date) {
    
    final String calendarString = CALENDAR_FIELDS_SEPARATOR 
      + date.isLenient() + CALENDAR_FIELDS_DELIMITER 
      + date.getFirstDayOfWeek() + CALENDAR_FIELDS_DELIMITER 
      + date.getMinimalDaysInFirstWeek() + CALENDAR_FIELDS_DELIMITER 
      + date.getTimeZone().getID();
    
    return (format(date) + calendarString).getBytes(); 
  } 
}
