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

import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;

/**
 * Array implementation of the JCR
 * {@link javax.jcr.EventListenerIterator EventListenerIterator} interface. This
 * class is used by the JCR-RMI client adapters to convert listener arrays to
 * iterators.
 */
public class ArrayEventListenerIterator extends ArrayIterator implements EventListenerIterator {

  /**
   * Creates an iterator for the given array of listeners.
   * 
   * @param listeners the listeners to iterate
   */
  public ArrayEventListenerIterator(EventListener[] listeners) {
    super(listeners);
  }

  /** {@inheritDoc} */
  public EventListener nextEventListener() {
    return (EventListener) next();
  }

}
