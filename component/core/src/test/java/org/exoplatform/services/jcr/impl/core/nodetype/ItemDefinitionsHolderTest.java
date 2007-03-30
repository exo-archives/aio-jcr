/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core.nodetype;

import javax.jcr.nodetype.NodeDefinition;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;

public class ItemDefinitionsHolderTest extends JcrImplBaseTest {
  
  public void testNodeDefinition() throws Exception {
    ItemDefinitionsHolder holder = ((NodeTypeManagerImpl)repository.getNodeTypeManager()).getItemDefinitionsHolder();
    
    NodeDefinition def1 = holder.getChildNodeDefinition(Constants.NT_FILE,
        Constants.JCR_CONTENT, Constants.NT_BASE);
    NodeDefinition def2 = holder.getChildNodeDefinition(Constants.NT_FILE,
        Constants.JCR_CONTENT, Constants.NT_RESOURCE);
    
    assertNotNull(def1);
    assertNotNull(def2);
    assertEquals(def1, def2);
    assertEquals("jcr:content", def1.getName());

    
    assertNull(holder.getChildNodeDefinition(Constants.NT_FILE,
        Constants.JCR_DEFAULTPRIMNARYTYPE, Constants.NT_RESOURCE));
  }
  
  public void testResidualNodeDefinition() throws Exception {
    ItemDefinitionsHolder holder = ((NodeTypeManagerImpl)repository.getNodeTypeManager()).getItemDefinitionsHolder();
//    System.out.println(">>>>>>>>>>>> "+holder.getChildNodeDefinition(Constants.NT_FILE, 
//        new InternalQName(null, "test"), Constants.NT_UNSTRUCTURED).getName());
    
    NodeDefinition def1 = holder.getChildNodeDefinition(Constants.NT_UNSTRUCTURED, 
        new InternalQName(null, "test"), Constants.NT_UNSTRUCTURED);
    NodeDefinition def2 = holder.getChildNodeDefinition(Constants.NT_UNSTRUCTURED, 
        new InternalQName(Constants.NS_EXO_URI, "test11111"), Constants.NT_FILE);
    
    assertNotNull(def1);
    assertNotNull(def2);
    assertEquals(def1, def2);
    assertEquals("*", def1.getName());
    
  }

}
