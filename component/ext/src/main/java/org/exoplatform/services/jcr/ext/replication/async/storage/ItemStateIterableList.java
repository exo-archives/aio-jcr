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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.exoplatform.services.jcr.dataflow.ItemState;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 15.01.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class ItemStateIterableList<T extends ItemState> implements List<ItemState> {

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
      throw new StorageRuntimeException(e.getMessage(), e);
    } catch (ClassCastException e) {
      throw new StorageRuntimeException(e.getMessage(), e);
    } catch (ClassNotFoundException e) {
      throw new StorageRuntimeException(e.getMessage(), e);
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
