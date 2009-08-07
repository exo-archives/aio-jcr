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
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.propfind.PropFindResponseEntity;
import org.exoplatform.services.jcr.webdav.resource.CollectionResource;
import org.exoplatform.services.jcr.webdav.resource.Resource;
import org.exoplatform.services.jcr.webdav.util.PropertyConstants;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.utils.WebDavProperty;
import org.exoplatform.services.jcr.webdav.utils.XmlUtils;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id$
 */

public class TestSupportedLock extends LockTest {

  private Node supportedLockNode;

  public void setUp() throws Exception {
    super.setUp();
    if (supportedLockNode == null) {
      supportedLockNode = lockNode.addNode("testSupportedLockNode", "nt:unstructured");
    }
  }

  public void testSuportedLock() throws Exception {
    WebDavNamespaceContext nsContext = new WebDavNamespaceContext(supportedLockNode.getSession());
    Resource resource = new CollectionResource(new URI(TextUtil.escape("http://localhost"
        + supportedLockNode.getPath(), '%', true)), supportedLockNode, nsContext);

    HashSet<QName> properties = new HashSet<QName>();

    properties.add(PropertyConstants.SUPPORTEDLOCK);

    PropFindResponseEntity resp = new PropFindResponseEntity(2, resource, properties, false);
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    resp.writeObject(outStream);

    XMLInputTransformer transformer = new XMLInputTransformer();
    HierarchicalProperty multistatus = (HierarchicalProperty) transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));

    assertEquals(multistatus.getName(), new QName("DAV:", "multistatus"));

    HierarchicalProperty response = multistatus.getChild(new QName("DAV:", "response"));

    Map<QName, WebDavProperty> davProperties = XmlUtils.parsePropStat(response);

    WebDavProperty supportedLock = davProperties.get(PropertyConstants.SUPPORTEDLOCK);
    assertEquals(WebDavStatus.OK, supportedLock.getStatus());

    HierarchicalProperty lockEntry = supportedLock.getChild(new QName("DAV:", "lockentry"));

    HierarchicalProperty lockScope = lockEntry.getChild(new QName("DAV:", "lockscope"));
    assertNotNull(lockScope);
    HierarchicalProperty exclusive = lockScope.getChild(new QName("DAV:", "exclusive"));
    assertNotNull(exclusive);

    HierarchicalProperty lockType = lockEntry.getChild(new QName("DAV:", "locktype"));
    assertNotNull(lockType);
    HierarchicalProperty write = lockType.getChild(new QName("DAV:", "write"));
    assertNotNull(write);
  }

}
