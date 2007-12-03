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

import javax.jcr.PropertyType;

/**
 * The <code>BooleanValue</code> class implements the committed value state
 * for Boolean values as a part of the State design pattern (Gof) used by this
 * package.
 * 
 * @since 0.16.4.1
 * @see org.exoplatform.services.jcr.rmi.api.value.SerialValue
 */
public class BooleanValue extends BaseNonStreamValue implements Serializable, StatefulValue {

  /** The serial version UID */
  private static final long serialVersionUID = 8212168298890947089L;

  /** The boolean value */
  private final boolean     value;

  /**
   * Creates an instance for the given boolean <code>value</code>.
   */
  protected BooleanValue(boolean value) {
    this.value = value;
  }

  /**
   * Creates an instance for the given string representation of a boolean.
   * <p>
   * Calls {@link #toBoolean(String)} to convert the string to a boolean.
   */
  protected BooleanValue(String value) {
    this(toBoolean(value));
  }

  /**
   * Returns the boolean value represented by the string <code>value</code>.
   * <p>
   * This implementation uses the <code>Boolean.valueOf(String)</code> method
   * to convert the string to a boolean.
   */
  protected static boolean toBoolean(String value) {
    return Boolean.valueOf(value).booleanValue();
  }

  /**
   * Returns <code>PropertyType.BOOLEAN</code>.
   */
  public int getType() {
    return PropertyType.BOOLEAN;
  }

  /**
   * Returns the boolean value.
   */
  @Override
  public boolean getBoolean() {
    return value;
  }

  /**
   * Returns the boolean as a string converted by the
   * <code>Boolean.toString(boolean)</code>.
   */
  public String getString() {
    return Boolean.toString(value);
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
