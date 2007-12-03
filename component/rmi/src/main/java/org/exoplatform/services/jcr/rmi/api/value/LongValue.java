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

/**
 * The <code>LongValue</code> class implements the committed value state for
 * Long values as a part of the State design pattern (Gof) used by this package.
 * 
 * @since 0.16.4.1
 * @see org.exoplatform.services.jcr.rmi.api.value.SerialValue
 */
public class LongValue extends BaseNonStreamValue implements Serializable, StatefulValue {

  /** The serial version UID */
  private static final long serialVersionUID = 2115837525193497922L;

  /** The long value */
  private final long        value;

  /**
   * Creates an instance for the given long <code>value</code>.
   */
  protected LongValue(long value) {
    this.value = value;
  }

  /**
   * Creates an instance for the given string representation of a long.
   * <p>
   * This implementation uses the <code>Long.valueOf(String)</code> method to
   * convert the string to a long.
   * 
   * @throws ValueFormatException if the string <code>value</code> cannot be
   *           parsed to long.
   */
  protected LongValue(String value) throws ValueFormatException {
    this(toLong(value));
  }

  /**
   * Returns the long value represented by the string <code>value</code>.
   * 
   * @throws ValueFormatException if the string <code>value</code> cannot be
   *           parsed to long.
   */
  protected static long toLong(String value) throws ValueFormatException {
    try {
      return Long.valueOf(value).longValue();
    } catch (NumberFormatException e) {
      throw new ValueFormatException(e);
    }
  }

  /**
   * Returns <code>PropertyType.LONG</code>.
   */
  public int getType() {
    return PropertyType.LONG;
  }

  /**
   * Returns a <code>Calendar</code> instance interpreting the long as the
   * time in milliseconds since the epoch (1.1.1970, 0:00, UTC).
   */
  @Override
  public Calendar getDate() {
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis(value);
    return date;
  }

  /**
   * Returns the double value.
   */
  @Override
  public long getLong() {
    return value;
  }

  /**
   * Returns the long as a string converted by the
   * <code>Long.toString(long)</code>.
   */
  public String getString() {
    return Long.toString(value);
  }

  /**
   * Returns the value converted to a double.
   */
  @Override
  public double getDouble() {
    return value;
  }

  public long getLength() {
    // TODO Auto-generated method stub
    return getString().length();
  }

  public int getOrderNumber() {
    // TODO Auto-generated method stub
    return 0;
  }
}
