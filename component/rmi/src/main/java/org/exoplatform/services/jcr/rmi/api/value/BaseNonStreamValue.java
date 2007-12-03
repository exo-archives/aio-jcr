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

import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

/**
 * The <code>BaseNonStreamValue</code> class implements the basic committed
 * value state for non-stream values as a part of the State design pattern (Gof)
 * used by this package.
 * <p>
 * This class implements all methods of the
 * {@link org.exoplatform.services.jcr.rmi.api.value.StatefullValue} except
 * <code>getString</code> and <code>getType</code> interface by always
 * throwing an appropriate exception. Extensions of this class should overwrite
 * methods as appropriate except for the {@link #getStream()} which must throw
 * an <code>IllegalStateException</code> for this line of committed non-stream
 * states.
 */
public abstract class BaseNonStreamValue implements StatefulValue {
  
  protected int orderNumber; 
  
  public int getOrderNumber() {
    return orderNumber;
  }

  public void setOrderNumber(int orderNumber) {
    this.orderNumber = orderNumber;
  }

  /**
   * Default constructor with no special tasks.
   */
  protected BaseNonStreamValue() {
  }

  /**
   * Always throws <code>IllegalStateException</code> because only non-stream
   * getters are available from this implementation.
   * <p>
   * This method is declared final to mark that this line of implementations
   * does not provide access to <code>InputStream</code>s.
   * 
   * @return nothing
   * @throws IllegalStateException as defined above.
   */
  public  InputStream getStream() throws IllegalStateException {
    throw new IllegalStateException("Stream not available");
  }

  /**
   * Always throws a <code>ValueFormatException</code>. Implementations
   * should overwrite if conversion to boolean is supported.
   * 
   * @return nothing
   * @throws ValueFormatException If the value cannot be converted to a boolean.
   */
  public boolean getBoolean() throws ValueFormatException {
    throw getValueFormatException(PropertyType.TYPENAME_BOOLEAN);
  }

  /**
   * Always throws a <code>ValueFormatException</code>. Implementations
   * should overwrite if conversion to <code>Calender</code> is supported.
   * 
   * @return nothing
   * @throws ValueFormatException If the value cannot be converted to a
   *           <code>Calendar</code> instance.
   */
  public Calendar getDate() throws ValueFormatException {
    throw getValueFormatException(PropertyType.TYPENAME_DATE);
  }

  /**
   * Always throws a <code>ValueFormatException</code>. Implementations
   * should overwrite if conversion to double is supported.
   * 
   * @return nothing
   * @throws ValueFormatException If the value cannot be converted to a double.
   */
  public double getDouble() throws ValueFormatException {
    throw getValueFormatException(PropertyType.TYPENAME_DOUBLE);
  }

  /**
   * Always throws a <code>ValueFormatException</code>. Implementations
   * should overwrite if conversion to long is supported.
   * 
   * @return nothing
   * @throws ValueFormatException If the value cannot be converted to a long.
   */
  public long getLong() throws ValueFormatException {
    throw getValueFormatException(PropertyType.TYPENAME_LONG);
  }

  /**
   * Returns a <code>ValueFormatException</code> with a message indicating
   * what kind of type conversion is not supported.
   * 
   * @return nothing
   * @param destType The name of the value type to which this value cannot be
   *          converted.
   */
  protected ValueFormatException getValueFormatException(String destType) {
    return new ValueFormatException("Cannot convert value of type "
        + PropertyType.nameFromValue(getType()) + " to " + destType);
  }

  /**
   * @return
   * @throws ValueFormatException, IllegalStateException, RepositoryException
   */
  public String getReference() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    throw new ValueFormatException("Can not convert " + PropertyType.nameFromValue(this.getType())
        + " to Reference");
  }

}
