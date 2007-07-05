/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
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
   Complete date plus hours, minutes, seconds and a decimal fraction of a
second
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
public class DateFormatHelper {
  
  public static Log log = ExoLogger.getLogger("jcr.DateFormatHelper");
  
  /**
   * ISO 8601 time zone designator
   */
  protected static final String TZD = "TZD";
  
  /**
   * Year:
   * YYYY (eg 1997)
   */
  public static final String YEAR_FORMAT = "yyyy";
  
  /**
   * Year and month:
   * YYYY-MM (eg 1997-07)
   */
  public static final String YEARMONTH_FORMAT = "yyyy-MM";  
  
  /**
   * Complete date:
   * YYYY-MM-DD (eg 1997-07-16)
   */
  public static final String COMPLETE_DATE_FORMAT = "yyyy-MM-dd";
  
  /**
   * NON ISO STANDARD. Simple date plus hours and minutes, wothout timezone:
   * YYYY-MM-DDThh:mm (eg 1997-07-16T19:20)
   */
  public static final String SIMPLE_DATEHOURSMINUTES_FORMAT = "yyyy-MM-dd'T'HH:mm";  
  
  /**
   * NON ISO STANDARD. Complete date plus hours and minutes, with timezone by RFC822:
   * YYYY-MM-DDThh:mmZ (eg 1997-07-16T19:20+0100)
   */
  public static final String COMPLETE_DATEHOURSMINUTESZRFC822_FORMAT = "yyyy-MM-dd'T'HH:mmZ";  
  
  /**
   * Complete date plus hours and minutes:
   * YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
   */
  public static final String COMPLETE_DATEHOURSMINUTESZ_FORMAT = "yyyy-MM-dd'T'HH:mm" + TZD;
  
  /**
   * NON ISO STANDARD. Simple date plus hours, minutes and seconds, wothout timezone:
   * YYYY-MM-DDThh:mm:ss (eg 1997-07-16T19:20:30)
   */
  public static final String SIMPLE_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
  
  /**
   * NON ISO STANDARD. Complete date plus hours, minutes and seconds, with timezone by RFC822:
   * YYYY-MM-DDThh:mm:ssZ (eg 1997-07-16T19:20:30+0100)
   */
  public static final String COMPLETE_DATETIMEZRFC822_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
  
  /**
   * Complete date plus hours, minutes and seconds:
   * YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
   */
  public static final String COMPLETE_DATETIMEZ_FORMAT = "yyyy-MM-dd'T'HH:mm:ss" + TZD;

  /**
   * NON ISO STANDARD. Simple date plus hours, minutes, seconds and a decimal fraction of a second, wothout timezone
   * YYYY-MM-DDThh:mm:ss.s (eg 1997-07-16T19:20:30.45)
   */
  public static final String SIMPLE_DATETIMEMS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";
  
  /**
   * Complete date plus hours, minutes, seconds and a decimal fraction of a second, with timezone by RFC822
   * YYYY-MM-DDThh:mm:ss.sZ (eg 1997-07-16T19:20:30.45+0100)
   */
  public static final String COMPLETE_DATETIMEMSZRFC822_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  
  /**
   * Complete date plus hours, minutes, seconds and a decimal fraction of a second
   * YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)
   */
  protected static final String COMPLETE_DATETIMEMSZ_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS" + TZD;
  
  /**
   * ISO 8601, RFC822 + simple formats in order of priority of parse
   */
  protected static final String[] FORMATS = {
    COMPLETE_DATETIMEMSZ_FORMAT,
    COMPLETE_DATETIMEMSZRFC822_FORMAT,
    SIMPLE_DATETIMEMS_FORMAT,
    COMPLETE_DATETIMEZ_FORMAT, 
    COMPLETE_DATETIMEZRFC822_FORMAT,
    SIMPLE_DATETIME_FORMAT,
    COMPLETE_DATEHOURSMINUTESZ_FORMAT,
    COMPLETE_DATEHOURSMINUTESZRFC822_FORMAT,
    SIMPLE_DATEHOURSMINUTES_FORMAT,
    COMPLETE_DATE_FORMAT, 
    YEARMONTH_FORMAT, 
    YEAR_FORMAT 
    };
  
  /**
   * ISO 8601, RFC822 formats for JCR datas deserialization in order of priority of parse
   */
  protected static final String[] JCR_FORMATS = {
    COMPLETE_DATETIMEMSZ_FORMAT,
    COMPLETE_DATETIMEMSZRFC822_FORMAT,
    };
  
