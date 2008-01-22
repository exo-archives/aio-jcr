/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;

import org.exoplatform.services.jcr.webdav.BaseStandaloneWebDavTest;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.GetCommand;
import org.exoplatform.services.jcr.webdav.command.PutCommand;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestGetByVersion extends BaseStandaloneWebDavTest {
  
  private Node getByVersionNode;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    if(getByVersionNode == null) {
      getByVersionNode = readNode.addNode("getByVersionNode", "nt:unstructured");
      session.save();
    }    
  }
  
  private void putResource(String path, InputStream inputStream, NullResourceLocksHolder lockHolder) {
    Response response = new PutCommand(lockHolder).put(session, path, inputStream, "nt:file", "text/xml", "create-version", null);
    assertEquals(WebDavStatus.CREATED, response.getStatus());
  }
  
  private void assertResponseContent(String path, String versionName, String content) throws IOException {
    Response response = new GetCommand().get(session, path, versionName, "http://localhost", -1, -1);
    assertEquals(WebDavStatus.OK, response.getStatus());
    InputStream entityStream = (InputStream)response.getEntity();    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    byte []buffer = new byte[4096];
    while (true) {
      int readed = entityStream.read(buffer);
      if (readed < 0) {
        break;
      }
      outStream.write(buffer, 0, readed);
    }    
    
    String receivedContent = new String(outStream.toByteArray());
    assertEquals(content, receivedContent);    
  }
  
  public void testGetByVersion1() throws Exception {
    String CONTENT1 = "TEST BASE FILE CONTENT..";
    String CONTENT2 = "TEST CONTENT FOR SECOND VERSION..";
    String CONTENT3 = "TEST CONTENT FOR THIRD VERSION OF THE FILE..";
    
    NullResourceLocksHolder lockHolder = new NullResourceLocksHolder();
    
    String path = getByVersionNode.getPath() + "/new test file.txt";

    putResource(path, new ByteArrayInputStream(CONTENT1.getBytes()), lockHolder);
    putResource(path, new ByteArrayInputStream(CONTENT2.getBytes()), lockHolder);
    putResource(path, new ByteArrayInputStream(CONTENT3.getBytes()), lockHolder);

    assertResponseContent(path, "1", CONTENT1);
    assertResponseContent(path, "2", CONTENT2);
    assertResponseContent(path, "3", CONTENT3);
  }

}
