/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.observation;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.org.ua
 *          reshetnyak.alex@gmail.com		
 * 20.06.2007 10:36:07 
 * @version $Id: TestUserIDEvent.java 20.06.2007 10:36:07 rainfox 
 */
public class TestUserIDEvent extends JcrAPIBaseTest  implements EventListener{

  public void testUserId() throws Exception {
        
    session.getWorkspace().getObservationManager().addEventListener(this, Event.NODE_ADDED, root.getPath(), true, null, null, false);
    
    CredentialsImpl credentialsEXO = new CredentialsImpl("exo", "exo".toCharArray());
     
    SessionImpl sessionEXO = (SessionImpl) repository.login(credentialsEXO, "ws");
 
    Node rootEXO = sessionEXO.getRootNode();
    
    rootEXO.addNode("addNode");
    
    sessionEXO.save();
  }

  public void onEvent(EventIterator events) {
    try {
      if (events.hasNext()) {
        Event event = events.nextEvent();
        String userId = event.getUserID();
        
        log.info("UserID     : " + userId);
        log.info("Event path : " + event.getPath());
        
        assertEquals("exo", userId);
      }
    } catch (RepositoryException e) {
      fail("Repository exeption");
    }
  }
}
