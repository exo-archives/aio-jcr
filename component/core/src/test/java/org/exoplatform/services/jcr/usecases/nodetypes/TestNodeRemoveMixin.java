/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.usecases.nodetypes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.jcr.Node;

import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL
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
