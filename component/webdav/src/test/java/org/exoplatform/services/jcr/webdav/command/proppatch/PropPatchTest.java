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

package org.exoplatform.services.jcr.webdav.command.proppatch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.BaseStandaloneWebDavTest;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.PropFindCommand;
import org.exoplatform.services.jcr.webdav.command.PropPatchCommand;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
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

public class PropPatchTest extends BaseStandaloneWebDavTest {
  
  public static final String DESCRIPTION = "test description property";
  
  public static final String RIGHTS = "test rights property";

  protected Node propPatchNode;
  
  public void setUp() throws Exception {
    super.setUp();
    if(propPatchNode == null) {
      propPatchNode = writeNode.addNode("propPatchNode", "nt:unstructured");
      session.save();
      propPatchNode.addMixin("dc:elementSet");
      session.save();
    }
  }  
  
  public void testPropPatchForCollection() throws Exception {
    String path = propPatchNode.getPath();
    
    String xml = ""+
    "<D:propertyupdate xmlns:D=\"DAV:\">"+
      "<D:set>"+
        "<D:prop>"+
          "<D:contentlength>10</D:contentlength>"+
          "<D:someprop>somevalue</D:someprop>"+
          "<dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" + DESCRIPTION + "</dc:description>"+
          "<dc:rights xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" + RIGHTS + "</dc:rights>"+
        "</D:prop>"+
      "</D:set>"+      
      "<D:remove>"+
        "<D:prop>"+
          "<D:prop2 />"+
          "<D:prop3 />"+
        "</D:prop>"+
      "</D:remove>"+    
    "</D:propertyupdate>";
    
    XMLInputTransformer transformer = new XMLInputTransformer();
    HierarchicalProperty body = 
      (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(xml.getBytes()));
    
    NullResourceLocksHolder lockHolder = new NullResourceLocksHolder();
    
    PropPatchCommand propPatch = new PropPatchCommand(lockHolder);
    
    Response response = propPatch.propPatch(session, path, body, null, "http://localhost");
    
    assertEquals(WebDavStatus.MULTISTATUS, response.getStatus());
    
    SerializableEntity entity = (SerializableEntity)response.getEntity();
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    entity.writeObject(outStream);
    
    HierarchicalProperty multistatus =
      (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());
    assertEquals(1, multistatus.getChildren().size());
    assertEquals(new QName("DAV:", "response"), multistatus.getChild(0).getName());
    
    Map<QName, WebDavProperty> properties = XmlUtils.parsePropStat(multistatus.getChild(0));
    
    WebDavProperty descriptionProp = properties.get(new QName("http://purl.org/dc/elements/1.1/", "description", "dc"));
    assertNotNull(descriptionProp);
    assertEquals(WebDavStatus.OK, descriptionProp.getStatus());
    
    WebDavProperty rightsProp = properties.get(new QName("http://purl.org/dc/elements/1.1/", "rights", "dc"));
    assertNotNull(rightsProp);
    assertEquals(WebDavStatus.OK, rightsProp.getStatus());
    
    PropFindCommand propFind = new PropFindCommand();
    response = propFind.propfind(session, path, null, Integer.MAX_VALUE, "http://localhost");
    assertEquals(WebDavStatus.MULTISTATUS, response.getStatus());
    
    entity = (SerializableEntity)response.getEntity();
    outStream = new ByteArrayOutputStream();
    entity.writeObject(outStream);
    
    multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());
    assertEquals(1, multistatus.getChildren().size());
    assertEquals(new QName("DAV:", "response"), multistatus.getChild(0).getName());    
    properties = XmlUtils.parsePropStat(multistatus.getChild(0));
    
    descriptionProp = properties.get(new QName("http://purl.org/dc/elements/1.1/", "description", "dc"));
    assertNotNull(descriptionProp);
    assertEquals(WebDavStatus.OK, descriptionProp.getStatus());
    assertEquals(DESCRIPTION, descriptionProp.getValue());
    
    rightsProp = properties.get(new QName("http://purl.org/dc/elements/1.1/", "rights", "dc"));
    assertNotNull(rightsProp);
    assertEquals(WebDavStatus.OK, rightsProp.getStatus());    
    assertEquals(RIGHTS, rightsProp.getValue());
  }
  
  public void testPropPatchWithoutSetTag() throws Exception {
    String path = propPatchNode.getPath();
    
    String xml = ""+
    "<D:propertyupdate xmlns:D=\"DAV:\">"+
      "<D:set>"+
        "<D:prop>"+
          "<D:contentlength>10</D:contentlength>"+
          "<D:someprop>somevalue</D:someprop>"+
        "</D:prop>"+
      "</D:set>"+      
      "<D:remove>"+
        "<D:prop>"+
          "<D:prop2 />"+
          "<D:prop3 />"+
        "</D:prop>"+
      "</D:remove>"+    
    "</D:propertyupdate>";
    
    XMLInputTransformer transformer = new XMLInputTransformer();
    HierarchicalProperty body = 
      (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(xml.getBytes()));
    
    NullResourceLocksHolder lockHolder = new NullResourceLocksHolder();
    
    PropPatchCommand propPatch = new PropPatchCommand(lockHolder);
    
    Response response = propPatch.propPatch(session, path, body, null, "http://localhost");
    
    assertEquals(WebDavStatus.MULTISTATUS, response.getStatus());
    
    SerializableEntity entity = (SerializableEntity)response.getEntity();
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    
    entity.writeObject(outStream);
    
    HierarchicalProperty multistatus =
      (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());
    assertEquals(1, multistatus.getChildren().size());
    assertEquals(new QName("DAV:", "response"), multistatus.getChild(0).getName());
    
    PropFindCommand propFind = new PropFindCommand();
    response = propFind.propfind(session, path, null, Integer.MAX_VALUE, "http://localhost");
    assertEquals(WebDavStatus.MULTISTATUS, response.getStatus());
    
    entity = (SerializableEntity)response.getEntity();
    outStream = new ByteArrayOutputStream();
    entity.writeObject(outStream);
    
    multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());
    assertEquals(1, multistatus.getChildren().size());
    assertEquals(new QName("DAV:", "response"), multistatus.getChild(0).getName());    
   }
  
}
