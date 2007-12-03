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
 * The <code>StringValue</code> class implements the committed value state for
 * String values as a part of the State design pattern (Gof) used by this
 * package.
 * 
 * @since 0.16.4.1
 * @see org.exoplatform.services.jcr.rmi.api.value.SerialValue
 */
public class StringValue extends BaseNonStreamValue implements Serializable, StatefulValue {

  /** The serial version UID */
  private static final long serialVersionUID = -6456025645604163205L;

  /** The string value */
  private final String      value;

  /**
   * Creates an instance for the given string <code>value</code>.
   */
  protected StringValue(String value) {
    this.value = value;
  }

  /**
   * Returns <code>PropertyType.STRING</code>.
   */
  public int getType() {
    return PropertyType.STRING;
  }

  /**
   * Returns the string value.
   */
  public String getString() {
    return value;
  }

  /**
   * Returns the string value parsed to a long calling the
   * <code>Long.valueOf(String)</code> method.
   * 
   * @throws ValueFormatException if the string cannot be parsed to long.
   */
  @Override
  public long getLong() throws ValueFormatException {
    return LongValue.toLong(value);
  }

  /**
   * Returns the string value parsed to a double calling the
   * <code>Double.valueOf(String)</code> method.
   * 
   * @throws ValueFormatException if the string cannot be parsed to double.
   */
  @Override
  public double getDouble() throws ValueFormatException {
    return DoubleValue.toDouble(value);
  }

  /**
   * Returns the string value parsed to a <code>Calendar</code> using the same
   * formatter as the {@link DateValue} class. This formatting bears the same
   * issues as parsing and formatting that class.
   * 
   * @throws ValueFormatException if the string cannot be parsed into a
   *           <code>Calendar</code> instance.
   */
  @Override
  public Calendar getDate() throws ValueFormatException {
    return DateValue.toCalendar(value);
  }

  /**
   * Returns the string value parsed to a boolean calling the
   * <code>Boolean.valueOf(String)</code> method.
   */
  @Override
  public boolean getBoolean() {
    return BooleanValue.toBoolean(value);
  }

  public long getLength() {
    // TODO Auto-generated method stub
    return 0;
  }

  public int getOrderNumber() {
    // TODO Auto-generated method stub
    return 0;
  }
}
