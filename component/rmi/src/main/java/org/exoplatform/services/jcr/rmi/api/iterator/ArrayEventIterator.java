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

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

/**
 * Array implementation of the JCR {@link javax.jcr.EventIterator EventIterator}
 * interface. This class is used by the JCR-RMI client adapters to convert node
 * arrays to iterators.
 */
public class ArrayEventIterator extends ArrayIterator implements EventIterator {

  /**
   * Creates an iterator for the given array of events.
   * 
   * @param nodes the nodes to iterate
   */
  public ArrayEventIterator(Event[] nodes) {
    super(nodes);
  }

  /** {@inheritDoc} */
  public Event nextEvent() {
    return (Event) next();
  }

}
