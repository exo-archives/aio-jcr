/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.observation;

import java.util.LinkedList;

/**
 * The <code>Queue</code> class is a very simple queue assuming that there is
 * at least one consumer and potentially multiple producers. This class poses no
 * restrictions on the size of the queue.
 */
public class Queue {

  /** The linked list implementing the queue of data */
  private final LinkedList queue;

  /**
   * Creates an instance of this queue.
   */
  public Queue() {
    queue = new LinkedList();
  }

  /**
   * Appends the given <code>object</code> to the end of the queue.
   * <p>
   * After appending the element, the queue is notified such that threads
   * waiting to retrieve an element from the queue are woken up.
   * 
   * @param object the object to be added
   */
  public void put(Object object) {
    synchronized (queue) {
      queue.addLast(object);
      queue.notifyAll();
    }
  }

  /**
   * Returns the first element from the queue. If the queue is currently empty
   * the method waits at most the given number of milliseconds.
   * 
   * @param timeout The maximum number of milliseconds to wait for an entry in
   *          the queue if the queue is empty. If zero, the method waits forever
   *          for an element.
   * @return The first element of the queue or <code>null</code> if the method
   *         timed out waiting for an entry.
   * @throws InterruptedException Is thrown if the current thread is interrupted
   *           while waiting for the queue to get at least one entry.
   */
  public Object get(long timeout) throws InterruptedException {
    synchronized (queue) {
      // wait for data if the queue is empty
      if (queue.isEmpty()) {
        queue.wait(timeout);
      }

      // return null if queue is (still) empty
      if (queue.isEmpty()) {
        return null;
      }

      // return first if queue has content now
      return queue.removeFirst();
    }
  }
}
