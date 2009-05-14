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
package org.exoplatform.services.jcr.ext.initializer;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 24.03.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: NoMemberToSendException.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class NoMemberToSendException extends Exception {

  /**
   * NoMemberToSendException constructor.
   * 
   * @param message
   *          the exception message
   * @param cause
   *          the cause exception
   */
  public NoMemberToSendException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * NoMemberToSendException constructor.
   * 
   * @param message
   *          the exception message
   */
  public NoMemberToSendException(String message) {
    super(message);
  }
}
