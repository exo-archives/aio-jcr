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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 28.01.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: CompositeItemStatesStorage.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class CompositeItemStatesStorage<T extends ItemState> extends AbstractChangesStorage<T>
    implements EditableChangesStorage<T> {

  protected final Member                  member;

  protected final File                    storageDir;

  protected final List<ChangesStorage<T>> storages = new ArrayList<ChangesStorage<T>>();

  protected EditableChangesStorage<T>     current;

  class StatesIterator implements Iterator<T> {

    Iterator<ChangesStorage<T>> csIter;

    Iterator<T>                 sIter;

    T                           next;

    StatesIterator() throws ClassCastException, IOException, ClassNotFoundException {
      csIter = storages.iterator();
      while (csIter.hasNext()) {
        sIter = csIter.next().getChanges();
        if (sIter.hasNext()) {
          next = sIter.next();
          break;
        } else
          continue;
      }
    }

    private T readNext() {
      if (next != null) {
        do {
          if (sIter.hasNext()) {
            return sIter.next();
          } else {
            if (csIter.hasNext())
              try {
                sIter = csIter.next().getChanges();
              } catch (ClassCastException e) {
                throw new StorageRuntimeException(e.getMessage(), e);
              } catch (IOException e) {
                throw new StorageRuntimeException(e.getMessage(), e);
              } catch (ClassNotFoundException e) {
                throw new StorageRuntimeException(e.getMessage(), e);
              }
            else
              sIter = null;
          }
        } while (sIter != null);
      }
      
      return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
      return next != null;
    }

    /**
     * {@inheritDoc}
     */
    public T next() {
      T res = next;
      next = readNext();
      return res;
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
      throw new RuntimeException("Not implemented!");
    }
  }

  public CompositeItemStatesStorage(File storageDir, Member member) {
    this.member = member;
    this.storageDir = storageDir;
  }

  /**
   * {@inheritDoc}
   */
  public Member getMember() {
    return member;
  }

  private EditableChangesStorage<T> current() {
    if (current == null) {
      current = new BufferedItemStatesStorage<T>(storageDir, member);
      storages.add(current);
    }

    return current;
  }

  /**
   * {@inheritDoc}
   */
  public void add(T change) throws IOException {
    current().add(change);
  }

  /**
   * {@inheritDoc}
   */
  public void addAll(ChangesStorage<T> changes) throws IOException {
    if (changes instanceof BufferedItemStatesStorage) {
      // special kind of storage, may be buffered itself
      // we have to copy changes to a current

      try {
        for (Iterator<T> chi = changes.getChanges(); chi.hasNext();)
          add(chi.next());
      } catch (ClassCastException e) {
        throw new StorageIOException(e.getMessage(), e);
      } catch (ClassNotFoundException e) {
        throw new StorageIOException(e.getMessage(), e);
      }
    } else if (changes == this) {
      throw new StorageIOException("Cannot add itself to the storage");
    } else {
      // close current, don't use it anymore
      current = null;

      // seems it's ChnagesLog storage, add storage to the list
      storages.add(changes);
    }
  }

  // =========== ChangesStorage impl

  /**
   * {@inheritDoc}
   */
  public void delete() throws IOException {
    current = null;

    for (ChangesStorage<T> cs : storages)
      cs.delete();

    storages.clear();
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<T> getChanges() throws IOException, ClassCastException, ClassNotFoundException {
    return new StatesIterator();
  }

  /**
   * {@inheritDoc}
   */
  public ChangesFile[] getChangesFile() {
    List<ChangesFile> cfiles = new ArrayList<ChangesFile>();
    for (ChangesStorage<T> cs : storages)
      for (ChangesFile cf : cs.getChangesFile())
        cfiles.add(cf);

    return cfiles.toArray(new ChangesFile[cfiles.size()]);
  }

  /**
   * {@inheritDoc}
   */
  public int size() throws IOException, ClassCastException, ClassNotFoundException {
    int size = 0;
    Iterator<T> c = getChanges();
    while (c.hasNext()) {
      c.next();
      size++;
    }

    return size;
  }

}
