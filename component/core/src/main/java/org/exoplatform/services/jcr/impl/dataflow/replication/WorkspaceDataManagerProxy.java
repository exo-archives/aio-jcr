/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.replication;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemDataKeeper;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.impl.core.lock.LockManagerImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .<br/>
 * Proxy of WorkspaceDataManager for "proxy" mode of replication
 * to let replicator not to make persistent changes but replicate
 * cache, indexes etc instead. This is the case if persistent replication
 * is done with some external way (by repliucation enabled RDB or AS etc)    
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class WorkspaceDataManagerProxy implements ItemDataKeeper {

  protected static Log log = ExoLogger.getLogger("jcr.WorkspaceDataManagerProxy");

  private List <ItemsPersistenceListener> listeners;

  public WorkspaceDataManagerProxy(CacheableWorkspaceDataManager dataManager,
      SearchIndex searchIndex, LockManagerImpl lockManager) {
    this.listeners = new ArrayList <ItemsPersistenceListener>();
    listeners.add(dataManager.getCache());
    if(searchIndex != null)
      listeners.add(searchIndex);
    if(lockManager != null)
      listeners.add(lockManager);
    log.info("WorkspaceDataManagerProxy is instantiated");
  }
  
  /**
   * calls onSaveItems on all registered listeners
   * @param changesLog
   */
  public void save(ItemStateChangesLog changesLog) throws InvalidItemStateException,
  UnsupportedOperationException, RepositoryException {
    for(ItemsPersistenceListener listener:listeners) {
      listener.onSaveItems(changesLog);
    }
    if(log.isDebugEnabled()) 
      log.debug("ChangesLog sent to "+listeners);
  }
}
