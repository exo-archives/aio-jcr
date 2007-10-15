/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
