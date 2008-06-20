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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.resource.CollectionResource;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.utils.WebDavProperty;
import org.exoplatform.services.jcr.webdav.utils.XmlUtils;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestPropResponseEntity extends PropfindTest {
  
  public static final String ROOT_HREF = "http://localhost";
  
  public static final String FILENAME1 = "prop-file1";
  
  public static final String FILECONTENT1 = "test file content 1";
  
  public static final String FILENAME2 = "prop-file2";
  
  public static final String FILECONTENT2 = "test content for the second file...";
  
  private Node folderNode;
  
  private Node fileNode1;
  
  private Node fileNode2;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    
    while (propfindNode.hasNode("prop-folder")) {
      propfindNode.getNode("prop-folder").remove();
      propfindNode.getSession().save();
    }
    
    folderNode = propfindNode.addNode("prop-folder", "nt:folder");
    
    fileNode1 = folderNode.addNode(FILENAME1, "nt:file");    
    Node content1 = fileNode1.addNode("jcr:content", "nt:resource");
    content1.setProperty("jcr:data", FILECONTENT1);
    content1.setProperty("jcr:mimeType", "text/plain");
    content1.setProperty("jcr:lastModified", Calendar.getInstance());
    
    fileNode2 = folderNode.addNode(FILENAME2, "nt:file");    
    Node content2 = fileNode2.addNode("jcr:content", "nt:resource");
    content2.setProperty("jcr:data", FILECONTENT2);
    content2.setProperty("jcr:mimeType", "text/plain");
    content2.setProperty("jcr:lastModified", Calendar.getInstance());
    
    propfindNode.save();
  }
  
  public void testSimple() throws Exception {    
    WebDavNamespaceContext nsContext = new WebDavNamespaceContext(folderNode.getSession());    
    Resource resource  = new CollectionResource(new URI(TextUtil.escape(ROOT_HREF + folderNode.getPath(), '%', true)) , folderNode, nsContext);
    
    HashSet<QName> properties = new HashSet<QName>();
    
    properties.add(new QName("DAV:", "displayname", "D"));
    properties.add(new QName("DAV:", "resourcetype", "D"));
    properties.add(new QName("DAV:", "getcontentlength", "D"));
    properties.add(new QName("DAV:", "childcount", "D"));
    properties.add(new QName("DAV:", "haschildren", "D"));
    properties.add(new QName("DAV:", "iscollection", "D"));
    properties.add(new QName("DAV:", "isfolder", "D"));
    properties.add(new QName("DAV:", "parentname", "D"));
    
    properties.add(new QName("DAV:", "someproperty"));
    
    //properties.add(new QName("DAV:", "creationdate", "D:"));
    //properties.add(new QName("DAV:", "getcontenttype", "D"));
    //properties.add(new QName("DAV:", "getlastmodified", "D"));    
    //properties.add(new QName("DAV:", "isroot", "D"));
    //properties.add(new QName("DAV:", "supported-method-set", "D"));
    //properties.add(new QName("DAV:", "lockdiscovery", "D"));
    //properties.add(new QName("DAV:", "supportedlock", "D"));
    
    PropFindResponseEntity resp = new PropFindResponseEntity(2, resource, properties, false);    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
    resp.writeObject(outStream);
    
    XMLInputTransformer transformer = new XMLInputTransformer();
    HierarchicalProperty multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    
    assertEquals(multistatus.getName(), new QName("DAV:", "multistatus"));
    
    List<HierarchicalProperty> childs = multistatus.getChildren();
    assertEquals(3, childs.size());
    
    // check response 0
    {
      HierarchicalProperty response = childs.get(0);

      HierarchicalProperty href = response.getChild(new QName("DAV:", "href"));
      String hrefMustBe = ROOT_HREF + TextUtil.escape(folderNode.getPath(), '%', true);        
      assertEquals(hrefMustBe, href.getValue());
      
      Map<QName, WebDavProperty> webDavProps = XmlUtils.parsePropStat(response);
      
      // check displayname
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "displayname"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(folderNode.getName(), p.getValue());
      }
      
      // check resourcetype
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "resourcetype"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        
        assertEquals(1, p.getChildren().size());
        HierarchicalProperty collection = p.getChild(new QName("DAV:", "collection"));
        assertNotNull(collection);
      }
      
      // check getcontentlength
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "getcontentlength"));
        assertEquals(WebDavStatus.NOT_FOUND, p.getStatus());
      }
      
      // check childcount
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "childcount"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(2, new Integer(p.getValue()).intValue());
      }
      
      // check haschildren
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "haschildren"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(1, new Integer(p.getValue()).intValue());
      }
      
      // check iscollection
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "iscollection"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(1, new Integer(p.getValue()).intValue());
      }
      
      // check isfolder
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "isfolder"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(1, new Integer(p.getValue()).intValue());
      }
      
      // check parentname
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "parentname"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(folderNode.getParent().getName(), p.getValue());
      }
      
      // check someproperty
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "someproperty"));
        assertEquals(WebDavStatus.NOT_FOUND, p.getStatus());
      }
      
    }
    
    // check response 1
    {
      HierarchicalProperty response1 = childs.get(1);

      HierarchicalProperty href = response1.getChild(new QName("DAV:", "href"));
      String hrefMustBe = ROOT_HREF + TextUtil.escape(fileNode1.getPath(), '%', true);
      assertEquals(hrefMustBe, href.getValue());
      
      Map<QName, WebDavProperty> webDavProps = XmlUtils.parsePropStat(response1);
      
      // check displayname
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "displayname"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(FILENAME1, p.getValue());
      }
      
      // check resourcetype
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "resourcetype"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(0, p.getChildren().size());
      }
      
      // check getcontentlength
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "getcontentlength"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(FILECONTENT1.length(), new Long(p.getValue()).longValue());
      }

      // check childcount
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "childcount"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(0, new Integer(p.getValue()).intValue());
      }
      
      // check haschildren
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "haschildren"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(0, new Integer(p.getValue()).intValue());
      }
      
      // check iscollection
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "iscollection"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(0, new Integer(p.getValue()).intValue());
      }
      
      // check isfolder
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "isfolder"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(0, new Integer(p.getValue()).intValue());
      }        

      // check parentname
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "parentname"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(fileNode1.getParent().getName(), p.getValue());
      }        
      
      // check someproperty
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "someproperty"));
        assertEquals(WebDavStatus.NOT_FOUND, p.getStatus());
      }
      
    }
    
    // check response 2
    {
      HierarchicalProperty response2 = childs.get(2);
      
      HierarchicalProperty href = response2.getChild(new QName("DAV:", "href"));
      String hrefMustBe = ROOT_HREF + TextUtil.escape(fileNode2.getPath(), '%', true);
      assertEquals(hrefMustBe, href.getValue());
      
      Map<QName, WebDavProperty> webDavProps = XmlUtils.parsePropStat(response2);
      
      // check displayname
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "displayname"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(FILENAME2, p.getValue());
      }
      
      // check resourcetype
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "resourcetype"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(0, p.getChildren().size());
      }
      
      // check getcontentlength
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "getcontentlength"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(FILECONTENT2.length(), new Long(p.getValue()).longValue());
      }
      
      // check childcount
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "childcount"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(0, new Integer(p.getValue()).intValue());
      } 
      
      // check haschildren
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "haschildren"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(0, new Integer(p.getValue()).intValue());
      }
      
      // check iscollection
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "iscollection"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(0, new Integer(p.getValue()).intValue());
      }
      
      // check isfolder
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "isfolder"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(0, new Integer(p.getValue()).intValue());
      }
      
      // check parentname
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "parentname"));
        assertEquals(WebDavStatus.OK, p.getStatus());
        assertEquals(fileNode2.getParent().getName(), p.getValue());
      }        
      
      // check someproperty
      {
        WebDavProperty p = webDavProps.get(new QName("DAV:", "someproperty"));
        assertEquals(WebDavStatus.NOT_FOUND, p.getStatus());
      }        
    }
    
  }  

}
