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
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

/**
 * The <code>NameValue</code> class implements the committed value state for
 * Name values as a part of the State design pattern (Gof) used by this package.
 * 
 * @since 0.16.4.1
 */
public class NameValue extends BaseNonStreamValue implements Serializable, StatefulValue {

  /** The serial version UID */
  private static final long serialVersionUID = -2165655071495230179L;

  /** The name value. */
  private final String      value;

  /**
   * Creates an instance for the given name <code>value</code>.
   */
  protected NameValue(String value) throws ValueFormatException {
    this.value = toName(value);
  }

  /**
   * Checks whether the string value adheres to the name syntax.
   * 
   * @param value The string to check for synthactical compliance with a name
   *          value.
   * @return The input value.
   * @throws ValueFormatException if the string <code>value</code> is not a
   *           synthactically correct name.
   */
  protected static String toName(String value) throws ValueFormatException {
    // TODO: check syntax
    return value;
  }

  /**
   * Returns <code>PropertyType.NAME</code>.
   */
  public int getType() {
    return PropertyType.NAME;
  }

  /**
   * Returns the string representation of the Name value.
   */
  public String getString() throws ValueFormatException, RepositoryException {
    return value;
  }

  public long getLength() {
    // TODO Auto-generated method stub
    try {
      return getString().length();
    } catch (ValueFormatException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RepositoryException e) {
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
