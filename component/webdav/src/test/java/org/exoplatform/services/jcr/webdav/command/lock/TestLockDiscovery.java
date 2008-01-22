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

package org.exoplatform.services.jcr.webdav.command.lock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.lock.Lock;
import javax.xml.namespace.QName;

import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.propfind.PropFindResponseEntity;
import org.exoplatform.services.jcr.webdav.resource.CollectionResource;
import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.util.PropertyConstants;
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

public class TestLockDiscovery extends LockTest {
  
  private Node lockDiscoveryNode;
  
  public void setUp() throws Exception {
    super.setUp();    
    lockDiscoveryNode = lockNode.addNode("testLockDiscoveryNode", "nt:unstructured");
    session.save();

    lockDiscoveryNode.addMixin("mix:lockable");
    session.save();    
  }
  
  public void testLockDiscovery() throws Exception {
    WebDavNamespaceContext nsContext = new WebDavNamespaceContext(lockDiscoveryNode.getSession());    
    Resource resource = new CollectionResource(new URI(TextUtil.escape("http://localhost" + lockDiscoveryNode.getPath(), '%', true)) , lockDiscoveryNode, nsContext);      
    HashSet<QName> properties = new HashSet<QName>();
    properties.add(PropertyConstants.LOCKDISCOVERY);
    
    // must have status NotFound
    {
      PropFindResponseEntity resp = new PropFindResponseEntity(2, resource, properties, false);    
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
      resp.writeObject(outStream);      
      
      XMLInputTransformer transformer = new XMLInputTransformer();
      HierarchicalProperty multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
      
      assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());    
      assertEquals(1, multistatus.getChildren().size());
      
      HierarchicalProperty response = multistatus.getChild(0);      
      Map<QName, WebDavProperty> props = XmlUtils.parsePropStat(response);
      
      WebDavProperty lockDiscoveryProperty = props.get(PropertyConstants.LOCKDISCOVERY);
      assertEquals(WebDavStatus.NOT_FOUND, lockDiscoveryProperty.getStatus());      
    }
    
    Lock lock = lockDiscoveryNode.lock(true, false);
    session.save();
    
    // must have inner content
    {      
      PropFindResponseEntity resp = new PropFindResponseEntity(2, resource, properties, false);    
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();    
      resp.writeObject(outStream);      
      
      XMLInputTransformer transformer = new XMLInputTransformer();
      HierarchicalProperty multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
      
      assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());
      assertEquals(1, multistatus.getChildren().size());
      
      HierarchicalProperty response = multistatus.getChild(0);
      Map<QName, WebDavProperty> props = XmlUtils.parsePropStat(response);
      
      WebDavProperty lockDiscoveryProperty = props.get(PropertyConstants.LOCKDISCOVERY);
      assertEquals(WebDavStatus.OK, lockDiscoveryProperty.getStatus());
      
      HierarchicalProperty activelock = lockDiscoveryProperty.getChild(new QName("DAV:", "activelock"));
      assertNotNull(activelock);
      
      HierarchicalProperty lockType = activelock.getChild(new QName("DAV:", "locktype"));
      assertNotNull(lockType);
      HierarchicalProperty write = lockType.getChild(new QName("DAV:", "write"));
      assertNotNull(write);
      
      HierarchicalProperty lockScope = activelock.getChild(new QName("DAV:", "lockscope"));
      assertNotNull(lockScope);
      HierarchicalProperty exclusive = lockScope.getChild(new QName("DAV:", "exclusive"));
      assertNotNull(exclusive);
      
      HierarchicalProperty depth = activelock.getChild(new QName("DAV:", "depth"));
      assertNotNull(depth);
      assertEquals("Infinity", depth.getValue());
      
      HierarchicalProperty owner = activelock.getChild(new QName("DAV:", "owner"));
      assertNotNull(owner);
      assertEquals(lock.getLockOwner(), owner.getValue());
      
      HierarchicalProperty timeOut = activelock.getChild(new QName("DAV:", "timeout"));
      assertNotNull(timeOut);
      //assertEquals("Second-" + Integer.MAX_VALUE, timeOut.getValue());      
      assertEquals("Second-86400", timeOut.getValue());
      
      HierarchicalProperty locktoken = activelock.getChild(new QName("DAV:", "locktoken"));
      assertNotNull(locktoken);
      HierarchicalProperty lockTokenHref = locktoken.getChild(new QName("DAV:", "href"));
      assertNotNull(lockTokenHref);
      assertEquals(lock.getLockToken(), lockTokenHref.getValue());
    }
    
  }
  
}
