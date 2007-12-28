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
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedNodeData;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedPropertyData;
import org.exoplatform.services.jcr.dataflow.persistent.WorkspaceStorageCache;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.util.IdGenerator;

public class TestWorkspaceStorageCache extends JcrImplBaseTest {

  private QPath nodePath1 = QPath.makeChildPath(Constants.ROOT_PATH, new InternalQName(Constants.NS_EXO_PREFIX,"node 1"));
  private QPath nodePath2 = QPath.makeChildPath(Constants.ROOT_PATH, new InternalQName(Constants.NS_EXO_PREFIX,"node 2"));
  private QPath nodePath3 = QPath.makeChildPath(Constants.ROOT_PATH, new InternalQName(Constants.NS_EXO_PREFIX,"node 3"));
  
  private QPath nodePath31 = QPath.makeChildPath(nodePath3, new InternalQName(Constants.NS_EXO_PREFIX,"node 3.1"));
  private QPath nodePath32 = QPath.makeChildPath(nodePath3, new InternalQName(Constants.NS_EXO_PREFIX,"node 3.2"));
  
  private QPath propertyPath11 = QPath.makeChildPath(nodePath1, new InternalQName(Constants.NS_EXO_PREFIX,"property 1.1"));
  private QPath propertyPath12 = QPath.makeChildPath(nodePath1, new InternalQName(Constants.NS_EXO_PREFIX,"property 1.2"));
  private QPath propertyPath21 = QPath.makeChildPath(nodePath2, new InternalQName(Constants.NS_EXO_PREFIX,"property 2.1"));
  private QPath propertyPath22 = QPath.makeChildPath(nodePath2, new InternalQName(Constants.NS_EXO_PREFIX,"property 2.2"));
  
  private QPath propertyPath311 = QPath.makeChildPath(nodePath31, new InternalQName(Constants.NS_EXO_PREFIX,"property 3.1.1"));
  private QPath propertyPath312 = QPath.makeChildPath(nodePath31, new InternalQName(Constants.NS_EXO_PREFIX,"property 3.1.2"));
  
  private String rootUuid;
  
  private String nodeUuid1;
  private String nodeUuid2;
  private String nodeUuid3;
  private String nodeUuid31;
  private String nodeUuid32;
  
  private String propertyUuid11;
  private String propertyUuid12;
  private String propertyUuid21;
  private String propertyUuid22;
  
  private String propertyUuid311;
  private String propertyUuid312;
  
  private NodeData nodeData1;
  private NodeData nodeData2;
  private NodeData nodeData3;
  private NodeData nodeData31;
  private NodeData nodeData32;
  
  private PropertyData propertyData11;
  private PropertyData propertyData12;
  private PropertyData propertyData21;
  private PropertyData propertyData22;
  private PropertyData propertyData311;
  private PropertyData propertyData312;
  
  private WorkspaceStorageCacheImpl cache;
  
  public void setUp() throws Exception {
    super.setUp();
    
    rootUuid = IdGenerator.generate();
    
    nodeUuid1 = IdGenerator.generate();
    nodeUuid2 = IdGenerator.generate();
    nodeUuid3 = IdGenerator.generate();
    nodeUuid31 = IdGenerator.generate();
    nodeUuid32 = IdGenerator.generate();
    
    propertyUuid11 = IdGenerator.generate();
    propertyUuid12 = IdGenerator.generate();
    propertyUuid21 = IdGenerator.generate();
    propertyUuid22 = IdGenerator.generate();
    
    propertyUuid311 = IdGenerator.generate();
    propertyUuid312 = IdGenerator.generate();
    
    WorkspaceStorageCache cacheProbe = (WorkspaceStorageCacheImpl) session.getContainer().getComponentInstanceOfType(WorkspaceStorageCacheImpl.class);
    assertNotNull("Cache is unaccessible (check access denied or configuration)", cacheProbe);
    assertTrue("Cache is disabled", cacheProbe.isEnabled());
    
    // new instance
    cache = new WorkspaceStorageCacheImpl(
        (CacheService) session.getContainer().getComponentInstanceOfType(CacheService.class),
        (WorkspaceEntry) session.getContainer().getComponentInstanceOfType(WorkspaceEntry.class)
        );
    assertNotNull("Cache is disabled (test cache)", cache);
  }
  
