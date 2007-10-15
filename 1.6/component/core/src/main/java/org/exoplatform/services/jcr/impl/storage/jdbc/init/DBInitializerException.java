/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.storage.jdbc.init;


/**
 * Created by The eXo Platform SARL
 *
 * 12.03.2007
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: DBInitializerException.java 13867 2007-03-28 13:43:08Z peterit $
 */
public class DBInitializerException extends Exception {

  public DBInitializerException(String message, Throwable e) {
    super(message, e);
  }
  
}
