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
