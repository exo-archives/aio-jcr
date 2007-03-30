/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.NoSuchElementException;

/**
 * Remote version of the JCR {@link javax.jcr.RangeIterator} interface. This
 * interface allows both the client and server side to control the amount of
 * buffering used to increase performance.
 */
public interface RemoteRangeIterator extends Remote {

  /**
   * Returns the size of the iteration, or <code>-1</code> if the size is
   * unknown.
   * 
   * @return size of the iteration, or <code>-1</code> if unknown
   * @throws RemoteException on RMI errors
   * @see javax.jcr.RangeIterator#getSize()
   */
  long getSize() throws RemoteException;

  /**
   * Skips the given number of elements in this iteration.
   * 
   * @param items number of elements to skip
   * @throws NoSuchElementException if skipped past the last element
   * @throws RemoteException on RMI errors
   * @see javax.jcr.RangeIterator#skip(long)
   */
  void skip(long items) throws NoSuchElementException, RemoteException;

  /**
   * Returns an array of remote references to the next elements in this
   * iterator. Returns <code>null</code> if the end of this iteration has been
   * reached.
   * <p>
   * To reduce the amount of remote method calls, this method returns an array
   * of one or more elements in this iteration.
   * 
   * @return array of remote references, or <code>null</code>
   * @throws IllegalArgumentException if <code>maxItems</code> is not positive
   * @throws RemoteException on RMI errors
   * @see java.util.Iterator#next()
   */
  Object[] nextObjects() throws IllegalArgumentException, RemoteException;

}