  private void initNodesData() throws RepositoryException {
    
    AccessControlList acl = ((NodeImpl) root).getACL();
    
    nodeData1 = new PersistedNodeData(nodeUuid1, nodePath1, rootUuid, 1, 0, Constants.NT_UNSTRUCTURED, null, acl);
    nodeData2 = new PersistedNodeData(nodeUuid2, nodePath2, rootUuid, 1, 1, Constants.NT_UNSTRUCTURED, null, acl);
    nodeData3 = new PersistedNodeData(nodeUuid3, nodePath3, rootUuid, 1, 2, Constants.NT_UNSTRUCTURED, null, acl);
    nodeData31 = new PersistedNodeData(nodeUuid31, nodePath31, nodeUuid3, 1, 0, Constants.NT_UNSTRUCTURED, null, acl);
    nodeData32 = new PersistedNodeData(nodeUuid32, nodePath32, nodeUuid3, 1, 1, Constants.NT_UNSTRUCTURED, null, acl);
    
    //nodeData31 = new TransientNodeData(nodePath31, nodeUuid31, 1, Constants.NT_UNSTRUCTURED, null, 1, nodeUuid3, new AccessControlList(true));
  }  
  
  private void initDataAsPersisted() {
    propertyData11 = new PersistedPropertyData(propertyUuid11, propertyPath11, nodeUuid1, 1, PropertyType.STRING, false);
    List<ValueData> stringData = new ArrayList<ValueData>(); 
    stringData.add(new ByteArrayPersistedValueData("property data 1".getBytes(), 0));
    stringData.add(new ByteArrayPersistedValueData("property data 2".getBytes(), 1));
    stringData.add(new ByteArrayPersistedValueData("property data 3".getBytes(), 2));
    try {
      ((PersistedPropertyData) propertyData11).setValues(stringData);
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    
    propertyData12 = new PersistedPropertyData(propertyUuid12, propertyPath12, nodeUuid1, 1, PropertyType.BINARY, false);
    List<ValueData> binData = new ArrayList<ValueData>(); 
    binData.add(new ByteArrayPersistedValueData("property data bin 1".getBytes(), 0));
    try {
      ((PersistedPropertyData) propertyData12).setValues(binData);
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    
    propertyData21 = new PersistedPropertyData(propertyUuid21, propertyPath21, nodeUuid2, 1, PropertyType.STRING, true);
    List<ValueData> stringData1 = new ArrayList<ValueData>(); 
    stringData1.add(new ByteArrayPersistedValueData("property data 1".getBytes(), 0));
    stringData1.add(new ByteArrayPersistedValueData("property data 2".getBytes(), 1));
    stringData1.add(new ByteArrayPersistedValueData("property data 3".getBytes(), 2));
    try {
      ((PersistedPropertyData) propertyData21).setValues(stringData1);
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    
    propertyData22 = new PersistedPropertyData(propertyUuid22, propertyPath22, nodeUuid2, 1, PropertyType.BOOLEAN, false);
    List<ValueData> booleanData = new ArrayList<ValueData>(); 
    booleanData.add(new ByteArrayPersistedValueData("true".getBytes(), 0));
    try {
      ((PersistedPropertyData) propertyData22).setValues(booleanData);
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    
    propertyData311 = new PersistedPropertyData(propertyUuid311, propertyPath311, nodeUuid31, 1, PropertyType.LONG, false);
    List<ValueData> longData = new ArrayList<ValueData>(); 
    longData.add(new ByteArrayPersistedValueData(new Long(123456).toString().getBytes(), 0));
    try {
      ((PersistedPropertyData) propertyData311).setValues(longData);
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
    
    propertyData312 = new PersistedPropertyData(propertyUuid312, propertyPath312, nodeUuid31, 1, PropertyType.REFERENCE, true);
    List<ValueData> refData = new ArrayList<ValueData>(); 
    refData.add(new ByteArrayPersistedValueData(nodeUuid1.getBytes(), 0));
    refData.add(new ByteArrayPersistedValueData(nodeUuid2.getBytes(), 1));
    refData.add(new ByteArrayPersistedValueData(nodeUuid3.getBytes(), 2));
    try {
      ((PersistedPropertyData) propertyData312).setValues(refData);
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  public void testGetItem_Persisted() throws Exception {

    initNodesData();
    initDataAsPersisted();
    
    cache.put(nodeData1);
    cache.put(nodeData2);
    cache.put(propertyData12);
    
    assertEquals("Cached node " + nodeData1.getQPath().getAsString() + " is not equals", 
        cache.get(rootUuid, nodePath1.getEntries()[nodePath1.getEntries().length - 1]), nodeData1);
    assertEquals("Cached node " + nodeData2.getQPath().getAsString() + " is not equals", 
        cache.get(rootUuid, nodePath2.getEntries()[nodePath2.getEntries().length - 1]), nodeData2);
    
    assertEquals("Cached node " + nodeData1.getIdentifier() + " is not equals", cache.get(nodeUuid1), nodeData1);
    assertEquals("Cached node " + nodeData2.getIdentifier() + " is not equals", cache.get(nodeUuid2), nodeData2);
    
    assertEquals("Cached property " + propertyPath12.getAsString() + " is not equals", 
        cache.get(nodeUuid1, propertyPath12.getEntries()[propertyPath12.getEntries().length - 1]), propertyData12);
    assertEquals("Cached property " + propertyData12.getIdentifier() + " is not equals", 
        cache.get(propertyUuid12), propertyData12);
  }
  
  public void testGetItems_Persisted() throws Exception {

    initNodesData();
    initDataAsPersisted();
    
    List<NodeData> nodes = new ArrayList<NodeData>();
    nodes.add(nodeData31);
    nodes.add(nodeData32);
    cache.addChildNodes(nodeData3, nodes);
    
    cache.put(nodeData1);
    cache.put(nodeData2);
    cache.put(propertyData12);
    
    List<PropertyData> properties2 = new ArrayList<PropertyData>();
    properties2.add(propertyData21);
    properties2.add(propertyData22);
    cache.addChildProperties(nodeData2, properties2);
    
    List<PropertyData> properties1 = new ArrayList<PropertyData>();
    properties1.add(propertyData11);
    properties1.add(propertyData12);
    cache.addChildProperties(nodeData1, properties1);
    
    // prev stuff
    assertEquals("Cached " + nodeData1.getQPath().getAsString() + " is not equals", 
        cache.get(rootUuid, nodePath1.getEntries()[nodePath1.getEntries().length - 1]), nodeData1);
    assertEquals("Cached " + nodeData2.getQPath().getAsString() + " is not equals", 
        cache.get(rootUuid, nodePath2.getEntries()[nodePath2.getEntries().length - 1]), nodeData2);
    assertEquals("Cached " + propertyData12.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid1, propertyPath12.getEntries()[propertyPath12.getEntries().length - 1]), propertyData12);
    
    // childs...
    // nodes
    assertEquals("Cached child node " + nodeData31.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid3, nodePath31.getEntries()[nodePath31.getEntries().length - 1]), nodeData31);
    assertEquals("Cached child node " + nodeData31.getIdentifier() + " is not equals", cache.get(nodeUuid31), nodeData31);
    
    assertEquals("Cached child node " + nodeData32.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid3, nodePath32.getEntries()[nodePath32.getEntries().length - 1]), nodeData32);
    assertEquals("Cached child node " + nodeData32.getIdentifier() + " is not equals", cache.get(nodeUuid32), nodeData32);
    
    assertTrue("Cached child node " + nodeData31.getQPath().getAsString() + " is not in the childs list", 
        cache.getChildNodes(nodeData3).contains(nodeData31));
    assertTrue("Cached child node " + nodeData32.getQPath().getAsString() + " is not in the childs list", 
        cache.getChildNodes(nodeData3).contains(nodeData32));
    
    assertEquals("Cached child nodes count is wrong", cache.getChildNodes(nodeData3).size(), 2);
    
    // props
    assertEquals("Cached child property " + propertyData11.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid1, propertyPath11.getEntries()[propertyPath11.getEntries().length - 1]), propertyData11);
    assertEquals("Cached child property " + propertyData11.getIdentifier() + " is not equals", cache.get(propertyUuid11), propertyData11);
    assertEquals("Cached child property " + propertyData12.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid1, propertyPath12.getEntries()[propertyPath12.getEntries().length - 1]), propertyData12);
    assertEquals("Cached child property " + propertyData12.getIdentifier() + " is not equals", cache.get(propertyUuid12), propertyData12);
    
    assertEquals("Cached child property " + propertyData21.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid2, propertyPath21.getEntries()[propertyPath21.getEntries().length - 1]), propertyData21);
    assertEquals("Cached child property " + propertyData21.getIdentifier() + " is not equals", cache.get(propertyUuid21), propertyData21);
    assertEquals("Cached child property " + propertyData22.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid2, propertyPath22.getEntries()[propertyPath22.getEntries().length - 1]), propertyData22);
    assertEquals("Cached child property " + propertyData22.getIdentifier() + " is not equals", cache.get(propertyUuid22), propertyData22);
    
    assertTrue("Cached child property " + propertyData11.getQPath().getAsString() + " is not in the childs list", cache.getChildProperties(nodeData1).contains(propertyData11));
    assertTrue("Cached child property " + propertyData12.getQPath().getAsString() + " is not in the childs list", cache.getChildProperties(nodeData1).contains(propertyData12));
    assertTrue("Cached child property " + propertyData21.getQPath().getAsString() + " is not in the childs list", cache.getChildProperties(nodeData2).contains(propertyData21));
    assertTrue("Cached child property " + propertyData22.getQPath().getAsString() + " is not in the childs list", cache.getChildProperties(nodeData2).contains(propertyData22));
    
    assertEquals("Cached child properties count is wrong", cache.getChildProperties(nodeData1).size(), 2);
    assertEquals("Cached child properties count is wrong", cache.getChildProperties(nodeData2).size(), 2);
  }
  
