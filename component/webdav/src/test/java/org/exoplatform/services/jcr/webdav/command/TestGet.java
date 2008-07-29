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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;

import javax.jcr.Node;

import org.exoplatform.services.jcr.webdav.BaseStandaloneWebDavTest;
import org.exoplatform.services.jcr.webdav.Range;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestGet extends BaseStandaloneWebDavTest {

  private Node getNode;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    if(getNode == null) {
      getNode = readNode.addNode("get", "nt:unstructured");
      session.save();
    }    
  } 
  
  public void testSimpleGet() throws Exception {
    String fileName = "test get node " + System.currentTimeMillis();
    
    String FILECONTENT = "test file content.........";
    
    Node fileNode = getNode.addNode(fileName, "nt:file");
    Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:mimeType", "text/xml");
    contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    contentNode.setProperty("jcr:data", new ByteArrayInputStream(FILECONTENT.getBytes()));    
    session.save();

    String path = fileNode.getPath();
    
    ArrayList<Range> ranges = new ArrayList<Range>();
    Range range = new Range();
    range.setStart(0);
    range.setEnd(-1);
    
    ranges.add(range);
    
    Response response = new GetCommand().get(session, path, null, "http://localhost", ranges);
    assertEquals(WebDavStatus.PARTIAL_CONTENT, response.getStatus());
    
    ByteArrayOutputStream outStrem = new ByteArrayOutputStream();
    InputStream inputStream = (InputStream)response.getEntity();
    byte []buffer = new byte[1024];
    
    while (true) {
      int readed = inputStream.read(buffer);
      if (readed < 0) {
        break;
      }
      outStrem.write(buffer, 0, readed);
    }
    
    String content = new String(outStrem.toByteArray());
    assertEquals(FILECONTENT, content);
  }
  
}
