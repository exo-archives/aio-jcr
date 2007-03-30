/**
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail.
 **/

package org.exoplatform.services.jcr.impl.core;

import java.util.Collection;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;

/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestItemDataChangesLog.java 13476 2007-03-16 11:15:32Z vetal_ok $
 */
public class TestItemDataChangesLog extends JcrImplBaseTest {

  private TransientNodeData data1;
  private TransientNodeData data2;
  private TransientPropertyData data3;

  public void setUp() throws Exception {
    super.setUp();
    InternalQPath path1 = InternalQPath.makeChildPath(Constants.ROOT_PATH, new InternalQName(null,"testBasicOperations1")); 
    InternalQPath path2 = InternalQPath.makeChildPath(Constants.ROOT_PATH, new InternalQName(null,"testBasicOperations2"));
    InternalQPath path3 = InternalQPath.makeChildPath(Constants.ROOT_PATH, new InternalQName(null,"testBasicOperations3")); 

    data1 = 
      new TransientNodeData(
          path1, 
          "1", 0, new InternalQName(Constants.NS_NT_URI, "unstructured"), 
          new InternalQName[0], 0, Constants.ROOT_UUID, new AccessControlList());
    data2 = 
      new TransientNodeData(
          path2, 
          "2", 0, new InternalQName(Constants.NS_NT_URI, "unstructured"), 
          new InternalQName[0], 0, Constants.ROOT_UUID, new AccessControlList());
    data3 = 
      new TransientPropertyData(
          path3, 
          "3", 0, PropertyType.STRING, Constants.ROOT_UUID, false);

  }

  public void testBasicOperations() throws Exception {
  
    SessionChangesLog cLog = new SessionChangesLog("s1");
    cLog.add(ItemState.createAddedState(data1));
    cLog.add(ItemState.createAddedState(data2));
    cLog.add(ItemState.createDeletedState(data2));
    cLog.add(ItemState.createAddedState(data3));
    cLog.add(ItemState.createUpdatedState(data3));
    
    assertEquals(5, cLog.getSize());
    assertEquals(ItemState.ADDED, cLog.getItemState(data1.getQPath()).getState());
    assertEquals(ItemState.ADDED, cLog.getItemState(data1.getQPath()).getState());
    assertEquals(ItemState.DELETED, cLog.getItemState(data2.getQPath()).getState());
    assertEquals(ItemState.ADDED, cLog.getItemState("1").getState());
    assertEquals(ItemState.DELETED, cLog.getItemState("2").getState());
    
    List <ItemState> states = cLog.getItemStates("3");
    assertEquals(2, states.size());
    assertEquals(ItemState.ADDED, states.get(0).getState());
    assertEquals(ItemState.UPDATED, states.get(1).getState());
    
  }
  
  public void testSessionOperations() throws Exception {
    
    SessionChangesLog cLog = new SessionChangesLog("s1");
    
    TransientNodeData d1 = TransientNodeData.createNodeData(
        data1,
        new InternalQName(null, "testSessionOperations"), 
        new InternalQName(Constants.NS_NT_URI, "unstructured"),
        "d1");

    // test remove
    cLog.add(ItemState.createAddedState(data1));
    cLog.add(ItemState.createAddedState(d1));
    assertEquals(2, cLog.getSize());
    cLog.remove(data1.getQPath());
    assertEquals(0, cLog.getSize());
    

    // test getChanges
    cLog.add(ItemState.createAddedState(data1));
    cLog.add(ItemState.createAddedState(data2));
    cLog.add(ItemState.createAddedState(d1));
    assertEquals(2, cLog.getDescendantsChanges(data1.getQPath()).size());
    assertEquals(1, cLog.getDescendantsChanges(data2.getQPath()).size());
    
    // test pushLog
    SessionChangesLog newLog = (SessionChangesLog)cLog.pushLog(data1.getQPath());
    assertEquals(2, newLog.getSize());
    assertEquals(1, cLog.getSize());
    cLog.remove(data2.getQPath());
    
    // test getLastStates
    cLog.add(ItemState.createAddedState(data1));
    cLog.add(ItemState.createAddedState(data2));
    cLog.add(ItemState.createAddedState(d1));

    Collection <ItemState> nodeStates = cLog.getLastChildrenStates(data1, true);
    assertEquals(1, nodeStates.size());
    ItemState s =nodeStates.iterator().next(); 
    assertTrue(d1.getQPath().equals(s.getData().getQPath()));
    assertTrue(s.isAdded());
    
    cLog.add(ItemState.createDeletedState(d1));
    nodeStates = cLog.getLastChildrenStates(data1, true);
    s =nodeStates.iterator().next(); 
    assertEquals(1, nodeStates.size());
//    System.out.println("log   ----- "+);
    assertTrue(s.isDeleted());

  }
  
  public void testMixinsAddTransient() throws Exception {
    
    String[] mixins = new String[] {"mix:referenceable", "mix:lockable"};
    String[] finalMixins = new String[] {"mix:lockable"};
    
    NodeImpl testNode = (NodeImpl) session.getRootNode().addNode("mixin_transient_test");
    NodeImpl node1 = (NodeImpl) testNode.addNode("node-1");
    session.save();
    
    node1.addMixin(mixins[0]);
    node1.addMixin(mixins[1]);
    
    PropertyImpl uuid = (PropertyImpl) node1.getProperty("jcr:uuid");
    
    try {
      NodeImpl sameNode1 = (NodeImpl) session.getNodeByUUID(uuid.getString());
      checkMixins(mixins, sameNode1);
      
      assertEquals("Nodes must be same", node1, sameNode1);
    } catch(RepositoryException e) {
      fail("Transient node must be accessible by uuid. " + e);
    }
    
    try {
      NodeImpl sameNode1 = (NodeImpl) session.getItem(node1.getPath());
      checkMixins(mixins, sameNode1);
      
      assertEquals("Nodes must be same", node1, sameNode1);
    } catch(RepositoryException e) {
      fail("Transient node must be accessible by path. " + e);
    }
    
    testNode.save();
    
    node1.removeMixin(mixins[0]);
    testNode.save();
    
    checkMixins(finalMixins, node1);
    
    // tear down - testNode will be deleted in tearDown()
  }
  
}