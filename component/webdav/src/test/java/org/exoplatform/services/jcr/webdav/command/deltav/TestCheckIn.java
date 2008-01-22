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

public class TestCheckIn extends BaseStandaloneWebDavTest {

  private Node checkInNode;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    if(checkInNode == null) {
      checkInNode = writeNode.addNode("checkInNode", "nt:unstructured");
      session.save();
    }
  }
  
  public void testCheckIn() throws Exception {
    String path = checkInNode.getPath();
    
    Response response = new CheckInCommand().checkIn(session, path);
    assertEquals(WebDavStatus.CONFLICT, response.getStatus());
    
    response = new VersionControlCommand().versionControl(session, path);
    assertEquals(WebDavStatus.OK, response.getStatus());
    
    assertEquals(true, checkInNode.isCheckedOut());
    
    response = new CheckInCommand().checkIn(session, path);
    assertEquals(WebDavStatus.OK, response.getStatus());
    
    assertEquals(false, checkInNode.isCheckedOut());
  }

}
