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
package org.exoplatform.services.jcr.rmi.api.value;

import java.io.Serializable;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.ValueFormatException;

import org.exoplatform.commons.utils.ISO8601;

/**
 * The <code>DateValue</code> class implements the committed value state for
 * Date values as a part of the State design pattern (Gof) used by this package.
 * <p>
 * To convert <code>Calendar</code> instances to and from strings, this class
 * uses a <code>SimpleDateFormat</code> instance with the pattern
 * <code>yyyy-MM-dd'T'HH:mm:ss'Z'</code>. The issue with this pattern is that
 * the era specification as defined in the JCR specification (+/- prefix) as
 * well as full time zone naming are not supported.
 * 
 * @since 0.16.4.1
 * @see org.exoplatform.services.jcr.rmi.api.value.SerialValue
 */
public class DateValue extends BaseNonStreamValue implements Serializable, StatefulValue {

  /** The serial version UID */
  private static final long serialVersionUID = 7158448867450810873L;

  /** The <code>Calendar</code> value */
  private final Calendar    value;

  /**
   * This should probably actually be a reference to the ISO8601 utility class.
   */
  /**
   * Creates an instance for the given <code>Calendar</code> <code>value</code>.
   */
  protected DateValue(Calendar value) {
    this.value = value;
  }

  /**
   * Creates an instance for the given string representation of a
   * <code>Calendar</code>.
   * <p>
   * This implementation uses a <code>ISO8601.format</code> instance with the
   * pattern <code>YYYY-MM-DDThh:mm:ss.sssTZD</code> to parse the string into
   * a <code>Calendar</code> object. See the class comment for issues
   * regarding this pattern.
   */
  protected DateValue(String value) throws ValueFormatException {
    this(toCalendar(value));
  }

  /**
   * Returns the string <code>value</code> parsed into a <code>Calendar</code>
   * instance.
   * 
   * @param value The string value.
   * @return The <code>Calendar</code> instance parsed from the string value.
   * @throws ValueFormatException if the string value cannot be parsed into a
   *           <code>Calendar</code> instance.
   */
  protected static Calendar toCalendar(String value) throws ValueFormatException {
    Calendar time = ISO8601.parse(value);
    if (null == time) {
      throw new ValueFormatException();
    }
    return time;
  }

  /**
   * Returns <code>PropertyType.DATE</code>.
   */
  public int getType() {
    return PropertyType.DATE;
  }

  /**
   * Returns the time represented by this instance as the number of milliseconds
   * since the epoch (1.1.1970, 0:00, UTC).
   */
  @Override
  public double getDouble() {
    return value.getTimeInMillis();
  }

  /**
   * Returns the string represented of this <code>Calendar</code> value
   * formatted using a <code>ISO8601.format</code> with the pattern
   * <code>YYYY-MM-DDThh:mm:ss.sssTZD</code>. See the class comment for
   * issues regarding this pattern.
   * 
   * @throws ValueFormatException
   */
  public String getString() throws ValueFormatException {
    // synchronized (DATE_FORMAT) {
    // return DATE_FORMAT.format(value.getTime());
    // }

    if (value != null) {
      return ISO8601.format(getDate());
    } else {
      throw new ValueFormatException("empty value");
    }

  }

  /**
   * Returns the time represented by this instance as the number of milliseconds
   * since the epoch (1.1.1970, 0:00, UTC).
   */
  @Override
  public long getLong() {
    return value.getTimeInMillis();
  }

  /**
   * Returns (a copy) of this <code>Calendar</code> value. Modifying the
   * returned <code>Calendar</code> does not change the value of this
   * instance.
   */
  @Override
  public Calendar getDate() {
    return (Calendar) value.clone();
  }

  public long getLength() {
    // TODO Auto-generated method stub
    try {
      return getString().length();
    } catch (ValueFormatException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return 0;
  }

  public int getOrderNumber() {
    // TODO Auto-generated method stub
    return 0;
  }
}
