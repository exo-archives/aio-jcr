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

package org.exoplatform.services.jcr.webdav.command.deltav;

import javax.jcr.Node;

import org.exoplatform.services.jcr.webdav.BaseStandaloneWebDavTest;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestUnCkeckOut extends BaseStandaloneWebDavTest {

  private Node unCheckOutNode;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    if(unCheckOutNode == null) {
      unCheckOutNode = writeNode.addNode("unCheckOutNode", "nt:unstructured");
      session.save();
    }
  }
  
  public void testUnCkeckOut() throws Exception {
    String path = unCheckOutNode.getPath();
    
    Response response = new UnCheckOutCommand().uncheckout(session, path);
    assertEquals(WebDavStatus.CONFLICT, response.getStatus());

    assertEquals(WebDavStatus.OK, new VersionControlCommand().versionControl(session, path).getStatus());
    
    response = new UnCheckOutCommand().uncheckout(session, path);
    assertEquals(WebDavStatus.INTERNAL_SERVER_ERROR, response.getStatus());
    
    assertEquals(WebDavStatus.OK, new CheckInCommand().checkIn(session, path).getStatus());
    assertEquals(WebDavStatus.OK, new CheckOutCommand().checkout(session, path).getStatus());
    
    assertEquals(WebDavStatus.OK, new UnCheckOutCommand().uncheckout(session, path).getStatus());
  }
  

}

