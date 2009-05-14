/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.storage;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: IteratorRuntimeException.java 111 2008-11-11 11:11:11Z serg $
 */
public class ItemStateReadException extends RuntimeException {

  /**
   * Constructs a new runtime exception with <code>null</code> as its detail message. The cause is
   * not initialized, and may subsequently be initialized by a call to {@link #initCause}.
   */
  public ItemStateReadException() {
    super();
  }

  /**
   * Constructs a new runtime exception with the specified detail message. The cause is not
   * initialized, and may subsequently be initialized by a call to {@link #initCause}.
   * 
   * @param message
   *          the detail message. The detail message is saved for later retrieval by the
   *          {@link #getMessage()} method.
   */
  public ItemStateReadException(String message) {
    super(message);
  }

}
