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

import javax.jcr.Node;

import org.exoplatform.services.jcr.webdav.BaseStandaloneWebDavTest;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.PropPatchCommand;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropPatchTest extends BaseStandaloneWebDavTest {

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
  
  public void testSimplePropPatch() throws Exception {
    String path = propPatchNode.getPath();
    
    String xml = ""+
    "<D:propertyupdate xmlns:D=\"DAV:\">"+
      "<D:set>"+
        "<D:contentlength>10</D:contentlength>"+
        "<D:someprop>somevalue</D:someprop>"+
        "<dc:description xmlns:dc=\"http://purl.org/dc/elements/1.1/\">test description property</dc:description>"+
        "<dc:rights xmlns:dc=\"http://purl.org/dc/elements/1.1/\">test rights property</dc:rights>"+
      "</D:set>"+      
      "<D:remove>"+
        "<D:prop2 />"+
        "<D:prop3 />"+
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

  }
  
}
