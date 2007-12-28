/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.storage.jdbc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 *          
 * NOTE! For debug purpose only!
 *          
 * 17.01.2007
 */
public class ItemsArrayList<E extends ItemData> extends ArrayList<E> {

  protected static Log log = ExoLogger.getLogger("jcr.ItemsArrayList");
  
  private final long timestamp;
  
  // wrapped from AbstractList
  public class Itr implements Iterator<E> {
    /**
     * Index of element to be returned by subsequent call to next.
     */
    int cursor           = 0;

    /**
     * Index of element returned by most recent call to next or previous. Reset
     * to -1 if this element is deleted by a call to remove.
     */
    int lastRet          = -1;

    /**
     * The modCount value that the iterator believes that the backing List
     * should have. If this expectation is violated, the iterator has detected
     * concurrent modification.
     */
    int expectedModCount = modCount;

    public boolean hasNext() {
      return cursor != size();
    }

    public E next() {
      ConcurrentModificationException concurrentException = null;
      try {
        checkForComodification();
      } catch(ConcurrentModificationException e) {
        log.error(timestamp + " >>>> ConcurrentModificationException");
        concurrentException = e;
      }
      try {
        E next = get(cursor);
        
        if (concurrentException != null) {
          log.warn(timestamp + " next accesed with concurrent changes " + next.getQPath().getAsString(), concurrentException);
          throw concurrentException;
        }
        
        lastRet = cursor++;
        return next;
      } catch (IndexOutOfBoundsException e) {
        checkForComodification();
        throw new NoSuchElementException();
      }
    }

    public void remove() {
      if (lastRet == -1)
        throw new IllegalStateException();
      checkForComodification();

      try {
        ItemsArrayList.this.remove(lastRet);
        if (lastRet < cursor)
          cursor--;
        lastRet = -1;
        expectedModCount = modCount;
      } catch (IndexOutOfBoundsException e) {
        throw new ConcurrentModificationException();
      }
    }

    final void checkForComodification() {
      if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
    }
  }
  
  ItemsArrayList() {
    super();
    timestamp = System.currentTimeMillis();
  }
  
  /* (non-Javadoc)
   * @see java.util.ArrayList#add(int, java.lang.Object)
   */
  @Override
  public void add(int index, ItemData element) {
    log.info(timestamp + " -- add(" + index + "," + element.getQPath().getAsString() + ")");
    super.add(index, (E) element);
  }

  /* (non-Javadoc)
   * @see java.util.ArrayList#add(java.lang.Object)
   */
  @Override
  public boolean add(ItemData element) {
    log.info(timestamp + " -- add(" + element.getQPath().getAsString() + ")");
    return super.add((E) element);
  }

  /* (non-Javadoc)
   * @see java.util.ArrayList#addAll(java.util.Collection)
   */
  @Override
  public boolean addAll(Collection<? extends E> c) {
    log.info(timestamp + " -- addAll(" + c + ")");
    return super.addAll(c);
  }

  /* (non-Javadoc)
   * @see java.util.ArrayList#addAll(int, java.util.Collection)
   */
  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    log.info(timestamp + " -- addAll(" + index + "," + c + ")");
    return super.addAll(index, c);
  }

  /* (non-Javadoc)
   * @see java.util.ArrayList#clear()
   */
  @Override
  public void clear() {
    log.info(timestamp + " -- clear()");
    super.clear();
  }

  /* (non-Javadoc)
   * @see java.util.AbstractList#iterator()
   */
  @Override
  public Iterator<E> iterator() {
    Iterator<E> iter = new Itr();
    log.info(timestamp + " -- iterator()");
    return iter;
  }

  /* (non-Javadoc)
   * @see java.util.AbstractList#listIterator()
   */
  @Override
  public ListIterator<E> listIterator() {
    log.info(timestamp + " -- listIterator()");
    return super.listIterator();
  }

  /* (non-Javadoc)
   * @see java.util.AbstractList#listIterator(int)
   */
  @Override
  public ListIterator<E> listIterator(int index) {
    log.info(timestamp + " -- listIterator(" + index + ")");
    return super.listIterator(index);
  }

  /* (non-Javadoc)
   * @see java.util.ArrayList#remove(int)
   */
  @Override
  public E remove(int index) {
    log.info(timestamp + " -- remove(" + index + ")");
    return super.remove(index);
  }

  /* (non-Javadoc)
   * @see java.util.ArrayList#remove(java.lang.Object)
   */
  @Override
  public boolean remove(Object o) {
    log.info(timestamp + " -- remove(" + (o instanceof ItemData ? ((ItemData) o).getQPath().getAsString() : o) + ")");
    return super.remove(o);
  }

  /* (non-Javadoc)
   * @see java.util.AbstractCollection#removeAll(java.util.Collection)
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    log.info(timestamp + " -- removeAll(" + c + ")");
    return super.removeAll(c);
  }

  /* (non-Javadoc)
   * @see java.util.ArrayList#removeRange(int, int)
   */
  @Override
  protected void removeRange(int fromIndex, int toIndex) {
    log.info(timestamp + " -- removeRange(" + fromIndex + " " + toIndex + ")");
    super.removeRange(fromIndex, toIndex);
  }

  /* (non-Javadoc)
   * @see java.util.ArrayList#set(int, java.lang.Object)
   */
  @Override
  public E set(int index, E element) {
    log.info(timestamp + " -- set(" + index + "," + element.getQPath().getAsString() + ")");
    return super.set(index, element);
  }

  
}
