/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 23.08.2006
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: DateFormatHelper.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class DateFormatHelper {
  
  protected static Log log = ExoLogger.getLogger("jcr.DateFormatHelper");
  
  protected static final String DATETZ_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  protected static final String CALENDAR_FIELDS_DELIMITER = ";"; // [PN] I hope it's unique will be for any time zone ID etc.
  protected static final String CALENDAR_FIELDS_SEPARATOR = "--";
  
  public Calendar deserialize(String serString) throws ValueFormatException {
    //System.out.println("Deserialyze date string: [" + serString + "] " + hashCode());
    String[] parts = serString.split(CALENDAR_FIELDS_SEPARATOR);
    if (parts.length == 2) {
      // 22.09.06 [PN] imported from ISO8601.java
      // check optional leading sign (ERA)
      char sign;
      int start;
      if (parts[0].startsWith("-")) {
          sign = '-';
          start = 1;
      } else if (parts[0].startsWith("+")) {
          sign = '+';
          start = 1;
      } else {
          sign = '+'; // no sign specified, implied '+'
          start = 0;
      }
      
      Date isoDate = null;
    	try {
        isoDate = new SimpleDateFormat(DATETZ_FORMAT).parse(parts[0].substring(start));
    	} catch (ParseException e){
        throw new ValueFormatException("Can't parse serialized calendar from '" + parts[0].substring(start) 
            + "' text, source '" + parts[0] + "', string '" + serString + "'", e);
    	} catch(NumberFormatException e) {
        throw new ValueFormatException("Can't parse serialized calendar from '" + parts[0].substring(start) 
            + "' text, source '" + parts[0] + "', string '" + serString + "'", e);
      }
      
      Calendar isoCalendar = Calendar.getInstance();
      isoCalendar.setTime(isoDate);
      //System.out.println("Parse Calendar fields: [" + parts[1] + "] " + hashCode());
      String[] calendarFields = parts[1].split(CALENDAR_FIELDS_DELIMITER);
      if (calendarFields.length == 4) {
        try {
          isoCalendar.setLenient(Boolean.parseBoolean(calendarFields[0]));
          isoCalendar.setFirstDayOfWeek(Integer.parseInt(calendarFields[1]));
          isoCalendar.setMinimalDaysInFirstWeek(Integer.parseInt(calendarFields[2]));
          
          // [PN] 22.01.07
          isoCalendar.setTimeZone(TimeZone.getTimeZone(calendarFields[3]));
        } catch(Exception e) {
          log.warn("Can't parse serialized fields for the calendar '" 
              + parts[1] + "' but calendar has '" + isoDate.toString() + "'", e);
        }
      } else {
        log.warn("Fields of the calendar is not serialized properly '" 
            + parts[1] + "' but calendar has '" + isoDate.toString() + "'");
      }
      
      // 22.09.06 [PN] imported from ISO8601.java
      // Fix ERA
      if (sign == '-' || isoCalendar.get(Calendar.YEAR) == 0) {
        // not CE, need to set era (BCE) and adjust year
        isoCalendar.set(Calendar.YEAR, isoCalendar.get(Calendar.YEAR) + 1);
        isoCalendar.set(Calendar.ERA, GregorianCalendar.BC);
      } 
      
      return isoCalendar;
    }
    throw new ValueFormatException("Can't deserialize calendar string '" + serString + "'");
  }
  
  public byte[] serialize(Calendar date) {
    
    String calendarString = CALENDAR_FIELDS_SEPARATOR 
      + date.isLenient() + CALENDAR_FIELDS_DELIMITER 
      + date.getFirstDayOfWeek() + CALENDAR_FIELDS_DELIMITER 
      + date.getMinimalDaysInFirstWeek() + CALENDAR_FIELDS_DELIMITER 
      + date.getTimeZone().getID();
    
    // [PN] 22.01.07
    SimpleDateFormat formater = new SimpleDateFormat(DATETZ_FORMAT);
    formater.setTimeZone(date.getTimeZone());
    
    return (formater.format(date.getTime()) + calendarString).getBytes(); 
  }    
}
