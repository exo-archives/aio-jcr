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

package org.exoplatform.services.jcr.webdav.command;

import javax.jcr.Node;

import org.exoplatform.services.jcr.webdav.BaseStandaloneWebDavTest;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestMove extends BaseStandaloneWebDavTest {
  
  private Node moveNode;
  
  private Node sourceNode;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    if(moveNode == null) {
      moveNode = writeNode.addNode("copy", "nt:unstructured");
      sourceNode = moveNode.addNode("source node " + System.currentTimeMillis(), "nt:folder");
      session.save();
    }
  }  
  
  public void testModeSameWorkspace() throws Exception {
    String source = sourceNode.getPath();
    String destination = moveNode.getPath() + "/destination node " + System.currentTimeMillis();

    assertEquals(true, session.itemExists(source));
    assertEquals(false, session.itemExists(destination));
    
    Response response = new MoveCommand().move(session, source, destination);
    assertEquals(WebDavStatus.CREATED, response.getStatus());
    
    assertEquals(false, session.itemExists(source));
    assertEquals(true, session.itemExists(destination));
  }

  public void testMoveDifferentWorkspace() throws Exception {
    String source = sourceNode.getPath();
    String destination = moveNode.getPath() + "/destination node " + System.currentTimeMillis();
    
    assertEquals(true, session.itemExists(source));
    assertEquals(false, session.itemExists(destination));
    
    Response response = new MoveCommand().move(session, session, source, destination);
    assertEquals(WebDavStatus.CREATED, response.getStatus());
    
    assertEquals(false, session.itemExists(source));
    assertEquals(true, session.itemExists(destination));
  }
  
}

