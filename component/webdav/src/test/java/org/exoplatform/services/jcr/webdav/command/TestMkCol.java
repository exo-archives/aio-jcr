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
import java.net.URI;
import java.util.HashSet;
import java.util.Map;

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.BaseStandaloneWebDavTest;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.propfind.PropFindResponseEntity;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.resource.CollectionResource;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.util.PropertyConstants;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.utils.WebDavProperty;
import org.exoplatform.services.jcr.webdav.utils.XmlUtils;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestMkCol extends BaseStandaloneWebDavTest {
  
  private Node mkColNode;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    if(mkColNode == null) {
      mkColNode = writeNode.addNode("mkcol", "nt:unstructured");
      session.save();
    }
  }   
  
  public void testMkCol() throws Exception {
    String path = mkColNode.getPath() + "/test mkcol node " + System.currentTimeMillis();
    
    NullResourceLocksHolder nullResourceLocks = new NullResourceLocksHolder();
    Response restResponse = new MkColCommand(nullResourceLocks).mkCol(session, path, "nt:folder", null, null);
    
    assertEquals(WebDavStatus.CREATED, restResponse.getStatus()); 
    
    assertEquals(true, session.itemExists(path));

    WebDavNamespaceContext nsContext = new WebDavNamespaceContext(mkColNode.getSession());
    Node node = (Node)session.getItem(path);
    Resource resource = new CollectionResource(new URI(TextUtil.escape("http://localhost" + path, '%', true)) , node, nsContext);      
    HashSet<QName> properties = new HashSet<QName>();
    
    properties.add(PropertyConstants.DISPLAYNAME);
    properties.add(PropertyConstants.RESOURCETYPE);
    properties.add(PropertyConstants.ISCOLLECTION);
    properties.add(PropertyConstants.ISFOLDER);

    PropFindResponseEntity resp = new PropFindResponseEntity(Integer.MAX_VALUE, resource, properties, false);    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
    resp.writeObject(outStream);
    
    XMLInputTransformer transformer = new XMLInputTransformer();
    HierarchicalProperty multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    
    assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());    
    assertEquals(1, multistatus.getChildren().size());
    
    HierarchicalProperty response = multistatus.getChild(0);      
    Map<QName, WebDavProperty> props = XmlUtils.parsePropStat(response);
    
    WebDavProperty displayName = props.get(PropertyConstants.DISPLAYNAME);
    assertEquals(WebDavStatus.OK, displayName.getStatus());
    assertEquals(node.getName(), displayName.getValue());
    
    WebDavProperty resourceType = props.get(PropertyConstants.RESOURCETYPE);
    assertEquals(WebDavStatus.OK, resourceType.getStatus());
    assertEquals(1, resourceType.getChildren().size());
    
    HierarchicalProperty collection = resourceType.getChild(new QName("DAV:", "collection"));
    assertNotNull(collection);
    
    WebDavProperty isCollection = props.get(PropertyConstants.ISCOLLECTION);
    assertEquals(WebDavStatus.OK, isCollection.getStatus());
    assertEquals("1", isCollection.getValue());
    
    WebDavProperty isFolder = props.get(PropertyConstants.ISFOLDER);
    assertEquals(WebDavStatus.OK, isFolder.getStatus());
    assertEquals("1", isFolder.getValue());
  }
  
}

