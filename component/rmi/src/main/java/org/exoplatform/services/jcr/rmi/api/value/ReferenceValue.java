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
 * The <code>ReferenceValue</code> class implements the committed value state
 * for Reference values as a part of the State design pattern (Gof) used by this
 * package.
 * 
 * @since 0.16.4.1
 */
public class ReferenceValue extends BaseNonStreamValue implements Serializable, StatefulValue {

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.rmi.api.value.BaseNonStreamValue#getReference()
   */
  @Override
  public String getReference() throws ValueFormatException, IllegalStateException,
      RepositoryException {
    // TODO Auto-generated method stub
    return getString();
  }

  /** The serial version UID */
  private static final long serialVersionUID = -3160494922729580458L;

  /** The reference value */
  private final String      value;

  /**
   * Creates an instance for the given reference <code>value</code>.
   */
  protected ReferenceValue(String value) throws ValueFormatException {
    this.value = toReference(value);
  }

  /**
   * Checks whether the string value adheres to the reference syntax.
   * 
   * @param value The string to check for synthactical compliance with a
   *          reference value.
   * @return The input value.
   * @throws ValueFormatException if the string <code>value</code> is not a
   *           synthactically correct reference.
   */
  protected static String toReference(String value) throws ValueFormatException {
    // TODO: check syntax
    return value;
  }

  /**
   * Returns <code>PropertyType.REFERENCE</code>.
   */
  public int getType() {
    return PropertyType.REFERENCE;
  }

  /**
   * Returns the string representation of the reference value.
   */
  public String getString() throws ValueFormatException, RepositoryException {
    return value;
  }

  public long getLength() {
    String str = null;

    try {
      str = getString();
    } catch (ValueFormatException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (RepositoryException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    if (null != str) {
      return str.length();
    }
    return 0;
  }

  public int getOrderNumber() {
    // TODO Auto-generated method stub
    return 0;
  }
}
