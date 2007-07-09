/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.value;

import java.io.Serializable;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.ValueFormatException;

/**
 * The <code>DoubleValue</code> class implements the committed value state for
 * Double values as a part of the State design pattern (Gof) used by this
 * package.
 * 
 * @since 0.16.4.1
 * @see org.exoplatform.services.jcr.rmi.api.value.SerialValue
 */
public class DoubleValue extends BaseNonStreamValue implements Serializable, StatefulValue {

  /** The serial version UID */
  private static final long serialVersionUID = 1008752925622023274L;

  /** The double value */
  private final double      value;

  /**
   * Creates an instance for the given double <code>value</code>.
   */
  protected DoubleValue(double value) {
    this.value = value;
  }

  /**
   * Creates an instance for the given string representation of a double.
   * <p>
   * This implementation uses the <code>Double.valueOf(String)</code> method
   * to convert the string to a double.
   * 
   * @throws ValueFormatException if the string <code>value</code> cannot be
   *           parsed to double.
   */
  protected DoubleValue(String value) throws ValueFormatException {
    this(toDouble(value));
  }

  /**
   * Returns the double value represented by the string <code>value</code>.
   * 
   * @throws ValueFormatException if the string <code>value</code> cannot be
   *           parsed to double.
   */
  protected static double toDouble(String value) throws ValueFormatException {
    try {
      return Double.valueOf(value).doubleValue();
    } catch (NumberFormatException e) {
      throw new ValueFormatException(e);
    }
  }

  /**
   * Returns <code>PropertyType.DOUBLE</code>.
   */
  public int getType() {
    return PropertyType.DOUBLE;
  }

  /**
   * Returns a <code>Calendar</code> instance interpreting the double as the
   * time in milliseconds since the epoch (1.1.1970, 0:00, UTC).
   */
  @Override
  public Calendar getDate() throws ValueFormatException {
    Calendar date = Calendar.getInstance();
    date.setTimeInMillis((long) value);
    return date;
  }

  /**
   * Returns the double value.
   */
  @Override
  public double getDouble() {
    return value;
  }

  /**
   * Returns the double as a string converted by the
   * <code>Double.toString(double)</code>.
   */
  public String getString() {
    return Double.toString(value);
  }

  /**
   * Returns the value converted to a long.
   */
  @Override
  public long getLong() {
    return (long) value;
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
