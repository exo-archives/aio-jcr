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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PairChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: DevNullListener.java 111 2008-11-11 11:11:11Z serg $
 */
public class WorkspaceNullListener implements ItemsPersistenceListener {

  /**
   * LOG.
   */
  protected static final Log LOG = ExoLogger.getLogger("jcr.WorkspaceNullListener");

  /**
   * Version log holder.
   */
  private VersionLogHolder   versionLogHolder;

  /**
   * Constructor.
   * 
   * @param versionLogHolder VersionLogHolder.
   */
  public WorkspaceNullListener(VersionLogHolder versionLogHolder) {
    this.versionLogHolder = versionLogHolder;
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog itemStates) {

    if (!(itemStates instanceof SynchronizerChangesLog)) {
      TransactionChangesLog tLog = (TransactionChangesLog) itemStates;
      ChangesLogIterator cLogs = tLog.getLogIterator();

      while (cLogs.hasNextLog()) {
        PlainChangesLog cLog = cLogs.nextLog();
        if (cLog instanceof PairChangesLog) {
          if (versionLogHolder != null) {
            // Just get pair ChangesLog and do nothing
            versionLogHolder.getPairLog(((PairChangesLog) cLog).getPairId());
          } else {
            LOG.warn("Got PairChangesLog but there is no any versionHolder.");
          }
        }
      }
    }
  }

}
