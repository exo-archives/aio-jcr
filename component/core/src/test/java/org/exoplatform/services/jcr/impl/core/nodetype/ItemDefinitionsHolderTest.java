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

package org.exoplatform.services.jcr.impl.core.nodetype;

import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;

public class ItemDefinitionsHolderTest extends JcrImplBaseTest {
  private final boolean isImplemented = false;
  private boolean isLoaded = false;
  private ItemDefinitionsHolder holder;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    if (isImplemented) {
      isLoaded = true;
    } else if (!isImplemented && !isLoaded) {
      NodeTypeManagerImpl ntManager = ((NodeTypeManagerImpl) repository.getNodeTypeManager());
      NodeTypeIterator nodeTypes = ntManager.getAllNodeTypes();
      holder = ((NodeTypeManagerImpl) repository.getNodeTypeManager()).getItemDefinitionsHolder();
      while (nodeTypes.hasNext()) {
        NodeType type = nodeTypes.nextNodeType();
        holder.putDefinitions((ExtendedNodeType) type);
      }
      isLoaded = true;
    }
    
  }

  public void testNodeDefinition() throws Exception {
    
    

    if(isLoaded){
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
  }
  
  public void testResidualNodeDefinition() throws Exception {

//    System.out.println(">>>>>>>>>>>> "+holder.getChildNodeDefinition(Constants.NT_FILE, 
//        new InternalQName(null, "test"), Constants.NT_UNSTRUCTURED).getName());
    if(isLoaded){    
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

}
