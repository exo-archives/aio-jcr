/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.observation;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.apache.commons.logging.Log;

/**
 * Created by The eXo Platform SARL
 * 10.05.2006
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: SimpleListener.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class SimpleListener implements EventListener {
  
  private Log log;
  private String name;
  private Integer counter;

  public SimpleListener(String name, Log log, Integer counter) {
    this.log = log;
    this.name = (name == null ? "SimpleListener-"+System.currentTimeMillis() : name);
    this.counter = counter;
  }

  public void onEvent(EventIterator events) {
    while(events.hasNext()) {
      Event event = events.nextEvent();
      counter++;
      try {
        System.out.println("EVENT fired by " + name + " " +event.getPath() + " " + event.getType());
      } catch (RepositoryException e) {
        log.error("Error in " + name, e);
      }
    }
  }

  public Integer getCounter() {
    return counter;
  }

  public String getName() {
    return name;
  }
}
