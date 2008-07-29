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

package org.exoplatform.services.jcr.webdav.command.propfind;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.Map;

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.resource.CollectionResource;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.util.PropertyConstants;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.utils.WebDavProperty;
import org.exoplatform.services.jcr.webdav.utils.XmlUtils;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;

/**
 * Created by The eXo Platform SARL .<br/> 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class TestAllPropResponceEntity extends PropfindTest {
  
  public static final String ROOT_HREF = "http://somehost:someport";
  
  private Node allPropFolder;
  
  private Node allPropFile;
  
  public static final String FILECONTENT = "test file content........";
	
  public void setUp() throws Exception {
  	super.setUp();
  	allPropFolder = propfindNode.addNode("allprop-folder", "nt:folder");
  	
  	allPropFile = allPropFolder.addNode("allprop-file", "nt:file");
  	Node content = allPropFile.addNode("jcr:content", "nt:resource");
  	content.setProperty("jcr:data", FILECONTENT);
  	content.setProperty("jcr:mimeType", "text/plain");
  	content.setProperty("jcr:lastModified", Calendar.getInstance());
  	propfindNode.save();
  }	
  
  public void testSimple() throws Exception {
//  	Session internalSession = webdavSession();
//  	assertNotNull(internalSession);
//  	assertNotNull(internalSession.getRootNode());
    
    WebDavNamespaceContext nsContext = new WebDavNamespaceContext(allPropFolder.getSession());    
    Resource resource  = new CollectionResource(new URI(TextUtil.escape(ROOT_HREF + allPropFolder.getPath(), '%', true)) , allPropFolder, nsContext);    
    
    PropFindResponseEntity resp = new PropFindResponseEntity(2, resource, null, false);    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
    resp.writeObject(outStream);
    
    XMLInputTransformer transformer = new XMLInputTransformer();
    HierarchicalProperty multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    
    assertEquals(multistatus.getName(), new QName("DAV:", "multistatus"));    
    assertEquals(2, multistatus.getChildren().size());
    
    // check allprop for collection
    {
      HierarchicalProperty response = multistatus.getChild(0);
      String href = response.getChild(new QName("DAV:", "href")).getValue();
      String hrefMustBe = TextUtil.escape(ROOT_HREF + allPropFolder.getPath(), '%', true);      
      assertEquals(hrefMustBe, href);
      
      Map<QName, WebDavProperty> props = XmlUtils.parsePropStat(response);
      
      WebDavProperty displayname = props.get(PropertyConstants.DISPLAYNAME);
      assertNotNull(displayname);
      assertEquals(WebDavStatus.OK, displayname.getStatus());
      assertEquals(allPropFolder.getName(), displayname.getValue());
      
      WebDavProperty resourcetype = props.get(PropertyConstants.RESOURCETYPE);
      assertNotNull(resourcetype);
      assertEquals(WebDavStatus.OK, resourcetype.getStatus());
      assertEquals(1, resourcetype.getChildren().size());
      HierarchicalProperty collection = resourcetype.getChild(new QName("DAV:", "collection"));
      assertNotNull(collection);
      
      WebDavProperty creationdate = props.get(PropertyConstants.CREATIONDATE);
      assertNotNull(creationdate);      
      assertEquals(WebDavStatus.OK, creationdate.getStatus());
      
//      WebDavProperty jcr_created = props.get(new QName("http://www.jcp.org/jcr/1.0", "created", "jcr"));
//      assertNotNull(jcr_created);
//      assertEquals(WebDavStatus.OK, jcr_created.getStatus());
      
//      WebDavProperty jcr_primaryType = props.get(new QName("http://www.jcp.org/jcr/1.0", "primaryType", "jcr"));
//      assertNotNull(jcr_primaryType);
//      assertEquals(WebDavStatus.OK, jcr_primaryType.getStatus());
//      assertEquals("nt:folder", jcr_primaryType.getValue());      
    }
    
    // check allprop for file
    {
      HierarchicalProperty response = multistatus.getChild(1);      
      String href = response.getChild(new QName("DAV:", "href")).getValue();
      String hrefMustBe = TextUtil.escape(ROOT_HREF + allPropFile.getPath(), '%', true);
      assertEquals(hrefMustBe, href);
      
      Map<QName, WebDavProperty> props = XmlUtils.parsePropStat(response);
      
      WebDavProperty displayname = props.get(PropertyConstants.DISPLAYNAME);
      assertNotNull(displayname);
      assertEquals(WebDavStatus.OK, displayname.getStatus());
      assertEquals(allPropFile.getName(), displayname.getValue());
      
      WebDavProperty resourcetype = props.get(PropertyConstants.RESOURCETYPE);
      assertNotNull(resourcetype);
      assertEquals(WebDavStatus.OK, resourcetype.getStatus());
      assertEquals(0, resourcetype.getChildren().size());
      
      WebDavProperty creationdate = props.get(PropertyConstants.CREATIONDATE);
      assertNotNull(creationdate);
      assertEquals(WebDavStatus.OK, creationdate.getStatus());
      
      WebDavProperty getlastmodified = props.get(PropertyConstants.GETLASTMODIFIED);
      assertNotNull(getlastmodified);
      assertEquals(WebDavStatus.OK, getlastmodified.getStatus());
      
      WebDavProperty getcontentlength = props.get(PropertyConstants.GETCONTENTLENGTH);
      assertNotNull(getcontentlength);
      assertEquals(WebDavStatus.OK, getcontentlength.getStatus());
      assertEquals(FILECONTENT.length(), new Integer(getcontentlength.getValue()).intValue());      

//      WebDavProperty jcr_uuid = props.get(new QName("http://www.jcp.org/jcr/1.0", "uuid", "jcr"));
//      assertNotNull(jcr_uuid);
//      assertEquals(WebDavStatus.OK, jcr_uuid.getStatus());
      
//      WebDavProperty jcr_created = props.get(new QName("http://www.jcp.org/jcr/1.0", "created", "jcr"));
//      assertNotNull(jcr_created);
//      assertEquals(WebDavStatus.OK, jcr_created.getStatus());
      
//      WebDavProperty jcr_primaryType = props.get(new QName("http://www.jcp.org/jcr/1.0", "primaryType", "jcr"));
//      assertNotNull(jcr_primaryType);
//      assertEquals(WebDavStatus.OK, jcr_primaryType.getStatus());
//      assertEquals("nt:file", jcr_primaryType.getValue());
      
//      WebDavProperty jcr_lastModified = props.get(new QName("http://www.jcp.org/jcr/1.0", "lastModified", "jcr"));
//      assertNotNull(jcr_lastModified);
//      assertEquals(WebDavStatus.OK, jcr_lastModified.getStatus());
      
//      WebDavProperty jcr_mimeType = props.get(new QName("http://www.jcp.org/jcr/1.0", "mimeType", "jcr"));
//      assertNotNull(jcr_mimeType);
//      assertEquals(WebDavStatus.OK, jcr_mimeType.getStatus());
//      assertEquals("text/plain", jcr_mimeType.getValue());      
    }
    
  }  
	
}
