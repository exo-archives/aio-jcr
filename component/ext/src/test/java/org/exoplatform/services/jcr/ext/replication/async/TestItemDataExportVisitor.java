/*
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
package org.exoplatform.services.jcr.ext.replication.async;

import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SAS
 * Author : Karpenko Sergiy
 *          exo@exoplatform.com
 * 
 */
public class TestItemDataExportVisitor extends BaseStandaloneTest{

  
  public void testGetItemAddStates() throws Exception{
    NodeImpl n = (NodeImpl)root.addNode("test","nt:unstructured");
    root.save();
    
    NodeData d = (NodeData)n.getData();
    ItemDataExportVisitor vis = new ItemDataExportVisitor(d, ((SessionImpl)session).getWorkspace().getNodeTypeManager(), ((SessionImpl)session).getTransientNodesManager());
    
    d.accept(vis);
    List<ItemState> list = vis.getItemAddStates();
    assertEquals(4,list.size());
  }
  
  public void testGetItemAddStatesSubNodes() throws Exception{
    NodeImpl n = (NodeImpl)root.addNode("test","nt:unstructured");
    n.addNode("secondName", "nt:base");
    root.save();
    
    NodeData d = (NodeData)n.getData();
    ItemDataExportVisitor vis = new ItemDataExportVisitor(d, ((SessionImpl)session).getWorkspace().getNodeTypeManager(), ((SessionImpl)session).getTransientNodesManager());
    
    d.accept(vis);
    List<ItemState> list = vis.getItemAddStates();
    assertEquals(8,list.size());
    
  }
  
}
