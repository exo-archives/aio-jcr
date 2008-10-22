/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.core.lock;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;

import org.picocontainer.Startable;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ExtendedSession;
import org.exoplatform.services.jcr.core.SessionLifecycleListener;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.CompositeChangesLog;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.AbstractValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientItemData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: LockManagerImpl.java 12096 2008-03-19 11:42:40Z gazarenkov $
 */

public class LockManagerImpl implements ItemsPersistenceListener, SessionLifecycleListener,
    LockManager, Startable {
  /**
   * Default lock time out. 30min
   */
  public static final long            DEFAULT_LOCK_TIMEOUT = 1000 * 60 * 30;

  // Search constants
  /**
   * The exact lock token.
   */
  private static final int            SEARCH_EXECMATCH     = 1;

  /**
   * Lock token of closed parent
   */
  private static final int            SEARCH_CLOSEDPARENT  = 2;

  /**
   * Lock token of closed child
   */
  private static final int            SEARCH_CLOSEDCHILD   = 4;

  /**
   * Logger
   */
  private final Log                   log                  = ExoLogger.getLogger("jcr.lock.LockManager");

  /**
   * Map NodeIdentifier -- lockData
   */
  private final Map<String, LockData> locks;

  /**
   * Data manager.
   */
  private final DataManager           dataManager;

  /**
   * Map NodeIdentifier -- lockData
   */
  private final Map<String, LockData> pendingLocks;

  /**
   * Map lockToken --lockData
   */
  private final Map<String, LockData> tokensMap;

  /**
   * Run time lock time out.
   */
  private long                        lockTimeOut;

  /**
   * Lock remover thread.
   */
  private LockRemover                 lockRemover;

  /**
   * Lock persister instance.
   */
  private final LockPersister         persister;

  /**
   * Constructor for workspace without LockPersister
   * 
   * @param dataManager
   * @param config
   */
  public LockManagerImpl(WorkspacePersistentDataManager dataManager, WorkspaceEntry config) {
    this(dataManager, config, null);
  }

  public LockManagerImpl(WorkspacePersistentDataManager dataManager,
                         WorkspaceEntry config,
                         LockPersister persister) {

    this.dataManager = dataManager;
    this.persister = persister;
    if (config.getLockManager() != null) {
      lockTimeOut = config.getLockManager().getTimeout() > 0
          ? config.getLockManager().getTimeout()
          : DEFAULT_LOCK_TIMEOUT;
    } else
       lockTimeOut = DEFAULT_LOCK_TIMEOUT;

    locks = new HashMap<String, LockData>();
    pendingLocks = new HashMap<String, LockData>();
    tokensMap = new HashMap<String, LockData>();

    dataManager.addItemPersistenceListener(this);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.impl.core.lock.LockManager#lockTokenAdded(org.exoplatform.services
   * .jcr.impl.core.SessionImpl, java.lang.String)
   */
  public synchronized void addLockToken(String sessionId, String lt) {
    LockData currLock = tokensMap.get(lt);
    if (currLock != null) {
      currLock.addLockHolder(sessionId);
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.impl.core.lock.LockManager#addPendingLock(org.exoplatform.services
   * .jcr.impl.core.NodeImpl, boolean, boolean, long)
   */
  public synchronized Lock addPendingLock(NodeImpl node,
                                          boolean isDeep,
                                          boolean isSessionScoped,
                                          long timeOut) throws LockException {
    LockData lData = getLockData((NodeData) node.getData(), SEARCH_EXECMATCH | SEARCH_CLOSEDPARENT);
    if (lData != null) {
      if (lData.getNodeIdentifier().equals(node.getInternalIdentifier())) {
        throw new LockException("Node already locked: " + node.getData().getQPath());
      } else if (lData.isDeep()) {
        throw new LockException("Parent node has deep lock.");
      }
    }

    if (isDeep && getLockData((NodeData) node.getData(), SEARCH_CLOSEDCHILD) != null) {
      throw new LockException("Some child node is locked.");
    }

    String lockToken = IdGenerator.generate();
    lData = new LockData(node.getInternalIdentifier(),
                         lockToken,
                         isDeep,
                         isSessionScoped,
                         node.getSession().getUserID(),
                         timeOut > 0 ? timeOut : lockTimeOut);

    lData.addLockHolder(node.getSession().getId());
    pendingLocks.put(node.getInternalIdentifier(), lData);
    tokensMap.put(lockToken, lData);

    LockImpl lock = new LockImpl(node.getSession(), lData);
    return lock;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.impl.core.lock.LockManager#getLock(org.exoplatform.services.jcr
   * .impl.core.NodeImpl)
   */
  public LockImpl getLock(NodeImpl node) throws LockException, RepositoryException {

    LockData lData = getLockData((NodeData) node.getData(), SEARCH_EXECMATCH | SEARCH_CLOSEDPARENT);

    if (lData == null
        || (!node.getInternalIdentifier().equals(lData.getNodeIdentifier()) && !lData.isDeep())) {
      throw new LockException("Node not locked: " + node.getData().getQPath());

    }
    return new LockImpl(node.getSession(), lData);
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.lock.LockManager#getLockTokens(java.lang.String)
   */
  public synchronized String[] getLockTokens(String sessionID) {
    List<String> retval = new ArrayList<String>();

    for (LockData lockData : locks.values()) {
      if (lockData.isLockHolder(sessionID))
        retval.add(lockData.getLockToken(sessionID));

    }
    return retval.toArray(new String[retval.size()]);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.impl.core.lock.LockManager#holdsLock(org.exoplatform.services.
   * jcr.impl.core.NodeImpl)
   */
  public boolean holdsLock(NodeData node) throws RepositoryException {
    return getLockData(node, SEARCH_EXECMATCH) != null;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.impl.core.lock.LockManager#isLocked(org.exoplatform.services.jcr
   * .datamodel.NodeData)
   */
  public boolean isLocked(NodeData node) {
    LockData lData = getLockData(node, SEARCH_EXECMATCH | SEARCH_CLOSEDPARENT);

    if (lData == null
        || (!node.getIdentifier().equals(lData.getNodeIdentifier()) && !lData.isDeep())) {
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.impl.core.lock.LockManager#isLockHolder(org.exoplatform.services
   * .jcr.impl.core.NodeImpl)
   */
  public boolean isLockHolder(NodeImpl node) throws RepositoryException {
    LockData lData = getLockData((NodeData) node.getData(), SEARCH_EXECMATCH | SEARCH_CLOSEDPARENT);
    return lData != null && lData.isLockHolder(node.getSession().getId());
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.core.SessionLifecycleListener#onCloseSession(org.exoplatform.services
   * .jcr.core.ExtendedSession)
   */
  public synchronized void onCloseSession(ExtendedSession session) {
    // List<String> deadLocksList = new ArrayList<String>();
    SessionImpl sessionImpl = (SessionImpl) session;
    for (Iterator<Map.Entry<String, LockData>> entries = locks.entrySet().iterator(); entries.hasNext();) {
      Map.Entry<String, LockData> entry = entries.next();
      LockData lockData = entry.getValue();
      if (lockData.isLive()) {
        if (lockData.isLockHolder(session.getId())) {
          if (lockData.isSessionScoped()) {
            // if no session currently holds lock except this
            try {
              ((NodeImpl) sessionImpl.getTransientNodesManager()
                                     .getItemByIdentifier(lockData.getNodeIdentifier(), false)).unlock();
            } catch (UnsupportedRepositoryOperationException e) {
              log.error(e.getLocalizedMessage());
            } catch (LockException e) {
              log.error(e.getLocalizedMessage());
            } catch (AccessDeniedException e) {
              log.error(e.getLocalizedMessage());
            } catch (RepositoryException e) {
              log.error(e.getLocalizedMessage());
            }

          } else {
            lockData.removeLockHolder(session.getId());
          }
        }
      } else {
        entries.remove();
      }
    }
  }

  /*
   * (non-Javadoc)
   * @seeorg.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener#onSaveItems(org.
   * exoplatform.services.jcr.dataflow.ItemStateChangesLog)
   */
  public void onSaveItems(ItemStateChangesLog changesLog) {
    List<PlainChangesLog> chengesLogList = new ArrayList<PlainChangesLog>();
    if (changesLog instanceof TransactionChangesLog) {
      ChangesLogIterator logIterator = ((TransactionChangesLog) changesLog).getLogIterator();

      while (logIterator.hasNextLog()) {
        chengesLogList.add(logIterator.nextLog());
      }
    } else if (changesLog instanceof PlainChangesLog) {
      chengesLogList.add((PlainChangesLog) changesLog);
    } else if (changesLog instanceof CompositeChangesLog) {
      for (ChangesLogIterator iter = ((CompositeChangesLog) changesLog).getLogIterator(); iter.hasNextLog();) {
        chengesLogList.add(iter.nextLog());
      }
    }

    for (PlainChangesLog currChangesLog : chengesLogList) {
      String nodeIdentifier;
      try {
        switch (currChangesLog.getEventType()) {
        case ExtendedEvent.LOCK:
          if (currChangesLog.getSize() < 2) {
            log.error("Incorrect changes log  of type ExtendedEvent.LOCK size="
                + currChangesLog.getSize() + "<2 \n" + currChangesLog.dump());
            break;
          }
          nodeIdentifier = currChangesLog.getAllStates().get(0).getData().getParentIdentifier();

          if (pendingLocks.containsKey(nodeIdentifier)) {
            internalLock(nodeIdentifier);
          } else {
            log.warn("No lock in pendingLocks for identifier " + nodeIdentifier
                + " Probably lock come from replication.");

            String lockToken = IdGenerator.generate();
            ItemState ownerState = getItemState(currChangesLog, Constants.JCR_LOCKOWNER);
            ItemState isDeepState = getItemState(currChangesLog, Constants.JCR_LOCKISDEEP);
            if (ownerState != null && isDeepState != null) {

              String owner = new String(((((TransientPropertyData) (ownerState.getData())).getValues()).get(0)).getAsByteArray(),
                                        Constants.DEFAULT_ENCODING);

              boolean isDeep = Boolean.valueOf(new String(((((TransientPropertyData) (isDeepState.getData())).getValues()).get(0)).getAsByteArray(),
                                                          Constants.DEFAULT_ENCODING))
                                      .booleanValue();

              createRemoteLock(currChangesLog.getSessionId(),
                               nodeIdentifier,
                               lockToken,
                               isDeep,
                               false,
                               owner);
            }
          }
          break;
        case ExtendedEvent.UNLOCK:
          if (currChangesLog.getSize() < 2) {
            log.error("Incorrect changes log  of type ExtendedEvent.UNLOCK size="
                + currChangesLog.getSize() + "<2 \n" + currChangesLog.dump());
            break;
          }

          internalUnLock(currChangesLog.getSessionId(), currChangesLog.getAllStates()
                                                                      .get(0)
                                                                      .getData()
                                                                      .getParentIdentifier());
          break;
        default:
          HashSet<String> removedLock = new HashSet<String>();
          for (ItemState itemState : currChangesLog.getAllStates()) {
            // this is a node and node is locked
            if (itemState.getData().isNode()
                && locks.containsKey(itemState.getData().getIdentifier())) {
              nodeIdentifier = itemState.getData().getIdentifier();
              if (itemState.isDeleted()) {
                removedLock.add(nodeIdentifier);
              } else if (itemState.isAdded() || itemState.isRenamed() || itemState.isUpdated()) {
                removedLock.remove(nodeIdentifier);
              }
            }
          }
          for (String identifier : removedLock) {
            internalUnLock(currChangesLog.getSessionId(), identifier);
          }
          break;
        }
      } catch (LockException e) {
        log.error(e.getLocalizedMessage(), e);
      } catch (UnsupportedEncodingException e) {
        log.error(e.getLocalizedMessage(), e);
      } catch (IllegalStateException e) {
        log.error(e.getLocalizedMessage(), e);
      } catch (IOException e) {
        log.error(e.getLocalizedMessage(), e);
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.impl.core.lock.LockManager#lockTokenRemoved(org.exoplatform.services
   * .jcr.impl.core.SessionImpl, java.lang.String)
   */
  public synchronized void removeLockToken(String sessionId, String lt) {
    LockData lData = tokensMap.get(lt);
    if (lData != null && lData.isLockHolder(sessionId)) {
      lData.removeLockHolder(sessionId);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  public void start() {
    lockRemover = new LockRemover(this);
  }

  // Quick method. We need to reconstruct
  synchronized List<LockData> getLockList() {
    return new ArrayList<LockData>(locks.values());
  }

  /**
   * Remove expired locks. Used from LockRemover.
   */
  synchronized void removeExpired() {
    final List<String> removeLockList = new ArrayList<String>();

    for (LockData lock : locks.values()) {
      if (!lock.isSessionScoped() && lock.getTimeToDeath() < 0) {
        removeLockList.add(lock.getNodeIdentifier());
      }
    }

    for (String rLock : removeLockList) {
      removeLock(rLock);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {
    lockRemover.halt();
    lockRemover.interrupt();
    locks.clear();
    pendingLocks.clear();
    tokensMap.clear();
  }

  /**
   * Copy <code>PropertyData prop<code> to new TransientItemData
   * 
   * @param prop
   * @return
   * @throws RepositoryException
   */
  private TransientItemData copyItemData(PropertyData prop) throws RepositoryException {

    if (prop == null)
      return null;

    // make a copy
    TransientPropertyData newData = new TransientPropertyData(prop.getQPath(),
                                                              prop.getIdentifier(),
                                                              prop.getPersistedVersion(),
                                                              prop.getType(),
                                                              prop.getParentIdentifier(),
                                                              prop.isMultiValued());

    List<ValueData> values = null;
    // null is possible for deleting items
    if (prop.getValues() != null) {
      values = new ArrayList<ValueData>();
      for (ValueData val : prop.getValues()) {
        values.add(((AbstractValueData) val).createTransientCopy());
      }
    }
    newData.setValues(values);
    return newData;
  }

  /**
   * Search item with name <code>itemName<code> in changesLog
   * 
   * @param changesLog
   * @param itemName
   * @return Item
   */
  private ItemState getItemState(PlainChangesLog changesLog, InternalQName itemName) {
    List<ItemState> allStates = changesLog.getAllStates();
    for (int i = allStates.size() - 1; i >= 0; i--) {
      ItemState state = allStates.get(i);
      if (state.getData().getQPath().getName().equals(itemName))
        return state;
    }
    return null;
  }

  /**
   * Search lock in maps.
   * 
   * @param data
   * @param searchType
   * @return
   */
  private LockData getLockData(NodeData data, int searchType) {
    if (data == null || locks.size() == 0)
      return null;
    LockData retval = null;
    try {
      if ((searchType & SEARCH_EXECMATCH) != 0) {
        retval = locks.get(data.getIdentifier());
      }
      if (retval == null && (searchType & SEARCH_CLOSEDPARENT) != 0) {

        NodeData parentData = (NodeData) dataManager.getItemData(data.getParentIdentifier());
        if (parentData != null) {
          retval = locks.get(parentData.getIdentifier());
          // parent not found try to fo upper
          if (retval == null) {
            retval = getLockData(parentData, SEARCH_CLOSEDPARENT);
          }
        }
      }
      if (retval == null && (searchType & SEARCH_CLOSEDCHILD) != 0) {

        List<NodeData> childData = dataManager.getChildNodesData(data);
        for (NodeData nodeData : childData) {
          retval = locks.get(nodeData.getIdentifier());
          if (retval != null)
            break;
        }
        if (retval == null) {
          // child not found try to find diper
          for (NodeData nodeData : childData) {
            retval = getLockData(nodeData, SEARCH_CLOSEDCHILD);
            if (retval != null)
              break;
          }
        }
      }
    } catch (RepositoryException e) {
      return null;
    }

    return retval;
  }

  /**
   * Internal lock
   * 
   * @param nodeIdentifier
   * @throws LockException
   */
  private synchronized void internalLock(String nodeIdentifier) throws LockException {
    LockData ldata = pendingLocks.get(nodeIdentifier);
    if (ldata != null) {
      locks.put(nodeIdentifier, ldata);

      if (persister != null) {
        persister.add(ldata);
      }
      pendingLocks.remove(nodeIdentifier);
    } else {
      throw new LockException("No lock in pending locks");
    }
  }

  /**
   * Internal unlock.
   * 
   * @param sessionId
   * @param nodeIdentifier
   * @throws LockException
   */
  private synchronized void internalUnLock(String sessionId, String nodeIdentifier) throws LockException {
    LockData lData = locks.get(nodeIdentifier);

    // TODO [AR] JCR-474
    // if (lData == null) {
    // throw new LockException("Node with id " + nodeIdentifier + " not locked");
    // }

    if (lData != null) {

      tokensMap.remove(lData.getLockToken(sessionId));
      locks.remove(nodeIdentifier);

      lData.setLive(false);
      if (persister != null) {
        persister.remove(lData);
      }
      lData = null;
    }
  }

  /**
   * For locks comes from remote JCRs (replication usecase)
   * 
   * @param sessionId
   * @param nodeIdentifier
   * @param lockToken
   * @param isDeep
   * @param sessionScoped
   * @param owner
   * @return
   */
  private synchronized LockData createRemoteLock(String sessionId,
                                                 String nodeIdentifier,
                                                 String lockToken,
                                                 boolean isDeep,
                                                 boolean sessionScoped,
                                                 String owner) {
    LockData lData = new LockData(nodeIdentifier,
                                  lockToken,
                                  isDeep,
                                  sessionScoped,
                                  owner,
                                  lockTimeOut);
    lData.addLockHolder(sessionId);
    locks.put(nodeIdentifier, lData);
    tokensMap.put(lockToken, lData);

    return lData;
  }

  /**
   * Remove lock, used by Lock remover.
   * 
   * @param nodeIdentifier
   */
  protected void removeLock(String nodeIdentifier) {
    // if (locks.containsKey(nodeIdentifier)) {
    try {
      NodeData nData = (NodeData) dataManager.getItemData(nodeIdentifier);
      PlainChangesLog changesLog = new PlainChangesLogImpl(new ArrayList<ItemState>(),
                                                           SystemIdentity.SYSTEM,
                                                           ExtendedEvent.UNLOCK);

      ItemData lockOwner = copyItemData((PropertyData) dataManager.getItemData(nData,
                                                                               new QPathEntry(Constants.JCR_LOCKOWNER,
                                                                                              1)));

      changesLog.add(ItemState.createDeletedState(lockOwner));

      ItemData lockIsDeep = copyItemData((PropertyData) dataManager.getItemData(nData,
                                                                                new QPathEntry(Constants.JCR_LOCKISDEEP,
                                                                                               1)));
      changesLog.add(ItemState.createDeletedState(lockIsDeep));

      // lock probably removed by other thread
      if (lockOwner == null && lockIsDeep == null)
        return;
      dataManager.save(new TransactionChangesLog(changesLog));
    } catch (RepositoryException e) {
      log.error("Error occur during removing lock" + e.getLocalizedMessage());
    }
    // }
  }

}
