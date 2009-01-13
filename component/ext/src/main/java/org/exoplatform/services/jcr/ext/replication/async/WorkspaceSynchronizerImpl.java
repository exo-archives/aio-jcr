/**
 * 
 */
/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogReadException;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.SynchronizationException;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class WorkspaceSynchronizerImpl implements WorkspaceSynchronizer {

  class ItemStateIterableList<T extends ItemState> implements List<ItemState> {

    private ChangesStorage<ItemState> storage;

    public ItemStateIterableList(ChangesStorage<ItemState> storage) {
      this.storage = storage;
    }

    public boolean add(ItemState o) {
      throw new RuntimeException("Not implemented!");
    }

    public void add(int index, ItemState element) {
      throw new RuntimeException("Not implemented!");
    }

    public boolean addAll(Collection<? extends ItemState> c) {
      throw new RuntimeException("Not implemented!");
    }

    public boolean addAll(int index, Collection<? extends ItemState> c) {
      throw new RuntimeException("Not implemented!");
    }

    public void clear() {
      throw new RuntimeException("Not implemented!");
    }

    public boolean contains(Object o) {
      throw new RuntimeException("Not implemented!");
    }

    public boolean containsAll(Collection<?> c) {
      throw new RuntimeException("Not implemented!");
    }

    public ItemState get(int index) {
      throw new RuntimeException("Not implemented!");
    }

    public int indexOf(Object o) {
      throw new RuntimeException("Not implemented!");
    }

    public boolean isEmpty() {
      throw new RuntimeException("Not implemented!");
    }

    public Iterator<ItemState> iterator() {
      try {
        return this.storage.getChanges();
      } catch (IOException e) {
        throw new ChangesLogReadException(e.getMessage());
      } catch (ClassCastException e) {
        throw new ChangesLogReadException(e.getMessage());
      } catch (ClassNotFoundException e) {
        throw new ChangesLogReadException(e.getMessage());
      }
    }

    public int lastIndexOf(Object o) {
      throw new RuntimeException("Not implemented!");
    }

    public ListIterator<ItemState> listIterator() {
      throw new RuntimeException("Not implemented!");
    }

    public ListIterator<ItemState> listIterator(int index) {
      throw new RuntimeException("Not implemented!");
    }

    public boolean remove(Object o) {
      throw new RuntimeException("Not implemented!");
    }

    public ItemState remove(int index) {
      throw new RuntimeException("Not implemented!");
    }

    public boolean removeAll(Collection<?> c) {
      throw new RuntimeException("Not implemented!");
    }

    public boolean retainAll(Collection<?> c) {
      throw new RuntimeException("Not implemented!");
    }

    public ItemState set(int index, ItemState element) {
      throw new RuntimeException("Not implemented!");
    }

    public int size() {
      throw new RuntimeException("Not implemented!");
    }

    public List<ItemState> subList(int fromIndex, int toIndex) {
      throw new RuntimeException("Not implemented!");
    }

    public Object[] toArray() {
      throw new RuntimeException("Not implemented!");
    }

    public <T> T[] toArray(T[] a) {
      throw new RuntimeException("Not implemented!");
    }

  }

  class SynchronizingChangesLog implements PlainChangesLog {

    private final ChangesStorage<ItemState> storage;
    
    private final String sessionId;

    public SynchronizingChangesLog(ChangesStorage<ItemState> synchronizedChanges) {
      this.storage = synchronizedChanges;
      this.sessionId = IdGenerator.generate();
    }

    /**
     * {@inheritDoc}
     */
    public String getSessionId() {
      return sessionId;
    }

    public String dump() {
      return "Not implemented!";
    }

    public List<ItemState> getAllStates() {
      return new ItemStateIterableList<ItemState>(storage);
    }

    public int getSize() {
      throw new RuntimeException("Not implemented!");
    }

    /**
     * {@inheritDoc}
     */
    public PlainChangesLog add(ItemState state) {
      throw new RuntimeException("Not implemented!");
    }

    /**
     * {@inheritDoc}
     */
    public PlainChangesLog addAll(List<ItemState> states) {
      throw new RuntimeException("Not implemented!");
    }

    /**
     * {@inheritDoc}
     */
    public void clear() {
      throw new RuntimeException("Not implemented!");
    }

    /**
     * {@inheritDoc}
     */
    public int getEventType() {
      throw new RuntimeException("Not implemented!");
    }
  }

  private static final Log              LOG = ExoLogger.getLogger("ext.WorkspaceSynchronizerImpl");

  protected final LocalStorage          storage;

  protected final PersistentDataManager workspace;

  public WorkspaceSynchronizerImpl(PersistentDataManager workspace, LocalStorage storage) {
    this.storage = storage;
    this.workspace = workspace;
  }

  /**
   * Return local changes.<br/> 1. to a merger<br/> 2. to a receiver
   * 
   * @return ChangesStorage
   */
  public ChangesStorage<ItemState> getLocalChanges() throws IOException {
    return storage.getLocalChanges();
  }

  /**
   * {@inheritDoc}
   */
  public void save(ChangesStorage<ItemState> synchronizedChanges) throws SynchronizationException,
                                                                 InvalidItemStateException,
                                                                 UnsupportedOperationException,
                                                                 RepositoryException {
    LOG.info("WorkspaceSynchronizer.save " + synchronizedChanges.getMember());
    ItemStateChangesLog changes = new TransactionChangesLog(new SynchronizingChangesLog(synchronizedChanges));
    try {
      workspace.save(changes);
    } catch (ChangesLogReadException e) {
      throw new SynchronizationException(e);
    }
  }

}
