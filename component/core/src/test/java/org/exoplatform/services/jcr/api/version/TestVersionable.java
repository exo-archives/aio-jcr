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
package org.exoplatform.services.jcr.api.version;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

/**
 * Created by The eXo Platform SAS 
 * Author : Peter Nedonosko
 * peter.nedonosko@exoplatform.com.ua 18.01.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestVersionable.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class TestVersionable extends BaseVersionTest {

  private Node testRoot;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    
    testRoot = root.addNode("testRemoveVersionable");
    root.save();
    testRoot.addMixin("mix:versionable");
    root.save();
    
    testRoot.checkin();
    testRoot.checkout();
    
    testRoot.addNode("node1");
    testRoot.addNode("node2").setProperty("prop1", "a property #1");
    testRoot.save();
    
    testRoot.checkin();
    testRoot.checkout();
    
    testRoot.getNode("node1").remove();
    testRoot.save();
    
    testRoot.checkin();
  }

  @Override
  protected void tearDown() throws Exception {
    testRoot.remove();
    root.save();
    
    super.tearDown();
  }
  
  public void testRemoveMixVersionable() throws Exception {

    testRoot.checkout();
    
    try {
      testRoot.removeMixin("mix:versionable");
      testRoot.save();
    } catch(RepositoryException e) {
      e.printStackTrace();
      fail("removeMixin(\"mix:versionable\") impossible due to error " + e.getMessage());
    }
  }
  
  public void testRemoveMixVersionableTwice() throws Exception {

    testRoot.checkout();
    
    testRoot.removeMixin("mix:versionable");
    testRoot.save();
    
    try {
      testRoot.removeMixin("mix:versionable");
      fail("removeMixin(\"mix:versionable\") should throw NoSuchNodeTypeException exception");
    } catch(NoSuchNodeTypeException e) {
      // ok
    }
  }
  
  public void testIsCheckedOut() throws Exception {
    // create versionable subnode and checkin its versionable parent
    // testRoot - versionable ancestor
    
    testRoot.checkout();
    Node subNode = testRoot.addNode("node1").addNode("node2").addNode("subNode");
    testRoot.save();
    
    subNode.addMixin("mix:versionable");
    testRoot.save();
    
    subNode.checkin();
    subNode.checkout();
    subNode.setProperty("property1", "property1 v1");
    subNode.save();
    subNode.checkin();
    subNode.checkout();
    
    // test
    testRoot.checkin(); // make subtree checked-in
    try {
      assertTrue("subNode should be checked-out as it's a mix:versionable", subNode.isCheckedOut());
    } catch(RepositoryException e) {
      
    }
  }

}

