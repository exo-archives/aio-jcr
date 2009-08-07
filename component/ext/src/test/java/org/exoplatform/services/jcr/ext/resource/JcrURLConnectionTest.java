/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.services.jcr.ext.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.security.ConversationState;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id$
 */
public class JcrURLConnectionTest extends BaseStandaloneTest {

  private String fname;

  private String data;

  private Node   testRoot;

  public void setUp() throws Exception {
    super.setUp();

    assertNotNull(System.getProperty("java.protocol.handler.pkgs"));

    fname = "" + System.currentTimeMillis();
    data = "Test JCR urls " + fname;
    testRoot = root.addNode("testRoot", "nt:unstructured");

    Node file = testRoot.addNode(fname, "nt:file");
    Node d = file.addNode("jcr:content", "nt:resource");
    d.setProperty("jcr:mimeType", "text/plain");
    d.setProperty("jcr:lastModified", Calendar.getInstance());
    d.setProperty("jcr:data", new ByteArrayInputStream(data.getBytes()));
    session.save();
    tlsp = (ThreadLocalSessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    tlsp.setSessionProvider(null, new SessionProvider(ConversationState.getCurrent()));
  }

  public void tearDown() throws Exception {
    super.tearDown();
    tlsp.removeSessionProvider(null);
  }

  private ThreadLocalSessionProviderService tlsp;

  public void testURL() throws Exception {
    UnifiedNodeReference nodeRef = new UnifiedNodeReference("jcr://db1/ws/#/jcr:system/");
    URL url = nodeRef.getURL();
    assertEquals("jcr", url.getProtocol());
    assertEquals("db1", url.getHost());
    assertEquals("/ws", url.getPath());
    assertEquals("/jcr:system/", url.getRef());
  }

  public void testNodeRepresentation() throws Exception {
    // there is no node representation for nt:unstructured
    // default must work, by default work document view node representation.
    UnifiedNodeReference nodeRef = new UnifiedNodeReference("jcr://db1/ws/#/testRoot/");
    JcrURLConnection conn = (JcrURLConnection) nodeRef.getURL().openConnection();

    conn.setDoOutput(false);
    Node content = (Node) conn.getContent();
    InputStream in = conn.getInputStream();
    assertEquals("text/xml", conn.getContentType());
    assertEquals("testRoot", content.getName());
    assertEquals("/testRoot", content.getPath());
    byte[] b = new byte[0x2000];
    in.read(b);
    in.close();
    log.info(new String(b));
    conn.disconnect();
  }

  public void testNtFileNodeRepresentation() throws Exception {
    // should be work node representation for nt:file
    UnifiedNodeReference nodeRef = new UnifiedNodeReference("jcr://db1/ws/#/testRoot/" + fname
        + "/");
    JcrURLConnection conn = (JcrURLConnection) nodeRef.getURL().openConnection();
    conn.setDoOutput(false);

    assertEquals("text/plain", conn.getContentType());
    log.info(conn.getLastModified());
    compareStream(conn.getInputStream(), new ByteArrayInputStream(data.getBytes()));
    conn.disconnect();
  }

}
