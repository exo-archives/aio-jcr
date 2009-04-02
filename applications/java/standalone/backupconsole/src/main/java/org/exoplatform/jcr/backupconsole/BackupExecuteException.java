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
package org.exoplatform.jcr.backupconsole;

/**
 * Internal backup client exception wrapper.
 * <p>
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: TransportException.java 111 2008-11-11 11:11:11Z serg $
 */
@SuppressWarnings("serial")
public class BackupExecuteException extends Exception {

  /**
   * Constructor.
   * 
   * @param message exception message.
   * @param cause exception cause.
   */
  public BackupExecuteException(String message, Throwable cause) {
    super(message, cause);
  }
  
  /**
   * Constructor.
   * 
   * @param message exception message.
   */
  public BackupExecuteException(String message) {
    super(message);
  }
  
}
