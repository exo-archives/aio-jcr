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

import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectReader;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ObjectReaderImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ReaderSpoolFileHolder;
import org.exoplatform.services.jcr.impl.dataflow.serialization.TransactionChangesLogReader;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;

/**
 * Iterator that goes throw all files in storage and returns TransactionChangesLog objects. Created
 * by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ChangesLogsIterator.java 111 2008-11-11 11:11:11Z serg $
 * @param <L>
 *          extender of TransactionChangesLog
 */
public class ChangesLogsIterator<L extends TransactionChangesLog> implements Iterator<L> {

  private static final Log            LOG = ExoLogger.getLogger("ext.ChangesLogsIterator");
  
  /**
   * ChangesFiles to iterate.
   */
  private final List<ChangesFile> list;

  /**
   * Current file index in list.
   */
  private int                     curFileIndex      = 0;

  /**
   * InputStream from current file.
   */
  private ObjectReader            currentIn         = null;

  /**
   * Current ChangesLog.
   */
  private L                       currentChangesLog = null;

  /**
   * FileCleaner used for read TransientValueData.
   */
  private final FileCleaner fileCleaner;
  
  /**
   * MaxBufferSize used for read TransientValueData.
   */
  private final int maxBufferSize;
  
  private final ReaderSpoolFileHolder holder;
  
  /**
   * Constructor. Changes file may contain many ChangesLogs.
   * 
   * @param list
   *          List of ChangesFile
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public ChangesLogsIterator(List<ChangesFile> list, FileCleaner fileCleaner, int maxBufferSize, ReaderSpoolFileHolder holder) throws IOException, ClassNotFoundException {
    this.list = list;
    this.currentChangesLog = readNextChangesLog();
    this.fileCleaner = fileCleaner;
    this.maxBufferSize = maxBufferSize;
    this.holder = holder;
  }

  public boolean hasNext() {
    return currentChangesLog != null;
  }

  @SuppressWarnings("unchecked")
  public L next() {
    if (!hasNext())
      throw new NoSuchElementException();

    try {
      L log = currentChangesLog;
      currentChangesLog = readNextChangesLog();
      return log;
    } catch (IOException e) {
      releaseResources();
      throw new StorageRuntimeException(e.getMessage(), e);
    } catch (ClassCastException e) {
      releaseResources();
      throw new StorageRuntimeException(e.getMessage(), e);
    } catch (ClassNotFoundException e) {
      releaseResources();
      throw new StorageRuntimeException(e.getMessage(), e);
    }
  }

  public void remove() {
    throw new RuntimeException("Unsupported");
  }

  /**
   * Read next changes log from current ChngesFile or next one.
   * 
   * @return extender of TransactionChangesLog
   * @throws IOException
   *           on read from file
   * @throws ClassCastException
   *           on read ChangesLog object
   * @throws ClassNotFoundException
   *           on read ChangesLog object
   */
  @SuppressWarnings("unchecked")
  private L readNextChangesLog() throws IOException, ClassCastException, ClassNotFoundException {
    if (curFileIndex >= list.size() && currentIn == null) {
      return null;
    } else {
      if (currentIn == null)
        currentIn = new ObjectReaderImpl(list.get(curFileIndex++).getInputStream());

      try {
        TransactionChangesLogReader rdr = new TransactionChangesLogReader(fileCleaner, maxBufferSize ,holder);
        return (L) rdr.read(currentIn);
      } catch (EOFException e) {
        currentIn.close();
        currentIn = null;
        // get next file, and try again
        return (readNextChangesLog());
      } catch (Throwable e) {
        currentIn.close();
        currentIn = null;
        throw new StorageIOException(e.getMessage(), e);
      }
    }
  }

  /**
   * Close all opened streams.
   */
  private void releaseResources() {
    currentChangesLog = null;
    try {
      if (currentIn != null)
        currentIn.close();
      currentIn = null;
    } catch (IOException e) {
      LOG.error(e.getMessage());
    }
  }
}
