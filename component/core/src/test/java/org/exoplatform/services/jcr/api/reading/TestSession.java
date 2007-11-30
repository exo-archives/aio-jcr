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
package org.exoplatform.services.jcr.api.reading;


import java.util.Calendar;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.security.impl.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.value.StringValue;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestSession.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestSession extends JcrAPIBaseTest{

  public void testGetRepository(){
    assertEquals(session.getRepository(), repository);
  }

  public void testCredentials(){
    assertEquals(session.getUserID(), credentials.getUserID());

    assertEquals(0, session.getAttributeNames().length);
    assertNull(session.getAttribute("test"));

    credentials.setAttribute("test", "value");
    assertEquals(1, session.getAttributeNames().length);
    assertEquals("value", session.getAttribute("test"));

  }

  public void testGetWorkspace(){
    assertEquals(session.getWorkspace().getSession(), session);
  }

  public void testImpersonate() throws LoginException, RepositoryException {
    Session session2 = session.impersonate(new CredentialsImpl("user", new char[0]));
    assertNotSame(session, session2);
    /*6.2.1 The new Session is tied to a new Workspace instance.
    In other words, Workspace instances are not re-used.*/
    assertNotSame(session.getWorkspace(), session2.getWorkspace());
    /* 6.2.1 However, the Workspace instance returned represents the same actual
       persistent workspace entity in the repository as is represented by
       the Workspace object tied to this Session.*/
    assertEquals(session.getWorkspace().getName(), session2.getWorkspace().getName());
  }

  public void testLogout() throws Exception {
    session.logout();
  }

  public void testGetRootNode() throws RepositoryException {
    assertNotNull(session.getRootNode());
    assertEquals("/", session.getRootNode().getPath());
  }

  public void testItem() throws RepositoryException {
    Node root = session.getRootNode();

    // Node
    Node node = root.addNode("testItem", "nt:folder").addNode("childNode2", "nt:file");
    assertNotNull(session.getItem("/testItem/childNode2"));

    // Property
    Property prop = root.setProperty("prop", "val");
    assertNotNull(session.getItem("/prop"));

    try {
       session.getItem("/not/found");
       fail("exception should have been thrown");
    } catch (PathNotFoundException e) {
    }

    assertTrue(session.itemExists("/testItem/childNode2"));
    assertFalse(session.itemExists("/not/found"));

  }

  public void testGetNodeByUUID() throws RepositoryException {

    Node root = session.getRootNode();
    Node folder = root.addNode("childNode", "nt:folder").addNode("childNode2", "nt:file");

    Node contentNode = folder.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", session.getValueFactory().createValue("this is the content", PropertyType.BINARY));
    contentNode.setProperty("jcr:mimeType", session.getValueFactory().createValue("text/html"));
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));

    try {
      //log.debug("SDM before save: "+session.getTransientNodesManager().dump());
      session.save();
//      child = child.getNode("jcr:content");
      assertNotNull(contentNode.getUUID());
//      System.out.println("LOC>>>"+session.getNodesManager().getLocation(contentNode.getUUID()));
      Node n = session.getNodeByUUID(contentNode.getUUID());
      assertNotNull(n);
      assertEquals(contentNode.getPath(), n.getPath());
    } finally {
      //folder.refresh(false);
      log.debug("SDM before remove: "+session.getTransientNodesManager().dump());
      folder.remove();
      session.save();
    }
    
  }

}
