/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.config;

/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id$
 */

/**
 * This exception is thrown when condition occurred
 */
public class RepositoryConfigurationException extends Exception {
  /**
   * Constructs an Exception without a message.
   */
  public RepositoryConfigurationException() {
    super();
  }

  /**
   * Constructs an Exception with a detailed message.
   *
   * @param Message The message associated with the exception.
   */
  public RepositoryConfigurationException(String message) {
    super(message);
  }
  
  public RepositoryConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
