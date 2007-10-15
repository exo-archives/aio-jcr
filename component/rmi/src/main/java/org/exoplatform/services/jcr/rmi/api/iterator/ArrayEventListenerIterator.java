/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
