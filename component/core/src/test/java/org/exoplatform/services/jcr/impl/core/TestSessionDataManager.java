/**
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail.
 **/

package org.exoplatform.services.jcr.impl.core;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.exoplatform.commons.utils.QName;
import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.JCRPath;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestSessionDataManager.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestSessionDataManager extends JcrImplBaseTest {

  private static String      TEST_ROOT = "TestSessionDataManager";

  private SessionDataManager modificationManager;

  private NodeImpl           testRoot;

  public void setUp() throws Exception {
    super.setUp();
    modificationManager = session.getTransientNodesManager();
    NodeImpl root = (NodeImpl) session.getRootNode();

    // init some test items
    // testRoot = (NodeImpl)root.createChildNode(TEST_ROOT, "nt:unstructured",
    // true, true);
    TransientNodeData data = TransientNodeData.createNodeData(root.nodeData(),
        new InternalQName(null, TEST_ROOT),
        new InternalQName(Constants.NS_NT_URI, "unstructured"));
    testRoot = (NodeImpl) modificationManager.update(ItemState.createAddedState(data), false);
    TransientPropertyData prop = TransientPropertyData.createPropertyData(data,
        new InternalQName(Constants.NS_JCR_URI, "primaryType"),
        PropertyType.NAME,
        false);
    prop.setValue(new TransientValueData(new InternalQName(Constants.NS_NT_URI, "unstructured")));
    PropertyImpl prop1 = (PropertyImpl) modificationManager
        .update(ItemState.createAddedState(prop), false);

    // System.out.println("Test root >>>>>> "+testRoot+" "+prop1);

    assertEquals(2, modificationManager.getChangesLog().getSize());

    modificationManager.commit(data.getQPath());
    assertEquals(0, modificationManager.getChangesLog().getSize());

    // session.save();

    // modificationManager.getTransactManager().saveItem(testRoot);

  }

  public void tearDown() throws Exception {
    log.info(" >before delete> " + modificationManager.dump());
    modificationManager.delete(testRoot.nodeData());
    log.info(" >after delete> " + modificationManager.dump());
    modificationManager.commit(testRoot.nodeData().getQPath());

    // testRoot.remove();
    // testRoot.save();
  }

  public void testItemReferencePool() throws Exception {

    SessionDataManager.ItemReferencePool pool = modificationManager.getItemsPool();
    System.gc();
    Thread.sleep(1000);
    // log.info(" >after commit> "+modificationManager.dump());

    // root node
    assertEquals(1, pool.size());

    NodeData parent = (NodeData) testRoot.getData();

    TransientNodeData data = TransientNodeData.createNodeData(parent, new InternalQName(null,
        "testItemReferencePool1"), new InternalQName(Constants.NS_NT_URI, "unstructured"));

    String uuid = data.getIdentifier();

    assertEquals(1, pool.size());
    NodeImpl node1 = (NodeImpl) modificationManager.update(ItemState.createAddedState(data), true);
    assertEquals(2, pool.size());
    assertEquals(uuid, node1.getInternalIdentifier());
    System.out.println("item >" + node1.getPath());
    assertTrue(pool.contains(uuid));
    // return the same value
    assertEquals(node1, pool.get(node1));

    // add one more node
    data = TransientNodeData.createNodeData(parent, new InternalQName(null,
        "testItemReferencePool2"), new InternalQName(Constants.NS_NT_URI, "unstructured"));
    
    NodeImpl node2 = (NodeImpl) modificationManager.update(ItemState.createAddedState(data), true);

    List<NodeImpl> nodes = new ArrayList<NodeImpl>();
    nodes.add(node1);
    nodes.add(node2);
    List<NodeImpl> testNodes = pool.getNodes(nodes);
    assertEquals(2, testNodes.size());
    assertEquals(node1, testNodes.get(0));
    assertEquals(node2, testNodes.get(1));

    // ... add property
    TransientPropertyData prop = TransientPropertyData.createPropertyData(parent,
        new InternalQName(null, "testItemReferencePoolProp1"),
        PropertyType.STRING,
        false);
    PropertyImpl prop1 = (PropertyImpl) modificationManager
        .update(ItemState.createAddedState(prop), true);
    List<PropertyImpl> props = new ArrayList<PropertyImpl>();
    props.add(prop1);
    List<PropertyImpl> testProps = pool.getProperties(props);
    assertEquals(1, testProps.size());
    assertEquals(prop1, testProps.get(0));
    prop.setValue(new TransientValueData(new InternalQName(Constants.NS_NT_URI, "unstructured")));

    pool.remove(uuid);
    // in case for GC
    Thread.sleep(1000);
    // 1 node and 1 prop
    log.info(" >>>>> >>>> > " + modificationManager.dump());

    // /TestSessionDataManager/testItemReferencePoolProp1
    // /TestSessionDataManager
    // /TestSessionDataManager/testItemReferencePool2

    assertEquals(3, pool.size());
  }

  public void testSessionChangesLog() throws Exception {
    SessionChangesLog changesLog = modificationManager.getChangesLog();

    assertEquals(0, changesLog.getAllStates().size());

    NodeData parent = (NodeData) testRoot.getData();

    TransientNodeData data = TransientNodeData.createNodeData(parent, new InternalQName(null,
        "testSessionChangesLogN1"), new InternalQName(Constants.NS_NT_URI, "unstructured"));

    NodeImpl node1 = (NodeImpl) modificationManager.update(ItemState.createAddedState(data), true);
    assertEquals(1, changesLog.getAllStates().size());
    assertNotNull(changesLog.getItemState(node1.getInternalIdentifier()));
    assertNotNull(changesLog.getItemState(node1.getInternalPath()));
    assertEquals(ItemState.ADDED, changesLog.getItemState(node1.getInternalIdentifier()).getState());

    // System.out.println(" > !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! after
    // testSessionChangesLog> "+modificationManager.dump());

    // delete this node ... state should be DELETED
    modificationManager.delete(data);
    assertEquals(2, changesLog.getAllStates().size());
    List<ItemState> lst = changesLog.getItemStates(data.getIdentifier());
    assertEquals(2, lst.size());
    assertEquals(ItemState.DELETED, changesLog.getItemState(data.getIdentifier()).getState());

    // System.out.println(" > 2 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! after
    // testSessionChangesLog> "+modificationManager.dump());

    // add the same node ... state should be ADDED again
    node1 = (NodeImpl) modificationManager.update(ItemState.createAddedState(data), true);
    assertEquals(3, changesLog.getItemStates(data.getIdentifier()).size());
    assertEquals(ItemState.ADDED, changesLog.getItemState(data.getIdentifier()).getState());

    assertEquals(3, changesLog.getAllStates().size());

    // System.out.println(" > 3 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!! after
    // testSessionChangesLog> "+modificationManager.dump());

    // ... add property to the node1
    TransientPropertyData prop = TransientPropertyData.createPropertyData((NodeData) node1
        .getData(), new InternalQName(null, "testSessionChangesLogP1"), PropertyType.STRING, false);

    PropertyImpl prop1 = (PropertyImpl) modificationManager
        .update(ItemState.createAddedState(prop), true);
    assertEquals(ItemState.ADDED, changesLog.getItemState(node1.getInternalIdentifier()).getState());
    assertEquals(ItemState.ADDED, changesLog.getItemState(prop.getIdentifier()).getState());

    assertEquals(4, changesLog.getAllStates().size());

    // // 4 changes: 3 for node1, and 1- for prop1
    assertEquals(4, changesLog.getDescendantsChanges(node1.getInternalPath()).size());

  }

  public void testReadMethods() throws Exception {

    NodeData parent = (NodeData) testRoot.getData();
    TransientNodeData someData = TransientNodeData.createNodeData(parent, new InternalQName(null,
        "testReadMethodsN1"), new InternalQName(Constants.NS_NT_URI, "unstructured"));
    // NodeImpl node1 =
    // (NodeImpl)modificationManager.update(ItemState.createAddedState(data),
    // true);

    // add one more node
    TransientNodeData data = TransientNodeData.createNodeData(parent, new InternalQName(null,
        "testReadMethodsN2"), new InternalQName(Constants.NS_NT_URI, "unstructured"));
    NodeImpl node2 = (NodeImpl) modificationManager.update(ItemState.createAddedState(data), true);

    // ... add property
    TransientPropertyData prop = TransientPropertyData.createPropertyData(parent,
        new InternalQName(null, "testReadMethodsP1"),
        PropertyType.STRING,
        false);
    PropertyImpl prop1 = (PropertyImpl) modificationManager
        .update(ItemState.createAddedState(prop), true);

    //assertNotNull(modificationManager.getItemData(data.getQPath()));
    //assertEquals(data.getUUID(), modificationManager.getItemData(data.getQPath()).getUUID());
    fail("Must fix getItemData method");
    
    
    //assertEquals(node2, modificationManager.getItem(data.getQPath(), true));
    
    fail("Must fix getItem method");
    
    assertEquals(prop.getIdentifier(), modificationManager.getItemData(prop.getIdentifier()).getIdentifier());
    assertEquals(prop1, modificationManager.getItemByIdentifier(prop.getIdentifier(), true));

    assertTrue(modificationManager.hasPendingChanges(data.getQPath()));
    assertFalse(modificationManager.hasPendingChanges(someData.getQPath()));

    assertTrue(modificationManager.isNew(prop.getIdentifier()));
    assertFalse(modificationManager.isNew(parent.getIdentifier()));

    assertFalse(modificationManager.isModified(prop));
    modificationManager.update(ItemState.createUpdatedState(prop), true);
    assertTrue(modificationManager.isModified(prop));

    assertEquals(1, modificationManager.getChildNodesData(parent).size());
    assertEquals(2, modificationManager.getChildPropertiesData(parent).size());

    assertEquals(1, modificationManager.getChildNodes(parent, true).size());
    // List <PropertyImpl> props =
    // modificationManager.getChildProperties(parent, true);
    // for(PropertyImpl p: props)
    // System.out.println(">>>>>>>>>>>>>>>> "+p.getPath());
    assertEquals(2, modificationManager.getChildProperties(parent, true).size());

    // Collections.copy(dest, src);

  }

  public void testCommitAndRefresh() throws Exception {

    NodeData parent = (NodeData) testRoot.getData();
    TransientNodeData data1 = TransientNodeData.createNodeData(parent, new InternalQName(null,
        "testCommitAndRefreshN1"), new InternalQName(Constants.NS_NT_URI, "unstructured"));
    NodeImpl node1 = (NodeImpl) modificationManager.update(ItemState.createAddedState(data1), true);
    TransientPropertyData nt = TransientPropertyData.createPropertyData(data1,
        new InternalQName(Constants.NS_JCR_URI, "primaryType"),
        PropertyType.NAME,
        false);
    nt.setValue(new TransientValueData(new InternalQName(Constants.NS_NT_URI, "unstructured")));
    modificationManager.update(ItemState.createAddedState(nt), true);
    assertEquals(1, modificationManager.getChildPropertiesData(data1).size());

    modificationManager.commit(data1.getQPath());

    // make sure changes are saved
    assertEquals(0, modificationManager.getChangesLog().getSize());

    //assertNotNull(modificationManager.getItem(data1.getQPath(), true));
    fail("Must fix getItem method");

    assertEquals(1, modificationManager.getChildProperties(data1, true).size());

    // ... add property
    TransientPropertyData prop = TransientPropertyData.createPropertyData(parent,
        new InternalQName(null, "testCommitAndRefreshP1"),
        PropertyType.STRING,
        false);
    prop.setValue(new TransientValueData("test"));
    modificationManager.update(ItemState.createAddedState(prop), true);
//    assertEquals("test", ((PropertyImpl) modificationManager.getItem(prop.getQPath(), true))
//        .getString());
    fail("Must fix getItem method");
  }

  @SuppressWarnings("deprecation")
  public void testGetItem() throws RepositoryException, IllegalNameException {
    // get non-existent data by getItemData(NodeData parent, QPathEntry name)
    assertNull(modificationManager.getItemData((NodeData) ((NodeImpl) root).getData(),
        new QPathEntry("", "testgetitemNode", 0)));
    // get non-existent data by ItemData getItemData(QPath path)
//    assertNull(modificationManager.getItemData(QPath.makeChildPath(((NodeImpl) root).getData()
//        .getQPath(), new InternalQName("", "testgetitemNode"))));
    fail("Must fix getItemData method");
    
    NodeImpl testNode = (NodeImpl) root.addNode("testgetitemNode");
    
    
    // get data by getItemData(NodeData parent, QPathEntry name)
    assertNotNull(modificationManager.getItemData((NodeData) ((NodeImpl) root).getData(),
        new QPathEntry("", "testgetitemNode", 0)));
    // get data by ItemData getItemData(QPath path)
//    assertNotNull(modificationManager.getItemData(QPath.makeChildPath(((NodeImpl) root).getData()
//        .getQPath(), new InternalQName("", "testgetitemNode"))));
    fail("Must fix getItemData method");
  }
}