  protected static final String CALENDAR_FIELDS_DELIMITER = ";"; // hope it's unique for any time zone ID etc.
  protected static final String CALENDAR_FIELDS_SEPARATOR = "--";
  
  protected class ISO8601DateFormat extends SimpleDateFormat {
    
    private final boolean isoTZ;
    
    ISO8601DateFormat(String format) {
      super(format.endsWith(TZD) ? format.substring(0, format.length() - TZD.length()) + "Z" : format);
      this.isoTZ = format.endsWith(TZD);
    }

    public Calendar parseISO(String dateString) throws ValueFormatException {
      
      Date isoDate = null;
      try {
        
        if (dateString.length() >= 16 && isoTZ) { 
          // need fix TZ from ISO 8601 (+01:00) to RFC822 (+0100)
          if (dateString.endsWith("Z")) {
            dateString = dateString.substring(0, dateString.length()-1) + "+0000";
          } else {
            int tzsindex = dateString.length() - 6;
            char tzsign = dateString.charAt(tzsindex); // sixth char from the end
            if (tzsign == '+' || tzsign == '-') {
              dateString = dateString.substring(0, tzsindex) + dateString.substring(tzsindex).replaceAll(":", ""); 
            }
          }
        }
                
        isoDate = super.parse(dateString);
      } catch (ParseException e){
        throw new ValueFormatException("Can not parse date from [" + dateString 
            + "]", e);
      } catch(NumberFormatException e) {
        throw new ValueFormatException("Can not parse date from [" + dateString 
            + "]", e);
      }
      
      Calendar isoCalendar = Calendar.getInstance();
      isoCalendar.setTime(isoDate);
      
      return isoCalendar;
    }
    
    public String formatISO(Calendar source) {
      if (isoTZ) {
        super.setTimeZone(source.getTimeZone());
        String formatedDate = super.format(source.getTime());
        
        if (formatedDate.endsWith("0000")) {
          return formatedDate.substring(0, formatedDate.length() - 5) + "Z"; // GMT (UTC)
        } else {
          int dindex = formatedDate.length() - 2;
          return formatedDate.substring(0, dindex) + ":" + formatedDate.substring(dindex); // GMT offset
        }
        
      } else 
        return super.format(source);
    }
  }

  /**
   * Format date using complete date plus hours, minutes, seconds and a decimal fraction of a second format.
   * I.e. format to JCR date value string representation. 
   * 
   * @param date
   * @return
   */
  public String format(Calendar date) {
    return new ISO8601DateFormat(COMPLETE_DATETIMEMSZ_FORMAT).formatISO(date);
  }
  
  /**
   * Parse string using possible formats list.
   * 
   * @param dateString - date string
   * @return - calendar
   * @throws ValueFormatException
   */  
  public Calendar parse(String dateString) throws ValueFormatException {
    return parse(dateString, FORMATS);
  }
  
  protected Calendar parse(String dateString, String[] formats) throws ValueFormatException {
    String problems = "";
    for (String format: formats) {
      try {
        Calendar isoDate = new ISO8601DateFormat(format).parseISO(dateString);
        return isoDate; // done
      } catch(ValueFormatException e) {
        problems += format + " -- " + e.getMessage() + " \n";
      }
    }
    
    throw new ValueFormatException("Can not parse " + dateString + " as Date. " + problems); // "Can not parse " + dateString + " as Date, problems: " + 
  }
    
  /**
   * Deserialize string (of JCR Value) to the date.
   * 
   * @param serString
   * @return
   * @throws ValueFormatException
   */
  public Calendar deserialize(String serString) throws ValueFormatException {
    String[] parts = serString.split(CALENDAR_FIELDS_SEPARATOR);
    if (parts.length == 2) {
      
      // try parse serialized string with two formats
      // 1. Complete ISO 8610 compliant
      // 2. Complete ISO 8610 + RFC822 (time zone) compliant (JCR 1.6 and prior)
      Calendar isoCalendar = parse(parts[0], JCR_FORMATS);
      
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
    
    String calendarString = CALENDAR_FIELDS_SEPARATOR 
      + date.isLenient() + CALENDAR_FIELDS_DELIMITER 
      + date.getFirstDayOfWeek() + CALENDAR_FIELDS_DELIMITER 
      + date.getMinimalDaysInFirstWeek() + CALENDAR_FIELDS_DELIMITER 
      + date.getTimeZone().getID();
    
    return (format(date) + calendarString).getBytes(); 
  } 
}
