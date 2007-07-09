/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.value;

import java.io.Serializable;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

/**
 * The <code>PathValue</code> class implements the committed value state for
 * Path values as a part of the State design pattern (Gof) used by this package.
 * 
 * @since 0.16.4.1
 */
public class PathValue extends BaseNonStreamValue implements Serializable, StatefulValue {

  /** The serial version UID */
  private static final long serialVersionUID = 961536566073928748L;

  /** The path value. */
  private final String      value;

  /**
   * Creates an instance for the given path <code>value</code>.
   */
  protected PathValue(String value) throws ValueFormatException {
    this.value = toPath(value);
  }

  /**
   * Checks whether the string value adheres to the path syntax.
   * 
   * @param value The string to check for synthactical compliance with a path
   *          value.
   * @return The input value.
   * @throws ValueFormatException if the string <code>value</code> is not a
   *           synthactically correct path.
   */
  protected static String toPath(String value) throws ValueFormatException {
    // TODO: check syntax
    return value;
  }

  /**
   * Returns <code>PropertyType.PATH</code>.
   */
  public int getType() {
    return PropertyType.PATH;
  }

  /**
   * Returns the string representation of the path value.
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
