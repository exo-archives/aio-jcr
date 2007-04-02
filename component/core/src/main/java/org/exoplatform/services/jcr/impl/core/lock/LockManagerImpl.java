/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.lock;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.WeakHashMap;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.CompositeChangesLog;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.SessionLifecycleListener;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.impl.proccess.WorkerThread;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.services.jcr.util.UUIDGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: LockManagerImpl.java 13866 2007-03-28 13:39:28Z ksm $
 */

public class LockManagerImpl implements ItemsPersistenceListener, SessionLifecycleListener,
    LockManager, Startable {
  // 30 min
  private static final long             DEFAULT_TIMEOUT     = 1800;                           // sec

  private static final int              SEARCH_EXECMATCH    = 1;

  private static final int              SEARCH_CLOSEDPARENT = 2;

  private static final int              SEARCH_CLOSEDCHILD  = 4;

  private static Log                    log                 = ExoLogger
                                                                .getLogger("jcr.LockManager");

  // NodeUuid -- lockData
  private WeakHashMap<String, LockData> locks;

  private final DataManager             dataManager;

  // NodeUuid -- lockData
  private WeakHashMap<String, LockData> pendingLocks;

  // lockToken --lockData
  private WeakHashMap<String, LockData> tokensMap;

  // private final RepositoryEntry config;

  private long                          lockTimeOut;

  private LockRemover                   lockRemover;

  public LockManagerImpl(WorkspacePersistentDataManager dataManager, RepositoryEntry config) {
    this.dataManager = dataManager;

    lockTimeOut = config.getLockTimeOut() > 0 ? config.getLockTimeOut() : DEFAULT_TIMEOUT;

    locks = new WeakHashMap<String, LockData>();
    pendingLocks = new WeakHashMap<String, LockData>();
    tokensMap = new WeakHashMap<String, LockData>();

    dataManager.addItemPersistenceListener(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.core.lock.LockManager#lockTokenAdded(org.exoplatform.services.jcr.impl.core.SessionImpl,
   *      java.lang.String)
   */
  public synchronized void addLockToken(String sessionId, String lt) {
    LockData currLock = tokensMap.get(lt);
    if (currLock != null) {
      currLock.addLockHolder(sessionId);
    }
  }

  public synchronized Lock addPendingLock(NodeImpl node,
      boolean isDeep,
      boolean isSessionScoped,
      long timeOut) throws LockException {
    LockData lData = getLockData((NodeData) node.getData(), SEARCH_EXECMATCH | SEARCH_CLOSEDPARENT);
    if (lData != null) {
      if (lData.getNodeUuid().equals(node.getInternalUUID())) {
        throw new LockException("Node already locked: " + node.getData().getQPath());
      } else if (lData.isDeep()) {
        throw new LockException("Parent node has deep lock.");
      }
    }

    if (isDeep && getLockData((NodeData) node.getData(), SEARCH_CLOSEDCHILD) != null) {
      throw new LockException("Some child node is locked.");
    }

    String lockToken = UUIDGenerator.generate();
    lData = new LockData(node.getInternalUUID(), lockToken, isDeep, isSessionScoped, node
        .getSession().getUserID(), timeOut > 0 ? timeOut : lockTimeOut);

    lData.addLockHolder(node.getSession().getId());
    pendingLocks.put(node.getInternalUUID(), lData);
    tokensMap.put(lockToken, lData);

    LockImpl lock = new LockImpl(node.getSession(), lData);
    return lock;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.core.lock.LockManager#getLock(org.exoplatform.services.jcr.impl.core.NodeImpl)
   */
  public LockImpl getLock(NodeImpl node) throws LockException, RepositoryException {

    LockData lData = getLockData((NodeData) node.getData(), SEARCH_EXECMATCH | SEARCH_CLOSEDPARENT);

    if (lData == null || (!node.getInternalUUID().equals(lData.getNodeUuid()) && !lData.isDeep())) {
      throw new LockException("Node not locked: " + node.getData().getQPath());

    }
    return new LockImpl(node.getSession(), lData);
  }

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
   * 
   * @see org.exoplatform.services.jcr.impl.core.lock.LockManager#holdsLock(org.exoplatform.services.jcr.impl.core.NodeImpl)
   */
  public boolean holdsLock(NodeData node) throws RepositoryException {
    return getLockData(node, SEARCH_EXECMATCH) != null;
  }

  public boolean isLocked(NodeData node) {
    LockData lData = getLockData(node, SEARCH_EXECMATCH | SEARCH_CLOSEDPARENT);
    if (lData == null || (!node.getUUID().equals(lData.getNodeUuid()) && !lData.isDeep())) {
      return false;
    }
    return true;
  }

  public boolean isLockHolder(NodeImpl node) throws RepositoryException {
    LockData lData = getLockData((NodeData) node.getData(), SEARCH_EXECMATCH | SEARCH_CLOSEDPARENT);
    // getLockData((NodeData) node.getData(), false);
    return lData != null && lData.isLockHolder(node.getSession().getId());
  }

  public synchronized void onCloseSession(SessionImpl session) {
    List<String> deadLocksList = new ArrayList<String>();
    for (String key : locks.keySet()) {
      LockData lockData = locks.get(key);
      if (lockData.isLive()) {

        if (lockData.isLockHolder(session.getId())) {
          if (lockData.isSessionScoped()) {
            // if no session currently holds lock except this
            if (lockData.getLockHolderSize() == 1) {
              try {
                ((NodeImpl) session.getTransientNodesManager()
                    .getItemByUUID(lockData.getNodeUuid(), true)).unlock();
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
          } else {
            lockData.removeLockHolder(session.getId());
          }
        }
      } else {
        deadLocksList.add(key);
      }
    }
    // possibly this is a unnecessary cod
    for (String deadkey : deadLocksList) {
      locks.remove(deadkey);
    }
  }

  public void onSaveItems(ItemStateChangesLog changesLog) {
    List<PlainChangesLog> chengesLogList = new ArrayList<PlainChangesLog>();
    if (changesLog instanceof PlainChangesLog) {
      chengesLogList.add((PlainChangesLog) changesLog);
    } else if (changesLog instanceof CompositeChangesLog) {
      for (ChangesLogIterator iter = ((CompositeChangesLog) changesLog).getLogIterator(); iter
          .hasNextLog();) {
        chengesLogList.add(iter.nextLog());
      }
    }
    
    for (PlainChangesLog currChangesLog : chengesLogList) {
      String nodeUuid;
      try {
        switch (currChangesLog.getEventType()) {
        case ExtendedEvent.LOCK:
          nodeUuid = currChangesLog.getAllStates().get(0).getData().getParentUUID();
          if (pendingLocks.containsKey(nodeUuid)) {
            internalLock(nodeUuid);
          } else {
            log.warn("No lock in pendingLocks for uuid " + nodeUuid
                + " Probably lock come from replication.");

            String lockToken = UUIDGenerator.generate();
            ItemState ownerState = getItemState(currChangesLog, Constants.JCR_LOCKOWNER);
            ItemState isDeepState = getItemState(currChangesLog, Constants.JCR_LOCKISDEEP);
            if (ownerState != null && isDeepState != null) {

              String owner = new String(((((TransientPropertyData) (ownerState.getData()))
                  .getValues()).get(0)).getAsByteArray(), Constants.DEFAULT_ENCODING);

              boolean isDeep = Boolean.valueOf(new String(((((TransientPropertyData) (isDeepState
                  .getData())).getValues()).get(0)).getAsByteArray(), Constants.DEFAULT_ENCODING))
                  .booleanValue();
              LockData lData = new LockData(nodeUuid, lockToken, isDeep, false, owner, lockTimeOut);
              lData.addLockHolder(currChangesLog.getSessionId());
              locks.put(nodeUuid, lData);
              tokensMap.put(lockToken, lData);
            }
          }
          break;
        case ExtendedEvent.UNLOCK:

          internalUnLock(currChangesLog.getSessionId(), currChangesLog.getAllStates().get(0)
              .getData().getParentUUID());
          break;
        default:
          HashSet<String> removedLock = new HashSet<String>();
          for (ItemState itemState : currChangesLog.getAllStates()) {

            // this is a node and node is locked
            if (itemState.getData().isNode() && locks.containsKey(itemState.getData().getUUID())) {
              nodeUuid = itemState.getData().getUUID();
              if (itemState.isDeleted()) {
                removedLock.add(nodeUuid);
              } else if (itemState.isAdded()) {
                removedLock.remove(nodeUuid);
              }
            }
          }
          for (String uuid : removedLock) {
            internalUnLock(currChangesLog.getSessionId(), uuid);
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
   * 
   * @see org.exoplatform.services.jcr.impl.core.lock.LockManager#lockTokenRemoved(org.exoplatform.services.jcr.impl.core.SessionImpl,
   *      java.lang.String)
   */
  public synchronized void removeLockToken(String sessionId, String lt) {
    LockData lData = tokensMap.get(lt);
    if (lData != null && lData.isLockHolder(sessionId)) {
      lData.removeLockHolder(sessionId);
    }
  }

  private synchronized LockData getLockData(NodeData data, int searchType) {
    if (data == null)
      return null;
    LockData retval = null;
    try {
      if ((searchType & SEARCH_EXECMATCH) != 0) {
        retval = locks.get(data.getUUID());
      }
      if (retval == null && (searchType & SEARCH_CLOSEDPARENT) != 0) {

        NodeData parentData = (NodeData) dataManager.getItemData(data.getParentUUID());
        if (parentData != null) {
          retval = locks.get(parentData.getUUID());
          // parent not found try to fo upper
          if (retval == null) {
            retval = getLockData(parentData, SEARCH_CLOSEDPARENT);
          }
        }
      }
      if (retval == null && (searchType & SEARCH_CLOSEDCHILD) != 0) {

        List<NodeData> childData = dataManager.getChildNodesData(data);
        for (NodeData nodeData : childData) {
          retval = locks.get(nodeData.getUUID());
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

  private synchronized void internalLock(String nodeUuid) throws LockException {
    LockData ldata = pendingLocks.get(nodeUuid);
    if (ldata != null) {
      locks.put(nodeUuid, ldata);
      pendingLocks.remove(nodeUuid);
    } else {
      throw new LockException("No lock in pending locks");
    }
  }

  private ItemState getItemState(PlainChangesLog changesLog, InternalQName itemName) {
    List<ItemState> allStates = changesLog.getAllStates();
    for (int i = allStates.size() - 1; i >= 0; i--) {
      ItemState state = allStates.get(i);
      if (!state.isOrderable() && state.getData().getQPath().getName().equals(itemName))
        return state;
    }
    return null;
  }

  private synchronized void internalUnLock(String sessionId, String nodeUuid) throws LockException {
    LockData lData = locks.get(nodeUuid);

    if (lData == null) {
      throw new LockException("Node with uuid " + nodeUuid + " not locked");
    }
    NodeData parentNode = null;
    try {
      NodeData node = (NodeData) dataManager.getItemData(nodeUuid);
      if (node != null)
        parentNode = (NodeData) dataManager.getItemData(node.getParentUUID());
    } catch (RepositoryException e) {
      log.error(e.getLocalizedMessage());
    }
    if (parentNode != null && isLocked(parentNode)) {
      throw new LockException("Session does not have the coorect lock token");
    }

    tokensMap.remove(lData.getLockToken(sessionId));
    locks.remove(nodeUuid);
    lData.setLive(false);
    lData = null;
  }

  private void removeLock(String nodeUuid) {
    NodeData nData;
    try {
      nData = (NodeData) dataManager.getItemData(nodeUuid);
      PlainChangesLog changesLog = new PlainChangesLogImpl(new ArrayList<ItemState>(),
          SystemIdentity.SYSTEM,
          ExtendedEvent.UNLOCK);

      ItemData lockOwner = dataManager.getItemData(QPath.makeChildPath(nData.getQPath(),
          Constants.JCR_LOCKOWNER));
      changesLog.add(ItemState.createDeletedState(lockOwner));

      ItemData lockIsDeep = dataManager.getItemData(QPath.makeChildPath(nData.getQPath(),
          Constants.JCR_LOCKISDEEP));
      changesLog.add(ItemState.createDeletedState(lockIsDeep));

      dataManager.save(changesLog);
    } catch (RepositoryException e) {
      log.error("Error occur during removing lock");
    }

  }

  public void start() {
    lockRemover = new LockRemover();
  }

  public void stop() {

    lockRemover.halt();
  }

  private class LockRemover extends WorkerThread {
    private static final long DEFAULT_TIMEOUT = 15000;//15sec

    public LockRemover() {
      this(DEFAULT_TIMEOUT);
    }

    public LockRemover(long timeout) {
      super(timeout);
      setName("LockRemover " + getId());
      setPriority(Thread.MIN_PRIORITY);
      setDaemon(true);
      start();
      log.info("LockRemover instantiated name= " + getName() + " timeout= " + timeout);

    }

    @Override
    protected void callPeriodically() throws Exception {
      synchronized (locks) {
        for (LockData lock : locks.values()) {
          if (!lock.isSessionScoped() && lock.getTimeToDeath() < 0) {
            removeLock(lock.getNodeUuid());
          }
        }
      }
    }
  }
}
