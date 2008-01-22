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

public class TestDelete extends BaseStandaloneWebDavTest {
  
  private Node deleteNode;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    if(deleteNode == null) {
      deleteNode = writeNode.addNode("delete", "nt:unstructured");
      session.save();
    }
  }     
  
  public void testDelete() throws Exception {
    String path = deleteNode.getPath();
    
    Response response = new PropFindCommand().propfind(session, path, null, Integer.MAX_VALUE, "http://localhost");
    assertEquals(WebDavStatus.MULTISTATUS, response.getStatus());

    response = new DeleteCommand().delete(session, path);
    assertEquals(WebDavStatus.NO_CONTENT, response.getStatus());    
    assertEquals(false, session.itemExists(path));
    
    response = new PropFindCommand().propfind(session, path, null, Integer.MAX_VALUE, "http://localhost");
    assertEquals(WebDavStatus.NOT_FOUND, response.getStatus());
  }

}
