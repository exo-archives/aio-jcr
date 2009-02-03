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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class ItemStatesStorage<T extends ItemState> extends AbstractChangesStorage<T> implements
    MemberChangesStorage<T> {

  protected static final Log  LOG = ExoLogger.getLogger("jcr.ItemStatesStorage");

  private final ChangesFile storage;

  protected final Member      member;

  class FileIterator<S extends ItemState> implements Iterator<S> {

    private ObjectInputStream in;

    private S                 nextItem;

    public FileIterator() throws IOException, ClassCastException, ClassNotFoundException {

      if (storage == null) {
        throw new NullPointerException("ChangesFile not exists.");
      }

      this.in = new ObjectInputStream(storage.getInputStream());
      this.nextItem = readNext();

    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
      return nextItem != null;
    }

    /**
     * {@inheritDoc}
     */
    public S next() throws NoSuchElementException {
      if (nextItem == null)
        throw new NoSuchElementException();

      S retVal = nextItem;
      try {
        nextItem = readNext();
      } catch (IOException e) {
        throw new StorageRuntimeException(e.getMessage() + " file: " + storage, e);
      } catch (ClassNotFoundException e) {
        throw new StorageRuntimeException(e.getMessage() + " file: " + storage, e);
      } catch (ClassCastException e) {
        throw new StorageRuntimeException(e.getMessage() + " file: " + storage, e);
      }
      return retVal;
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
      throw new RuntimeException("Remove not allowed!");
    }

    @SuppressWarnings("unchecked")
    protected S readNext() throws IOException, ClassNotFoundException, ClassCastException {
      if (in != null) {
        try {
          return (S) in.readObject();
        } catch (EOFException e) {
          // End of list
          in.close();
          in = null;
          return null;
        } catch (Throwable e) {
          in.close();
          in = null;
          throw new StorageIOException(e.getMessage(), e);
        }
      } else
        return null;
    }
  }

  /**
   * ItemStatesStorage constructor for merge (Editable...).
   */
  ItemStatesStorage(Member member) {
    this.member = member;
    this.storage = null;
  }

  /**
   * ItemStatesStorage constructor for export.
   * 
   * @param changes
   *          ChagesFiles
   * @param member
   *          owner
   */
  public ItemStatesStorage(ChangesFile changes, Member member) {
    this.storage = changes;
    this.member = member;
  }

  /**
   * {@inheritDoc}
   */
  public int size() throws IOException, ClassNotFoundException, ClassCastException {
    Iterator<T> it = getChanges();
    int i = 0;
    while (it.hasNext()) {
      i++;
      it.next();
    }
    return i;
  }

  /**
   * {@inheritDoc}
   */
  public Member getMember() {
    return member;
  }

  /**
   * {@inheritDoc}
   */
  public void delete() throws IOException {
    // for (ChangesFile cf : storage)
    // cf.delete();
    storage.delete();
  }

  /**
   * {@inheritDoc}
   */
  public ChangesFile[] getChangesFile() {
    return new ChangesFile[] { storage };// storage.toArray(new ChangesFile[storage.size()]);
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<T> getChanges() throws IOException, ClassCastException, ClassNotFoundException {
    return new FileIterator<T>();
  }



}
