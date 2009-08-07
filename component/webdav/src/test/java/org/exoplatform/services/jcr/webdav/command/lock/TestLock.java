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
import java.util.Map;

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.Depth;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.LockCommand;
import org.exoplatform.services.jcr.webdav.command.PropFindCommand;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.utils.WebDavProperty;
import org.exoplatform.services.jcr.webdav.utils.XmlUtils;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS. Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id$
 */

public class TestLock extends LockTest {

  private Node testLockNode;

  public void setUp() throws Exception {
    super.setUp();
    if (testLockNode == null) {
      testLockNode = lockNode.addNode("testNodeLock", "nt:unstructured");
      session.save();
    }
  }

  protected void tearDown() throws Exception {
    testLockNode.remove();
    session.save();
    super.tearDown();
  }

  public void testSimpleLock() throws Exception {
    NullResourceLocksHolder lockHolder = new NullResourceLocksHolder();

    String path = testLockNode.getPath();

    String reqXml = "<D:lockinfo xmlns:D = \"DAV:\">" + "<D:lockscope>" + "<D:exclusive />"
        + "</D:lockscope>" + "<D:locktype>" + "<D:write/>" + "</D:locktype>" + "<D:owner>"
        + "<D:href>testlockowner</D:href>" + "</D:owner>" + "</D:lockinfo>";

    XMLInputTransformer transformer = new XMLInputTransformer();
    HierarchicalProperty body = (HierarchicalProperty) transformer.readFrom(new ByteArrayInputStream(reqXml.getBytes()));

    Response restResponse = new LockCommand(lockHolder).lock(session,
                                                             path,
                                                             body,
                                                             new Depth(null),
                                                             "" + Integer.MAX_VALUE);
    assertEquals(WebDavStatus.OK, restResponse.getStatus());
    assertEquals(true, testLockNode.isLocked());

    String tokenHeader = restResponse.getResponseHeaders().get("Lock-Token");
    tokenHeader = tokenHeader.substring(1, tokenHeader.length() - 1);
    assertEquals(testLockNode.getLock().getLockToken(), tokenHeader);

    {
      SerializableEntity lockEntity = (SerializableEntity) restResponse.getEntity();
      assertNotNull(lockEntity);
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      lockEntity.writeObject(outStream);

      HierarchicalProperty propertyResponse = (HierarchicalProperty) transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
      assertEquals(new QName("DAV:", "prop"), propertyResponse.getName());

      HierarchicalProperty lockDiscoveryProp = propertyResponse.getChild(0);
      assertEquals(new QName("DAV:", "lockdiscovery"), lockDiscoveryProp.getName());

      HierarchicalProperty activeLock = lockDiscoveryProp.getChild(0);
      assertEquals(new QName("DAV:", "activelock"), activeLock.getName());

      HierarchicalProperty lockType = activeLock.getChild(new QName("DAV:", "locktype"));
      assertNotNull(lockType);
      assertEquals(new QName("DAV:", "write"), lockType.getChild(0).getName());

      HierarchicalProperty lockScope = activeLock.getChild(new QName("DAV:", "lockscope"));
      assertNotNull(lockScope);
      assertEquals(new QName("DAV:", "exclusive"), lockScope.getChild(0).getName());

      HierarchicalProperty depth = activeLock.getChild(new QName("DAV:", "depth"));
      assertNotNull(depth);
      assertEquals("Infinity", depth.getValue());

      HierarchicalProperty owner = activeLock.getChild(new QName("DAV:", "owner"));
      assertNotNull(owner);
      assertEquals("testlockowner", owner.getValue());

      HierarchicalProperty timeOut = activeLock.getChild(new QName("DAV:", "timeout"));
      assertNotNull(timeOut);
      assertEquals("Second-" + Integer.MAX_VALUE, timeOut.getValue());

      HierarchicalProperty lockToken = activeLock.getChild(new QName("DAV:", "locktoken"));
      assertNotNull(lockToken);
      HierarchicalProperty lockTokenHref = lockToken.getChild(0);
      assertEquals(new QName("DAV:", "href"), lockTokenHref.getName());
      assertEquals(testLockNode.getLock().getLockToken(), lockTokenHref.getValue());
    }

    reqXml = "<D:propfind xmlns:D= \"DAV:\">" + "<D:prop>" + "<D:displayname/>"
        + "<D:lockdiscovery/>" + "</D:prop>" + "</D:propfind>";

    body = (HierarchicalProperty) transformer.readFrom(new ByteArrayInputStream(reqXml.getBytes()));

    restResponse = new PropFindCommand().propfind(session,
                                                  path,
                                                  body,
                                                  Integer.MAX_VALUE,
                                                  "http://localhost");
    assertEquals(WebDavStatus.MULTISTATUS, restResponse.getStatus());

    SerializableEntity entity = (SerializableEntity) restResponse.getEntity();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    entity.writeObject(outputStream);

    HierarchicalProperty multistatus = (HierarchicalProperty) transformer.readFrom(new ByteArrayInputStream(outputStream.toByteArray()));
    HierarchicalProperty response = multistatus.getChild(0);

    Map<QName, WebDavProperty> properties = XmlUtils.parsePropStat(response);
    WebDavProperty lockDiscovery = properties.get(new QName("DAV:", "lockdiscovery"));
    assertEquals(WebDavStatus.OK, lockDiscovery.getStatus());

    HierarchicalProperty activeLock = lockDiscovery.getChild(new QName("DAV:", "activelock"));
    HierarchicalProperty lockToken = activeLock.getChild(new QName("DAV:", "locktoken"));
    String lockTokenValue = lockToken.getChild(0).getValue();

    String nativeToken = testLockNode.getLock().getLockToken();
    assertEquals(nativeToken, lockTokenValue);

  }

}
