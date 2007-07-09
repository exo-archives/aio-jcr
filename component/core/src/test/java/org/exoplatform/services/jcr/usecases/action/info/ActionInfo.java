/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases.action.info;

import javax.jcr.RepositoryException;

import org.apache.commons.chain.Context;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: ActionInfo.java 13421 2007-03-15 10:46:47Z geaz $
 */
public abstract class ActionInfo {
  public abstract int getEventType();
  public abstract void execute(Context ctx) throws RepositoryException; 
  public void tearDown(Context ctx) throws RepositoryException{

  }
}
