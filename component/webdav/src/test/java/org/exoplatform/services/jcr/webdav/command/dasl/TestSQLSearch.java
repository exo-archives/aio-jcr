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

package org.exoplatform.services.jcr.webdav.command.dasl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.jcr.Node;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.PutCommand;
import org.exoplatform.services.jcr.webdav.command.SearchCommand;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestSQLSearch extends SearchTest {

  private Node sqlSearchNode;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    if(sqlSearchNode == null) {
      sqlSearchNode = searchNode.addNode("sqlSearchNode", "nt:unstructured");
      session.save();
    }    
  }

  public void testSimpleSQLSearch() throws Exception {
    System.out.println("TEST SIMPLE SQL Search...");

    String path = sqlSearchNode.getPath();
    System.out.println("PATH: " + path);    
    
    for (int i = 1; i <= 5; i++ ) {
      NullResourceLocksHolder lockHolder = new NullResourceLocksHolder();
      PutCommand put = new PutCommand(lockHolder);
      InputStream inputStream = new ByteArrayInputStream("CONTENT1".getBytes());
      String nodeType = "nt:file";
      String mimeType = "text/xml";
      Response response = put.put(session, path + "/test file " + i + ".txt", inputStream, nodeType, mimeType, "create-version", null);
      System.out.println("PUT STATUS: " + response.getStatus());
    }
    
    String xml = ""+
    "<D:searchrequest xmlns:D=\"DAV:\">"+
      "<S:sql xmlns:S=\"SQL:\">select * from nt:file</S:sql>"+
    "</D:searchrequest>";
    
    HierarchicalProperty body = body(xml);
    
    Response response = new SearchCommand().search(session, body, "http://localhost");
    System.out.println("SEARCH STATUS: " + response.getStatus());
    
    if (response.getEntity() != null && response.getStatus() == WebDavStatus.MULTISTATUS) {
      SerializableEntity entity = (SerializableEntity)response.getEntity();
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      entity.writeObject(outStream);
      //FileLogUtil.logToFile(outStream.toByteArray());      
    }
    
  }
  
}