  public void testGetProperty_Persisted() throws Exception {

    initNodesData();
    initDataAsPersisted();
    
    List<NodeData> nodes = new ArrayList<NodeData>();
    nodes.add(nodeData31);
    nodes.add(nodeData32);
    cache.addChildNodes(nodeData3, nodes);
    
    cache.put(nodeData1);
    cache.put(nodeData2);
    cache.put(propertyData12);
    
    List<PropertyData> properties2 = new ArrayList<PropertyData>();
    properties2.add(propertyData21);
    properties2.add(propertyData22);
    cache.addChildProperties(nodeData2, properties2);
    
    List<PropertyData> properties1 = new ArrayList<PropertyData>();
    properties1.add(propertyData11);
    properties1.add(propertyData12);
    cache.addChildProperties(nodeData1, properties1);
    
    // props, prev stuff
    assertEquals("Cached child property " + propertyData11.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid1, propertyPath11.getEntries()[propertyPath11.getEntries().length - 1]), propertyData11);
    assertEquals("Cached child property " + propertyData11.getIdentifier() + " is not equals", cache.get(propertyUuid11), propertyData11);
    assertEquals("Cached child property " + propertyData12.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid1, propertyPath12.getEntries()[propertyPath12.getEntries().length - 1]), propertyData12);
    assertEquals("Cached child property " + propertyData12.getIdentifier() + " is not equals", cache.get(propertyUuid12), propertyData12);
    
    assertEquals("Cached child property " + propertyData21.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid2, propertyPath21.getEntries()[propertyPath21.getEntries().length - 1]), propertyData21);
    assertEquals("Cached child property " + propertyData21.getIdentifier() + " is not equals", cache.get(propertyUuid21), propertyData21);
    assertEquals("Cached child property " + propertyData22.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid2, propertyPath22.getEntries()[propertyPath22.getEntries().length - 1]), propertyData22);
    assertEquals("Cached child property " + propertyData22.getIdentifier() + " is not equals", cache.get(propertyUuid22), propertyData22);
    
    assertTrue("Cached child property " + propertyData11.getQPath().getAsString() + " is not in the childs list", cache.getChildProperties(nodeData1).contains(propertyData11));
    assertTrue("Cached child property " + propertyData12.getQPath().getAsString() + " is not in the childs list", cache.getChildProperties(nodeData1).contains(propertyData12));
    assertTrue("Cached child property " + propertyData21.getQPath().getAsString() + " is not in the childs list", cache.getChildProperties(nodeData2).contains(propertyData21));
    assertTrue("Cached child property " + propertyData22.getQPath().getAsString() + " is not in the childs list", cache.getChildProperties(nodeData2).contains(propertyData22));
    
    assertEquals("Cached child properties count is wrong", cache.getChildProperties(nodeData1).size(), 2);
    assertEquals("Cached child properties count is wrong", cache.getChildProperties(nodeData2).size(), 2);
    
    // remove
    cache.remove(propertyData12);
    
    // check
    assertEquals("Cached child property " + propertyData11.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid1, propertyPath11.getEntries()[propertyPath11.getEntries().length - 1]), propertyData11);
    assertEquals("Cached child property " + propertyData11.getIdentifier() + " is not equals", cache.get(propertyUuid11), propertyData11);

    // here
    assertNull("Child property " + propertyData12.getQPath().getAsString() + " is not in the cache", 
        cache.get(nodeUuid1, propertyPath12.getEntries()[propertyPath12.getEntries().length - 1]));
    assertNull("Child property " + propertyData12.getQPath().getAsString() + " is not in the cache", cache.get(propertyUuid12));
    
    assertEquals("Cached child property " + propertyData21.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid2, propertyPath21.getEntries()[propertyPath21.getEntries().length - 1]), propertyData21);
    assertEquals("Cached child property " + propertyData21.getIdentifier() + " is not equals", cache.get(propertyUuid21), propertyData21);
    assertEquals("Cached child property " + propertyData22.getQPath().getAsString() + " is not equals", 
        cache.get(nodeUuid2, propertyPath22.getEntries()[propertyPath22.getEntries().length - 1]), propertyData22);
    assertEquals("Cached child property " + propertyData22.getIdentifier() + " is not equals", cache.get(propertyUuid22), propertyData22);
    
    assertTrue("Cached child property " + propertyData11.getQPath().getAsString() + " is not in the childs list", cache.getChildProperties(nodeData1).contains(propertyData11));
    
    // here
    assertFalse("Cached child property " + propertyData12.getQPath().getAsString() + " is not in the childs list", cache.getChildProperties(nodeData1).contains(propertyData12));

    assertTrue("Cached child property " + propertyData21.getQPath().getAsString() + " is not in the childs list", cache.getChildProperties(nodeData2).contains(propertyData21));
    assertTrue("Cached child property " + propertyData22.getQPath().getAsString() + " is not in the childs list", cache.getChildProperties(nodeData2).contains(propertyData22));
    
    // and here
    assertEquals("Cached child properties count is wrong", cache.getChildProperties(nodeData1).size(), 1);
    
    assertEquals("Cached child properties count is wrong", cache.getChildProperties(nodeData2).size(), 2);
  }

  public void testRemoveChildNodesInCN() throws Exception {

    initNodesData();
    initDataAsPersisted();

    // the case here    
    List<NodeData> nodes = new ArrayList<NodeData>();
    nodes.add(nodeData31);
    nodes.add(nodeData32);
    cache.addChildNodes(nodeData3, nodes);
    
    // any stuff
    cache.put(nodeData1);
    cache.put(nodeData2);
    cache.put(propertyData12);
    
    List<PropertyData> properties2 = new ArrayList<PropertyData>();
    properties2.add(propertyData21);
    properties2.add(propertyData22);
    cache.addChildProperties(nodeData2, properties2);
    
    List<PropertyData> properties1 = new ArrayList<PropertyData>();
    properties1.add(propertyData11);
    properties1.add(propertyData12);
    cache.addChildProperties(nodeData1, properties1);
    
    // remove
    cache.remove(nodeData3); // remove node3 and its childs (31, 32) 
    
    // check
    assertNull("Node " + nodeData3.getQPath().getAsString() + " in the cache", cache.get(nodeUuid3));
    
    assertNull("Child node " + nodeData31.getQPath().getAsString() + " in the cache", cache.get(nodeUuid31));
    assertNull("Child node " + nodeData32.getQPath().getAsString() + " in the cache", cache.get(nodeUuid32));    
  }
  
  public void testRemoveChildNodes() throws Exception {

    initNodesData();
    initDataAsPersisted();
    
    // the case here
    cache.put(nodeData3);
    cache.put(nodeData31);
    cache.put(nodeData32);

    // any stuff    
    cache.put(nodeData1);
    cache.put(nodeData2);
    cache.put(propertyData12);
    
    List<PropertyData> properties2 = new ArrayList<PropertyData>();
    properties2.add(propertyData21);
    properties2.add(propertyData22);
    cache.addChildProperties(nodeData2, properties2);
    
    List<PropertyData> properties1 = new ArrayList<PropertyData>();
    properties1.add(propertyData11);
    properties1.add(propertyData12);
    cache.addChildProperties(nodeData1, properties1);
    
    // remove
    cache.remove(nodeData3); // remove node3 and its childs (31, 32) 
    
    // check
    assertNull("Node " + nodeData3.getQPath().getAsString() + " in the cache", cache.get(nodeUuid3));
    
    assertNull("Child node " + nodeData31.getQPath().getAsString() + " in the cache", cache.get(nodeUuid31));
    assertNull("Child node " + nodeData32.getQPath().getAsString() + " in the cache", cache.get(nodeUuid32));    
  }
  
}