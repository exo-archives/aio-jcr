/**
 * 
 */
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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.storage.ResourcesHolder;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: AbstractMergerManager.java 111 2008-11-11 11:11:11Z $
 */
public abstract class AbstractMergeManager implements MergeManager {
  /**
   * Remote exporter.
   */
  protected final RemoteExporter      exporter;

  /**
   * Data manager.
   */
  protected final DataManager         dataManager;

  /**
   * System data manager.
   */
  protected final DataManager         systemDataManager;

  /**
   * Note type manager.
   */
  protected final NodeTypeDataManager ntManager;

  /**
   * Resource holder.
   */
  protected final ResourcesHolder     resHolder   = new ResourcesHolder();

  /**
   * Storage directory.
   */
  protected final String              storageDir;

  /**
   * Local priority.
   */
  protected final int                 priority;

  /**
   * Flag allowing run of merge.
   */
  protected volatile boolean          run         = true;

  protected Member                    localMember = null;

  /**
   * Helper.
   */
  protected final AsyncHelper         asyncHelper;

  /**
   * Log.
   */
  protected static final Log          LOG         = ExoLogger.getLogger("jcr.MergerManager");

  AbstractMergeManager(RemoteExporter exporter,
                       int priority,
                       DataManager dataManager,
                       DataManager systemDataManager,
                       NodeTypeDataManager ntManager,
                       String storageDir) {

    this.exporter = exporter;
    this.dataManager = dataManager;
    this.systemDataManager = systemDataManager;
    this.ntManager = ntManager;
    this.storageDir = storageDir;
    this.priority = priority;
    this.asyncHelper = new AsyncHelper();
  }

  /**
   * @param localMember
   *          the localMember to set
   */
  public void setLocalMember(Member localMember) {
    this.localMember = localMember;
  }

  /**
   * Cancel current merge process.
   */
  public void cancel() {
    run = false;
  }

  /**
   * Perform cleaup of resorces.
   * 
   */
  public void cleanup() {
    run = false; // but it should be already stopped or canceled

    try {
      resHolder.close();
    } catch (IOException e) {
      LOG.error("Cannot close merge data streams " + e, e);
    }

    // delete files
    File dir = new File(storageDir);
    if (dir.exists()) {
      File[] files = dir.listFiles();
      for (File f : files) {
        deleteStorage(f);
      }
    }
  }

  protected File makePath(Member first, Member second) {
    File dir = new File(storageDir, first.getPriority() + "-" + second.getPriority());
    dir.mkdirs();
    return dir;
  }

  protected File makePath(String dirName) {
    File dir = new File(storageDir, dirName);
    dir.mkdirs();
    return dir;
  }

  private void deleteStorage(File file) {
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      for (File f : files) {
        deleteStorage(f);
      }
    }
    if (!file.delete())
      LOG.warn("Cannot delete file " + file.getAbsolutePath());
  }

}
