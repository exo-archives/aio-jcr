/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core.value;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

/**
 * <code>ExtendedValue</code> is a addition to the
 * {@link javax.jcr.Value Value} interface It is created for compatibility with
 * RMI interface with JSR specification.
 * 
 * @author Gennady Azarenkov
 * @version $Id: ExtendedValue.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface ExtendedValue extends Value {
  /**
   * Return order value in the list
   * 
   * @return order value in the list
   */
  public int getOrderNumber();

  /**
   * Set order value in the list
   * 
   * @return order value in the list
   */
  public void setOrderNumber(int orderNumber);

  /**
   * Return length of the value .
   * 
   * @return length
   */
  public long getLength();

  /**
   * Return the reference representation of the value
   * 
   * @return The reference
   * @throws ValueFormatException
   * @throws IllegalStateException
   * @throws RepositoryException
   */
  public String getReference() throws ValueFormatException, IllegalStateException,
      RepositoryException;
}
