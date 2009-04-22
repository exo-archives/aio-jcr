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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ReaderSpoolFileHolder;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: StartChangesStorage.java 111 2008-11-11 11:11:11Z $
 */
public class StartChangesLocalStorageImpl extends AbstractLocalStorage {

  public StartChangesLocalStorageImpl(String storagePath,
                                      FileCleaner fileCleaner,
                                      int maxBufferSize,
                                      ReaderSpoolFileHolder holder) throws ChecksumNotFoundException,
      NoSuchAlgorithmException {
    super(storagePath, fileCleaner, maxBufferSize, holder);
  }

  /**
   * {@inheritDoc}
   */
  public ChangesStorage<ItemState> getLocalChanges(boolean skipInternal) throws IOException {
    if (currentDir != null) {
      List<ChangesFile> chFiles = getChangesFiles(skipInternal);
      return new ChangesLogStorage<ItemState>(chFiles, fileCleaner, maxBufferSize, holder);
    } else {
      return null;
    }
  }

  /**
   * Get local changes.
   * 
   * @param skipInternal
   *          skip internal changes or not
   * @return local changes.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public ChangesLogsIterator<TransactionChangesLog> getChangesLogs(boolean skipInternal) throws IOException,
                                                                                        ClassNotFoundException {
    if (currentDir != null) {
      List<ChangesFile> chFiles = getChangesFiles(skipInternal);
      return new ChangesLogsIterator<TransactionChangesLog>(chFiles,
                                                            fileCleaner,
                                                            maxBufferSize,
                                                            holder);
    } else {
      return null;
    }
  }

  /**
   * Get changes files.
   * 
   * @param skipInternal
   *          skip internal changes or not
   * @return changes files in storage
   * @throws IOException
   */
  private List<ChangesFile> getChangesFiles(boolean skipInternal) throws IOException {
    List<ChangesFile> chFiles = new ArrayList<ChangesFile>();

    File[] files = currentDir.listFiles(new ChangesFilenameFilter(skipInternal));

    java.util.Arrays.sort(files, new ChangesFileComparator<File>());

    for (int j = 0; j < files.length; j++) {
      try {
        File curFile = files[j];

        String curFileName = curFile.getName().endsWith(LocalStorageImpl.INTERNAL_CHANGES_FILE_TAG)
            ? curFile.getName()
                     .substring(0,
                                curFile.getName().length()
                                    - LocalStorageImpl.INTERNAL_CHANGES_FILE_TAG.length())
            : curFile.getName();

        // crc does not calculate
        chFiles.add(new SimpleChangesFile(curFile,
                                          new byte[0],
                                          Long.parseLong(curFileName),
                                          resHolder));
      } catch (NumberFormatException e) {
        throw new IOException(e.getMessage());
      }
    }

    return chFiles;
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog itemStates) {
    TransactionChangesLog tLog = (TransactionChangesLog) itemStates;
    ChangesLogIterator cLogs = tLog.getLogIterator();

    changesQueue.add(tLog);

    if (changesSpooler == null) {
      // changesSpooler var can be nulled from ChangesSpooler.run()
      ChangesSpooler csp = changesSpooler = new ChangesSpooler();
      csp.start();
    }
  }

}
