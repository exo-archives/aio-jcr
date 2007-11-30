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
