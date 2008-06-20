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
import java.util.Map;

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.BaseStandaloneWebDavTest;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.util.PropertyConstants;
import org.exoplatform.services.jcr.webdav.utils.WebDavProperty;
import org.exoplatform.services.jcr.webdav.utils.XmlUtils;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestPut extends BaseStandaloneWebDavTest {

  public static final String FILECONTENT = "test file content";
  
  private Node putNode;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    if(putNode == null) {
      putNode = writeNode.addNode("putNode", "nt:unstructured");
      session.save();
    }
  }       
  
  public void testPut() throws Exception {
    String fileName = "test put node " + System.currentTimeMillis();
    
    String path = putNode.getPath() + "/" + fileName;
    
    Response response = new PropFindCommand().propfind(session, path, null, Integer.MAX_VALUE, "http://localhost");
    assertEquals(WebDavStatus.NOT_FOUND, response.getStatus());
    
    NullResourceLocksHolder nullResourceLocks = new NullResourceLocksHolder();
    
    ByteArrayInputStream inputStream = new ByteArrayInputStream(FILECONTENT.getBytes());
    response = new PutCommand(nullResourceLocks).put(session, path, inputStream, "nt:file", "test/xml", null, null);
    assertEquals(WebDavStatus.CREATED, response.getStatus());
    
    response = new PropFindCommand().propfind(session, path, null, Integer.MAX_VALUE, "http://localhost");
    assertEquals(WebDavStatus.MULTISTATUS, response.getStatus());
    SerializableEntity entity = (SerializableEntity)response.getEntity();
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    entity.writeObject(outStream);
    
    XMLInputTransformer transformer = new XMLInputTransformer();
    HierarchicalProperty multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    
    assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());
    assertEquals(1, multistatus.getChildren().size());
    
    HierarchicalProperty property = multistatus.getChild(0);
    Map<QName, WebDavProperty> properties = XmlUtils.parsePropStat(property);
    
    WebDavProperty displayName = properties.get(PropertyConstants.DISPLAYNAME);
    assertEquals(WebDavStatus.OK, displayName.getStatus());
    assertEquals(fileName, displayName.getValue());
    
    WebDavProperty getContentLength = properties.get(PropertyConstants.GETCONTENTLENGTH);
    assertEquals(WebDavStatus.OK, getContentLength.getStatus());
    assertEquals(FILECONTENT.length(), new Integer(getContentLength.getValue()).intValue());
  }

}
