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
package org.exoplatform.services.jcr.ext.backup.server.bean.response;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 27.03.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: FailureBeen.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class FailureBean {

  /**
   * The failure message.
   */
  private String message;
  /**
   * The message form exception.
   */
  private String exceptionMessage;

  /**
   * FailureBeen  constructor.
   *
   */
  public FailureBean() {
  }
  
  /**
   * FailureBeen  constructor.
   *
   * @param message
   *          the failure message
   * @param t
   *          the exception
   */
  public FailureBean(String message, Throwable t) {
    this.message = message;
    this.exceptionMessage = t.getMessage();
  }

  /**
   * getMessage.
   *
   * @return String
   *           return the failure message
   */
  public String getMessage() {
    return message;
  }

  /**
   * setMessage.
   *
   * @param message
   *          String, the failure message
   */
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * getExceptionMessage.
   *
   * @return String
   *           return the message from exception
   */
  public String getExceptionMessage() {
    return exceptionMessage;
  }

  /**
   * setExceptionMessage.
   *
   * @param exceptionMessage
   *          String, the message from exception
   */
  public void setExceptionMessage(String exceptionMessage) {
    this.exceptionMessage = exceptionMessage;
  }
  
}
