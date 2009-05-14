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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PairChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.async.LocalEventListener;
import org.exoplatform.services.jcr.ext.replication.async.RemoteEventListener;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ReaderSpoolFileHolder;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: SolidLocalStorageImpl.java 111 2008-11-11 11:11:11Z serg $
 */
public class LocalStorageImpl extends AbstractLocalStorage implements LocalStorage,
    LocalEventListener, RemoteEventListener {

  private VersionLogHolder versionLogHolder = null;

  private boolean          incorrectPreviouslySavedData;

  /**
   * LocalStorageImpl constructor.
   * 
   * @param storagePath
   *          - path to store changesLogs files.
   * @param fileCleaner
   *          - FileCleaner used for delete changesLogs and TransiendValueData object
   *          deserialization.
   * @param maxBufferSize
   *          - int used for internal TransientValueData deserialization.
   * @throws NoSuchAlgorithmException
   *           - message digest instantiating error.
   * @throws ChecksumNotFoundException
   *           - there is no file contains changesLog digest.
   */
  public LocalStorageImpl(String storagePath,
                          FileCleaner fileCleaner,
                          int maxBufferSize,
                          ReaderSpoolFileHolder holder) throws NoSuchAlgorithmException,
      ChecksumNotFoundException {
    super(storagePath, fileCleaner, maxBufferSize, holder);

    this.incorrectPreviouslySavedData = false;

    // check files
    File[] files = currentDir.listFiles(new ChangesFilenameFilter(false));

    java.util.Arrays.sort(files, new ChangesFileComparator<File>());

    for (int j = 0; j < files.length; j++) {
      File curFile = files[j];
      // read digest
      File dFile = new File(currentDir, curFile.getName() + DIGESTFILE_EXTENTION);
      if (!dFile.exists() || dFile.length() == 0) {
        LOG.warn(curFile.getName() + " does not have digest file. File may be uncomplete!");
        this.incorrectPreviouslySavedData = true;
      }
    }

    // synchronization is not started for default
    doStop();
  }

  /**
   * LocalStorageImpl constructor.
   * 
   * @param storagePath
   *          - path to store changesLogs files.
   * @param fileCleaner
   *          - FileCleaner used for delete changesLogs and TransiendValueData object
   *          deserialization.
   * @param maxBufferSize
   *          - int used for internal TransientValueData deserialization.
   * @param versionHolder
   *          - VersionHolder.
   * @throws NoSuchAlgorithmException
   *           - message digest instantiating error.
   * @throws ChecksumNotFoundException
   *           - there is no file contains changesLog digest.
   */
  public LocalStorageImpl(String storagePath,
                          FileCleaner fileCleaner,
                          int maxBufferSize,
                          ReaderSpoolFileHolder holder,
                          VersionLogHolder versionLogHolder) throws NoSuchAlgorithmException,
      ChecksumNotFoundException {
    this(storagePath, fileCleaner, maxBufferSize, holder);
    this.versionLogHolder = versionLogHolder;
  }

  /**
   * {@inheritDoc}
   */
  public ChangesStorage<ItemState> getLocalChanges(boolean skipInternal) throws IOException {

    if (isStopped())
      throw new IOException("Local storage already stopped.");

    if (currentDir != null) {
      List<ChangesFile> chFiles = new ArrayList<ChangesFile>();

      File[] files = currentDir.listFiles(new ChangesFilenameFilter(skipInternal));

      java.util.Arrays.sort(files, new ChangesFileComparator<File>());

      for (int j = 0; j < files.length; j++) {
        try {
          File curFile = files[j];
          // read digest
          File dFile = new File(currentDir, curFile.getName() + DIGESTFILE_EXTENTION);
          if (!dFile.exists() || dFile.length() == 0) {
            throw new ChecksumNotFoundException(curFile.getName()
                + " does not have digest file. File may be uncomplete!");
          } else {
            FileInputStream din = new FileInputStream(dFile);

            byte[] crc = new byte[(int) dFile.length()];
            din.read(crc);
            din.close();

            chFiles.add(new SimpleChangesFile(curFile,
                                              crc,
                                              Long.parseLong(asyncHelper.removeInternalTag(curFile.getName())),
                                              resHolder));

          }
        } catch (NumberFormatException e) {
          throw new IOException(e.getMessage());
        }
      }

      return new ChangesLogStorage<ItemState>(chFiles, fileCleaner, maxBufferSize, holder);
    } else {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog itemStates) {
    synchronized (this) {
      if (!incorrectPreviouslySavedData) {
        saveItems(itemStates);
      }
    }
  }

  /**
   * Save list changeslogs.
   * 
   * @param listItemStates
   *          The list of changeslogs.
   */
  public void saveStartChanges(ChangesLogsIterator<TransactionChangesLog> changes) {
    synchronized (this) {
      while (changes.hasNext()) {
        saveItems(changes.next());
      }
    }
  }

  /**
   * Save one changeslog to storage.
   * 
   * @param itemStates
   *          The changeslog to save.
   */
  protected void saveItems(ItemStateChangesLog itemStates) {
    if (!(itemStates instanceof SynchronizerChangesLog)) {
      TransactionChangesLog tLog = (TransactionChangesLog) itemStates;
      ChangesLogIterator cLogs = tLog.getLogIterator();

      if (!cLogs.hasNextLog()) {
        changesQueue.add(tLog);
      } else {
        while (cLogs.hasNextLog()) {
          PlainChangesLog cLog = cLogs.nextLog();
          if (cLog instanceof PairChangesLog) {
            processedPairChangesLog((PairChangesLog) cLog, tLog.getSystemId());
          } else {
            TransactionChangesLog t = new TransactionChangesLog(cLog);
            t.setSystemId(tLog.getSystemId());

            changesQueue.add(t);
          }
        }
      }

      if (changesSpooler == null) {
        // changesSpooler var can be nulled from ChangesSpooler.run()
        ChangesSpooler csp = changesSpooler = new ChangesSpooler();
        csp.start();
      }
    }
  }

  /**
   * Process PairChangesLog according to implementation.
   * 
   * @param pcLog
   *          The PairChangesLog for process
   */
  protected void processedPairChangesLog(PairChangesLog pcLog, String systemId) {
    if (versionLogHolder != null) {
      TransactionChangesLog t = new TransactionChangesLog(versionLogHolder.getPairLog(pcLog.getPairId()));
      t.setSystemId(systemId);

      changesQueue.add(t);
    }
    changesQueue.add(new TransactionChangesLog(pcLog));
  }

}
