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

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.services.jcr.dataflow.PairChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ReaderSpoolFileHolder;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: SystemLocalStorageImpl.java 111 2008-11-11 11:11:11Z $
 */
public class SystemLocalStorageImpl extends LocalStorageImpl implements VersionLogHolder {

  protected final Map<String, PairChangesLog> pcLogs = new HashMap<String, PairChangesLog>();

  /**
   * SystemLocalStorageImpl constructor.
   * 
   * @param storagePath
   * @param fileCleaner
   * @throws NoSuchAlgorithmException
   * @throws ChecksumNotFoundException
   * @throws NoSuchAlgorithmException
   * @throws ChecksumNotFoundException
   */
  public SystemLocalStorageImpl(String storagePath,
                                FileCleaner fileCleaner,
                                int maxBufferSize,
                                ReaderSpoolFileHolder holder) throws ChecksumNotFoundException,
      NoSuchAlgorithmException {
    super(storagePath, fileCleaner, maxBufferSize, holder);
  }

  /**
   * {@inheritDoc}
   */
  public PairChangesLog getPairLog(String pairId) {
    return pcLogs.remove(pairId);
  }

  /**
   * {@inheritDoc}
   */
  protected void processedPairChangesLog(PairChangesLog pcLog, String systemId) {
    if (pcLogs.get(pcLog.getPairId()) == null) {
      pcLogs.put(pcLog.getPairId(), pcLog);
    } else {
      TransactionChangesLog t = new TransactionChangesLog(pcLog);
      t.setSystemId(systemId);

      changesQueue.add(t);

      t = new TransactionChangesLog(getPairLog(pcLog.getPairId()));
      t.setSystemId(systemId);
      changesQueue.add(t);
    }
  }

}
