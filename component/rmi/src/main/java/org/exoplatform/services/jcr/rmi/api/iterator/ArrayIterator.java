/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
