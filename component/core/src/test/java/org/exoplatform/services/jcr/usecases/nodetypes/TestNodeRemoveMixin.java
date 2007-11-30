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

package org.exoplatform.services.jcr.usecases.nodetypes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.jcr.Node;

import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SAS
 * Author : Anh Nguyen
 *          ntuananh.vn@gmail.com
 * Nov 13, 2007  
 */
public class TestNodeRemoveMixin extends BaseUsecasesTest {

  public void testNodeRemoveMixin() throws Exception{
    
    //Register Nodetypes
    byte[] xmlData = readXmlContent("/org/exoplatform/services/jcr/usecases/nodetypes/nodetypes-usecase-test.xml");

    ByteArrayInputStream xmlInput = new ByteArrayInputStream(xmlData);

    NodeTypeManagerImpl ntManager = (NodeTypeManagerImpl) session.getWorkspace()
        .getNodeTypeManager();
    ntManager.registerNodeTypes(xmlInput, 0);
    
    String nodeType = "exo:archiveable";
    
    //Create Node
    Node rootNode = session.getRootNode();    
    Node testNode = rootNode.addNode("testMixinNode","exo:myType");
    
    //Add mixin to Node
    testNode.addMixin(nodeType);
    
    //Set a value to Node's Property
    String restorePath = "test/restore/path";
    testNode.setProperty("exo:restorePath", restorePath);
    
    rootNode.save();
    session.save();
    
    assertTrue(testNode.isNodeType(nodeType));
    assertNotNull(testNode.getProperty("exo:restorePath"));
    
    //Do remove Mixin from Node
    testNode = null;
    testNode = rootNode.getNode("testMixinNode");
    assertNotNull(testNode.getProperties());
    testNode.removeMixin(nodeType);
    testNode.save();
    rootNode.save();    
    session.save();
    
    //Error should not be here!
    assertNotNull(testNode.getProperties());       
    
    
  }
  
  private byte[] readXmlContent(String fileName) {

    try {
      InputStream is = TestNodeTypeRegisterReferenced.class.getResourceAsStream(fileName);
      ByteArrayOutputStream output = new ByteArrayOutputStream();

      int r = is.available();
      byte[] bs = new byte[r];
      while (r > 0) {
        r = is.read(bs);
        if (r > 0) {
          output.write(bs, 0, r);
        }
        r = is.available();
      }
      is.close();
      return output.toByteArray();
    } catch (Exception e) {
      log.error("Error read file '" + fileName + "' with NodeTypes. Error:" + e);
      return null;
    }
  }
  
  
  
}
