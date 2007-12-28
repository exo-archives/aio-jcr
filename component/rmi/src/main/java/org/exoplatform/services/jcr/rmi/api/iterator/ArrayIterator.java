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
package org.exoplatform.services.jcr.rmi.api.iterator;

import javax.jcr.RangeIterator;

/**
 * Array implementation of the JCR {@link javax.jcr.RangeIterator RangeIterator}
 * interface. This class implements the RangeIterator functionality for an
 * underlying array of objects. Used as the base class for the type-specific
 * iterator classes defined in this package.
 */
public class ArrayIterator implements RangeIterator {

  /** The current iterator position. */
  private int      position;

  /** The underlying array of objects. */
  private Object[] array;

  /**
   * Creates an iterator for the given array of objects.
   * 
   * @param array the objects to iterate
   */
  public ArrayIterator(Object[] array) {
    this.position = 0;
    this.array = array;
  }

  /** {@inheritDoc} */
  public boolean hasNext() {
    return (position < array.length);
  }

  /** {@inheritDoc} */
  public Object next() {
    return array[position++];
  }

  /** {@inheritDoc} */
  public void remove() {
    throw new UnsupportedOperationException();
  }

  /** {@inheritDoc} */
  public void skip(long items) {
    position += items;
  }

  /** {@inheritDoc} */
  public long getSize() {
    return array.length;
  }

  /** {@inheritDoc} */
  public long getPosition() {
    return position;
  }

}
