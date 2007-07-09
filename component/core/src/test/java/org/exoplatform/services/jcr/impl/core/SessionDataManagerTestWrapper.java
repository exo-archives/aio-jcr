/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import org.exoplatform.services.jcr.impl.core.SessionDataManager.ItemReferencePool;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;

/**
 * Created by The eXo Platform SARL
 *
 * 04.12.2006
 *
 * For testing purpose
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: SessionDataManagerTestWrapper.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class SessionDataManagerTestWrapper {

  protected final SessionDataManager manager;
  
  public SessionDataManagerTestWrapper(SessionDataManager manager) {
    this.manager = manager;
  }
  
  public ItemReferencePool getItemsPool() {
    return this.manager.getItemsPool();
  }

  public SessionChangesLog getChangesLog() {
    return this.manager.getChangesLog();
  }
}
