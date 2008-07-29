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
import org.exoplatform.services.jcr.webdav.resource.CollectionResource;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.util.PropertyConstants;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.utils.WebDavProperty;
import org.exoplatform.services.jcr.webdav.utils.XmlUtils;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestPropNamesResponseEntity extends PropfindTest {
  
  public static final String ROOT_HREF = "http://somehost";
  
  public static final String FILECONTENT = "test file content";
  
  private Node propNamesCollection;
  
  private Node propNamesFile;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    
    propNamesCollection = propfindNode.addNode("test prop names collection", "nt:folder");
    
    propNamesFile = propNamesCollection.addNode("tets prop names file", "nt:file");
    Node content = propNamesFile.addNode("jcr:content", "nt:resource");
    content.setProperty("jcr:data", FILECONTENT);
    content.setProperty("jcr:mimeType", "text/plain");
    content.setProperty("jcr:lastModified", Calendar.getInstance());
    
    propfindNode.save();
  }   
  
  private void checkSingleProperty(HierarchicalProperty property) {
    assertNotNull(property);
    assertNull(property.getValue());
    assertEquals(0, property.getChildren().size());
  }
  
  public void testPropNamesResponseEntity() throws Exception {
    WebDavNamespaceContext nsContext = new WebDavNamespaceContext(propNamesCollection.getSession());    
    Resource resource  = new CollectionResource(new URI(TextUtil.escape(ROOT_HREF + propNamesCollection.getPath(), '%', true)) , propNamesCollection, nsContext);    
    
    PropFindResponseEntity resp = new PropFindResponseEntity(2, resource, null, true);    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
    resp.writeObject(outStream);
    
    XMLInputTransformer transformer = new XMLInputTransformer();
    HierarchicalProperty multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    
    assertEquals(multistatus.getName(), new QName("DAV:", "multistatus"));
    assertEquals(2, multistatus.getChildren().size());
    
    /*
     * allprop result on collection
     */    
    {
      HierarchicalProperty collectionResponse = multistatus.getChild(0);
      
      Map<QName, WebDavProperty> properties = XmlUtils.parsePropStat(collectionResponse);

      checkSingleProperty(properties.get(PropertyConstants.DISPLAYNAME));
      checkSingleProperty(properties.get(PropertyConstants.RESOURCETYPE));
      checkSingleProperty(properties.get(PropertyConstants.CREATIONDATE));
    }
    
    /*
     * allprop result on the file 
     */
    {
      HierarchicalProperty fileResponse = multistatus.getChild(1);
      
      Map<QName, WebDavProperty> properties = XmlUtils.parsePropStat(fileResponse);
      
      checkSingleProperty(properties.get(PropertyConstants.DISPLAYNAME));
      checkSingleProperty(properties.get(PropertyConstants.RESOURCETYPE));
      checkSingleProperty(properties.get(PropertyConstants.CREATIONDATE));
      checkSingleProperty(properties.get(PropertyConstants.GETLASTMODIFIED));
      
      //checkSingleProperty(properties.get(new QName("http://www.jcp.org/jcr/1.0", "uuid", "jcr")));
      //checkSingleProperty(properties.get(new QName("http://www.jcp.org/jcr/1.0", "created", "jcr")));
      //checkSingleProperty(properties.get(new QName("http://www.jcp.org/jcr/1.0", "primaryType", "jcr")));
      //checkSingleProperty(properties.get(new QName("http://www.jcp.org/jcr/1.0", "lastModified", "jcr")));
      //checkSingleProperty(properties.get(new QName("http://www.jcp.org/jcr/1.0", "mimeType", "jcr")));        
    }
    
  }
  
}
