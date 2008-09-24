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
package org.exoplatform.services.jcr.ext.backup.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .<br/>
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class AbstractIncrementalBackupJob extends AbstractBackupJob implements
    ItemsPersistenceListener {

  private static Log                        log = ExoLogger.getLogger("ext.IncrementalBackupJob");

  protected final List<ItemStateChangesLog> suspendBuffer;

  public AbstractIncrementalBackupJob() {
    this.suspendBuffer = new ArrayList<ItemStateChangesLog>();
    this.id = 1;

    notifyListeners();
  }

  public final int getType() {
    return INCREMENTAL;
  }

  /**
   * @see org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener#onSaveItems(org.exoplatform.services.jcr.dataflow.ItemStateChangesLog)
   */
  public void onSaveItems(ItemStateChangesLog chlog) {
    if (state == WAITING)
      suspendBuffer.add(chlog);
    else if (state == WORKING)
      try {
        save(chlog);
      } catch (IOException e) {
        log.error("Incremental backup: Can't save log ", e);
        notifyError("Incremental backup: Can't save log ", e);
      }
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public final void run() {
    // TODO [PN] listener was added but never will be removed
    repository.addItemPersistenceListener(workspaceName, this);
    state = WORKING;

    notifyListeners();
  }

  public final void suspend() {
    state = WAITING;
    id++;

    notifyListeners();
  }

  public final URL resume() {
    try {
      url = createStorage();
      for (ItemStateChangesLog log : suspendBuffer) {
        save(log);
      }
      suspendBuffer.clear();
      state = WORKING;

      notifyListeners();
    } catch (FileNotFoundException e) {
      log.error("Incremental backup: resume failed ", e);
      notifyError("Incremental backup: resume failed ", e);
    } catch (IOException e) {
      log.error("Incremental backup: resume failed +", e);
      notifyError("Incremental backup: resume failed ", e);
    }

    return url;
  }

  /**
   * Implementation specific saving
   * 
   * @param log
   */
  protected abstract void save(ItemStateChangesLog log) throws IOException;
}
