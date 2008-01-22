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
import java.util.Map;

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.exoplatform.services.jcr.webdav.BaseStandaloneWebDavTest;
import org.exoplatform.services.jcr.webdav.WebDavConst;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
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

public class TestHead extends BaseStandaloneWebDavTest {
  
  public static final String CONTENT = "Test content for HEAD command...";
  
  protected Node headNode;
  
  public void setUp() throws Exception {
    super.setUp();
    if(headNode == null) {
      headNode = writeNode.addNode("testHeadNode", "nt:unstructured");
      session.save();
    }
  }   
  
  public void testHead() throws Exception {
    NullResourceLocksHolder lockHolder = new NullResourceLocksHolder();
    
    String path = headNode.getPath() + "/testSomeHeadNode";
    InputStream inputStream = new ByteArrayInputStream(CONTENT.getBytes());
    
    assertEquals(WebDavStatus.CREATED, 
        new PutCommand(lockHolder).put(session, path, inputStream, "nt:file", "text/xml", "create-version", null).getStatus());
    
    Response response = new HeadCommand().head(session, path, "http://localhost");
    assertEquals(WebDavStatus.OK, response.getStatus());
    
    String contentLengthHeader = response.getResponseHeaders().get(WebDavConst.Headers.CONTENTLENGTH);
    assertNotNull(contentLengthHeader);
    
    String contentTypeHeader = response.getResponseHeaders().get(WebDavConst.Headers.CONTENTTYPE);
    assertNotNull(contentTypeHeader);
    
    String lastModifiedHeader = response.getResponseHeaders().get(WebDavConst.Headers.LASTMODIFIED);
    assertNotNull(lastModifiedHeader);
    
    response = new PropFindCommand().propfind(session, path, null, 0, "http://locelhost");
    assertEquals(WebDavStatus.MULTISTATUS, response.getStatus());
    SerializableEntity entity = (SerializableEntity)response.getEntity();
    assertNotNull(entity);
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    entity.writeObject(outStream);
    XMLInputTransformer transformer = new XMLInputTransformer();
    HierarchicalProperty multistatus = 
      (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    
    assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());
    assertEquals(1, multistatus.getChildren().size());
    
    Map<QName, WebDavProperty> properties = XmlUtils.parsePropStat(multistatus.getChild(0));
    
    WebDavProperty contentLengthProp = properties.get(PropertyConstants.GETCONTENTLENGTH);
    assertNotNull(contentLengthProp);
    
    WebDavProperty contentTypeProp = properties.get(PropertyConstants.GETCONTENTTYPE);
    assertNotNull(contentTypeProp);
    
    WebDavProperty lastModifiedProp = properties.get(PropertyConstants.GETLASTMODIFIED);
    assertNotNull(lastModifiedProp);

    assertEquals(contentLengthHeader, contentLengthProp.getValue());
    assertEquals(contentTypeHeader, contentTypeProp.getValue());
    assertEquals(lastModifiedHeader, lastModifiedProp.getValue());
  }

}
