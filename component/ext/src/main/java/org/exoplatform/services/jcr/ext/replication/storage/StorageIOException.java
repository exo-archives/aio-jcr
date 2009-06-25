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
package org.exoplatform.services.jcr.ext.replication.storage;

import java.io.IOException;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 28.01.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: CompositeIOException.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class StorageIOException extends IOException {

  /**
   * The base cause exception.
   */
  protected final Throwable cause;

  /**
   * StorageIOException  constructor.
   *
   * @param message
   *          String, the exception message
   */
  public StorageIOException(String message) {
    super(message);
    this.cause = null;
  }

  /**
   * StorageIOException  constructor.
   *
   * @param message
   *          String, the exception message
   * @param cause
   *          Throwable, the cause exception
   */
  public StorageIOException(String message, Throwable cause) {
    super(message);
    this.cause = cause;
  }

  /**
   * {@inheritDoc}
   */
  public Throwable getCause() {
    return cause;
  }

}
