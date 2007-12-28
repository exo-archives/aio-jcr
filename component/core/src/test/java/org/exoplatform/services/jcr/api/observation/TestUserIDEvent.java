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
 * Created by The eXo Platform SAS
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.org.ua
 *          reshetnyak.alex@gmail.com		
 * 20.06.2007 10:36:07 
 * @version $Id: TestUserIDEvent.java 20.06.2007 10:36:07 rainfox 
 */
public class TestUserIDEvent extends JcrAPIBaseTest  implements EventListener{

  @Override
  protected void tearDown() throws Exception {
    session.getWorkspace().getObservationManager().removeEventListener(this);
    
    super.tearDown();
  }

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
