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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: AbstractMarkableIterator.java 111 2008-11-11 11:11:11Z $
 */
public abstract class AbstractMarkableIterator<C> implements MarkableIterator<C> {

  protected List<C> cache;

  protected boolean marked;

  protected int     markedCachePosition;

  protected int     curReadCachePosition;

  public AbstractMarkableIterator() {
    this.cache = new ArrayList<C>();
    this.marked = false;
    this.markedCachePosition = -1;
    this.curReadCachePosition = -1;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasNext() {
    if (curReadCachePosition >= 0 && curReadCachePosition < cache.size()) {
      return true;
    } else {
      return hasNextFromStorage();
    }
  }

  /**
   * {@inheritDoc}
   */
  public C next() {
    if (curReadCachePosition >= 0 && curReadCachePosition < cache.size()) {
      return cache.get(curReadCachePosition++);
    } else {
      if (marked) {
        C item = nextFromStorage();
        if (cache.size() == 0) {
          curReadCachePosition = 0;
          markedCachePosition = 0;
        }
        cache.add(item);
        return item;
      } else {
        if (cache.size() != 0) {
          cache.clear();
          curReadCachePosition = -1;
          markedCachePosition = -1;
        }
        return nextFromStorage();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void mark() throws IOException {
    if (marked) {
      throw new IOException("Position already marked");
    } else {
      if (curReadCachePosition >= 0 && curReadCachePosition < cache.size()) {
        markedCachePosition = curReadCachePosition;
      } else if (curReadCachePosition == cache.size()) {
        cache.clear();
        markedCachePosition = -1;
        curReadCachePosition = -1;
      }

      marked = true;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void reset() throws IOException {
    if (marked) {
      curReadCachePosition = markedCachePosition;
      marked = false;
    } else {
      throw new IOException("Resetting to invalid mark");
    }
  }

  /**
   * Returns the next element in the iteration.
   * 
   * @return the next element in the iteration
   */
  abstract protected boolean hasNextFromStorage();

  /**
   * Returns true if the iteration has more elements. (In other words, returns true if next would
   * return an element rather than throwing an exception.)
   * 
   * @return true if the iterator has more elements.
   */
  abstract protected C nextFromStorage();
}
