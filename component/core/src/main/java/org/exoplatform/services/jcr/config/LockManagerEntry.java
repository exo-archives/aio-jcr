/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.config;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class LockManagerEntry {

  private long               timeout = -1;

  private LockPersisterEntry persister;

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public LockPersisterEntry getPersister() {  
    return persister;
  }

  public void setPersister(LockPersisterEntry persister) {
    this.persister = persister;
  }


}
