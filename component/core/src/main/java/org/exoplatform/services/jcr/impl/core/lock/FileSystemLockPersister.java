/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.lock;

import java.io.File;
import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.LockPersisterEntry;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Store information about locks on file system. After start and before stop
 * it remove all lock properties in repository
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class FileSystemLockPersister implements LockPersister {
  public final static String                   PARAM_ROOT_DIR = "path";

  private final Log                            log            = ExoLogger
                                                                  .getLogger("jcr.lock.LockPersister");

  private File                                 rootDir;

  private final WorkspacePersistentDataManager dataManager;

  private final LockPersisterEntry             config;

  public FileSystemLockPersister(WorkspacePersistentDataManager dataManager, WorkspaceEntry config) throws RepositoryConfigurationException,
      RepositoryException {
    this.dataManager = dataManager;
    this.config = config.getLockManager().getPersister();
    init();
  }

  /**
   * Initialize directory tree for storing lock information
   * 
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  private void init() throws RepositoryConfigurationException, RepositoryException {
    String root = config.getParameterValue(PARAM_ROOT_DIR);

    if (root == null)
      throw new RepositoryConfigurationException("Repository service configuration. Source name (sourceName) is expected");
    rootDir = new File(root);
    if (rootDir.exists()) {
      if (!rootDir.isDirectory()) {
        throw new RepositoryConfigurationException("'" + root + "' is not a directory");
      }
    } else {
      if (!rootDir.mkdirs())
        throw new RepositoryException("Can't create dir" + root);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.core.lock.LockPersister#remove(org.exoplatform.services.jcr.impl.core.lock.LockData)
   */
  public void remove(LockData lock) throws LockException {
    log.debug("remove event fire");
    File lockFile = new File(rootDir, lock.getNodeIdentifier());
    if (!lockFile.exists()) {
      throw new LockException("Persistent lock information not exists");
    }
    if (!lockFile.delete())
      throw new LockException("Fail to remove lock information");

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.core.lock.LockPersister#removeAll()
   */
  public void removeAll() throws LockException {
    log.debug("removeAll event fire");
    String[] list = rootDir.list();
    PlainChangesLog changesLog = new PlainChangesLogImpl();
    try {
      for (int i = 0; i < list.length; i++) {
        NodeData lockedNodeData = (NodeData) dataManager.getItemData(list[i]);
        // No item no problem
        if (lockedNodeData != null) {
          PropertyData dataLockIsDeep = (PropertyData) dataManager.getItemData(lockedNodeData,
              new QPathEntry(Constants.JCR_LOCKISDEEP, 0));

          if (dataLockIsDeep != null) {
            changesLog.add(ItemState.createDeletedState(new TransientPropertyData(QPath
                .makeChildPath(lockedNodeData.getQPath(), Constants.JCR_LOCKISDEEP),
                dataLockIsDeep.getIdentifier(),
                0,
                dataLockIsDeep.getType(),
                dataLockIsDeep.getParentIdentifier(),
                dataLockIsDeep.isMultiValued())));
          }

          PropertyData dataLockOwner = (PropertyData) dataManager.getItemData(lockedNodeData,
              new QPathEntry(Constants.JCR_LOCKOWNER, 0));
          if (dataLockOwner != null)
            changesLog.add(ItemState.createDeletedState(new TransientPropertyData(QPath
                .makeChildPath(lockedNodeData.getQPath(), Constants.JCR_LOCKOWNER),
                dataLockOwner.getIdentifier(),
                0,
                dataLockOwner.getType(),
                dataLockOwner.getParentIdentifier(),
                dataLockOwner.isMultiValued())));
        }
      }
      dataManager.save(new TransactionChangesLog(changesLog));
    } catch (RepositoryException e) {
      log.error(e.getLocalizedMessage(), e);
      throw new LockException(e);
    }
    for (int i = 0; i < list.length; i++) {
      File lockFile = new File(rootDir, list[i]);
      if (!lockFile.exists()) {
        throw new LockException("Persistent lock information not exists");
      }
      if (!lockFile.delete())
        throw new LockException("Fail to remove lock information");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.core.lock.LockPersister#add(org.exoplatform.services.jcr.impl.core.lock.LockData)
   */
  public void add(LockData lock) throws LockException {
    log.debug("add event fire");
    File lockFile = new File(rootDir, lock.getNodeIdentifier());

    if (lockFile.exists()) {
      throw new LockException("Persistent lock information already exists");
    }

    try {
      lockFile.createNewFile();
    } catch (IOException e) {
      throw new LockException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.picocontainer.Startable#start()
   */
  public void start() {
    log.debug("Start event fire");
    try {
      removeAll();
    } catch (LockException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() {
    log.debug("Stop event fire");
    try {
      removeAll();
    } catch (LockException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
