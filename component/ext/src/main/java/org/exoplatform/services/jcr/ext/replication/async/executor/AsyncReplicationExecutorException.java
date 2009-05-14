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
package org.exoplatform.services.jcr.ext.replication.async.executor;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 29.01.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: AsyncReplicationExecutorException.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class AsyncReplicationExecutorException extends Exception {

  /**
   * AsyncReplicationExecutorException constructor.
   * 
   * @param message
   * @param cause
   */
  AsyncReplicationExecutorException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * AsyncReplicationExecutorException constructor.
   * 
   * @param message
   */
  AsyncReplicationExecutorException(String message) {
    super(message);
  }

}
