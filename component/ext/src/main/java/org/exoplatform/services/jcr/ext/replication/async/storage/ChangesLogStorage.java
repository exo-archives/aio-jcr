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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: SolidChangesLogStorage.java 111 2008-11-11 11:11:11Z serg $
 */
public class ChangesLogStorage<T extends ItemState> extends AbstractChangesStorage<T> {

  protected static final Log        LOG = ExoLogger.getLogger("jcr.ChangesLogStorage");

  /**
   * Storage ChangesFiles.
   */
  protected final List<ChangesFile> storage;

  /**
   * Storage owner member info.
   */
  // protected final Member member;
  /**
   * Iterator that goes throw ChangesFiles and return ItemStates.
   * 
   * @param <C>
   *          ItemState extender
   */
  class ItemStatesIterator<C extends ItemState> implements Iterator<C> {

    // private final List<ChangesFile> store;

    private SolidChangesLogsIterator<TransactionChangesLog> logIterator;

    private Iterator<C>                                     currentChangesLog;

    public ItemStatesIterator(List<ChangesFile> store) throws IOException,
        ClassCastException,
        ClassNotFoundException {
      logIterator = new SolidChangesLogsIterator<TransactionChangesLog>(store);
      currentChangesLog = readNextIterator();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
      if (currentChangesLog == null) {
        return false;
      } else {
        if (currentChangesLog.hasNext() == true) {
          return true;
        } else {
          try {
            currentChangesLog = readNextIterator();
            return hasNext();
          } catch (IOException e) {
            throw new StorageRuntimeException(e.getMessage(), e);
          } catch (ClassNotFoundException e) {
            throw new StorageRuntimeException(e.getMessage(), e);
          }
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    public C next() {
      if (currentChangesLog == null)
        throw new NoSuchElementException();

      if (currentChangesLog.hasNext() == true) {
        return currentChangesLog.next();
      } else {
        try {
          currentChangesLog = readNextIterator();
          return next();
        } catch (IOException e) {
          throw new StorageRuntimeException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
          throw new StorageRuntimeException(e.getMessage(), e);
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
      throw new RuntimeException("Remove not allowed!");
    }

    @SuppressWarnings("unchecked")
    protected Iterator<C> readNextIterator() throws IOException,
                                            ClassNotFoundException,
                                            ClassCastException {
      // fetch next
      if (logIterator.hasNext() == false) {
        return null;
      } else {
        TransactionChangesLog curLog = logIterator.next();
        return (Iterator<C>) curLog.getAllStates().iterator();
      }
    }
  }

  /**
   * Class constructor.
   * 
   * @param storage
   *          list of ChangesFiles
   * @param member
   *          owner
   */
  public ChangesLogStorage(List<ChangesFile> storage) {
    this.storage = storage;
  }

  /**
   * Delete all ChangesFiles in storage.
   */
  public void delete() throws IOException {
    for (ChangesFile cf : storage)
      cf.delete();
  }

  public int size() throws IOException, ClassNotFoundException {
    int size = 0;
    SolidChangesLogsIterator<TransactionChangesLog> it = new SolidChangesLogsIterator<TransactionChangesLog>(storage);

    while (it.hasNext()) {
      size += it.next().getSize();
    }
    return size;
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<T> getChanges() throws IOException, ClassCastException, ClassNotFoundException {
    return new ItemStatesIterator<T>(storage);
  }

  /**
   * {@inheritDoc}
   */
  public ChangesFile[] getChangesFile() {
    ChangesFile[] files = new ChangesFile[storage.size()];
    storage.toArray(files);
    return files;
  }

}
