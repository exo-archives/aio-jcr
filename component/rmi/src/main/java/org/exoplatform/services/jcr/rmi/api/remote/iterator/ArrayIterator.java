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
package org.exoplatform.services.jcr.rmi.api.remote.iterator;

import java.io.Serializable;
import java.util.NoSuchElementException;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;

/**
 * A simple array-based remote iterator. Used when the iteration is short enough
 * for all the elements to be sent over the network in one go.
 */
public class ArrayIterator implements RemoteIterator, Serializable {

  /**
   * The elements in this iterator. Set to <code>null</code> when all elements
   * have been iterated.
   */
  private Object[] elements;

  /**
   * The position of this iterator. Set to the size of the iterator when all
   * elements have been iterated.
   */
  private int      position;

  /**
   * Creates an array-based remote iterator from the given array of remote
   * references or serializable objects.
   * 
   * @param elements elements of the iteration
   */
  public ArrayIterator(Object[] elements) {
    this.elements = elements;
    this.position = 0;
  }

  /**
   * Returns the size of the iterator.
   * 
   * @return length of the iterator
   * @see RemoteIterator#getSize()
   */
  public long getSize() {
    if (elements == null) {
      return position;
    } else {
      return elements.length;
    }
  }

  /**
   * Skips the first <code>items</code> elements in the array. {@inheritDoc}
   */
  public void skip(long items) throws IllegalArgumentException, NoSuchElementException {
    if (items < 0) {
      throw new IllegalArgumentException("Negative skip is not allowed");
    } else if (elements == null || items > elements.length) {
      throw new NoSuchElementException("Skipped past the last element");
    } else {
      int rest = elements.length - (int) items;
      Object[] tmp = null;

      if (rest > 0) {
        tmp = new Object[elements.length - (int) items];
        System.arraycopy(elements, (int) items, tmp, 0, tmp.length);
      }

      elements = tmp;
      position += items;

    }
  }

  /**
   * Returns the underlying array. {@inheritDoc}
   */
  public Object[] nextObjects() throws IllegalArgumentException {
    if (elements == null) {
      return null;
    } else {
      Object[] tmp = elements;
      position += elements.length;
      elements = null;
      return tmp;
    }
  }

}
