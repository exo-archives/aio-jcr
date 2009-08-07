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

package org.exoplatform.services.jcr.core;

import java.io.InputStream;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface ExtendedProperty extends Property {

  /**
   * Write binary data portion to the property value data.
   * 
   * @param index
   *          - value index, 0 for first-in-multivalue/single-value, 1 - second etc.
   * @param value
   *          - stream with the data portion
   * @param length
   *          - value bytes count will be written
   * @param position
   *          - position in the property value data from which the value will be written
   */
  void updateValue(int index, InputStream value, long length, long position) throws ValueFormatException,
                                                                            VersionException,
                                                                            LockException,
                                                                            ConstraintViolationException,
                                                                            RepositoryException;

}
