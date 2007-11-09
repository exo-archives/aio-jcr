/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.audit;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.observation.ExtendedEvent;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: AuditServiceTest.java 12164 2007-01-22 08:39:22Z geaz $
 *
 * Prerequisites:
                  <value>
                    <object type="org.exoplatform.services.jcr.impl.ext.action.ActionConfiguration">
                      <field  name="eventTypes"><string>addProperty,changeProperty,removeProperty</string></field> 
                      <field  name="path"><string>/AuditServiceTest</string></field>
                      <field  name="parentNodeType"><string>exo:auditable</string></field>
                      <field  name="isDeep"><boolean>false</boolean></field>
                      <field  name="actionClassName"><string>org.exoplatform.services.jcr.ext.audit.AuditAction</string></field>
                    </object>
                  </value>
                  <value>
                    <object type="org.exoplatform.services.jcr.impl.ext.action.ActionConfiguration">
                      <field  name="eventTypes"><string>addMixin</string></field> 
                      <field  name="path"><string>/AuditServiceTest</string></field>
                      <field  name="nodeType"><string>exo:auditable</string></field>
                      <field  name="isDeep"><boolean>true</boolean></field>
                      <field  name="actionClassName"><string>org.exoplatform.services.jcr.ext.audit.AuditAction</string></field>
                    </object>
                  </value>

 */

public class AuditServiceTest extends BaseStandaloneTest {
  
  private static final String ROOT_PATH = "AuditServiceTest";
  private static final String AUTO_ROOT_NAME = "autoAdd";
  private AuditService service ;
  private Node rootNode = null;
  
  public void setUp() throws Exception {
    super.setUp();
    service = (AuditService)container.getComponentInstanceOfType(AuditService.class);
    if(session.getRootNode().hasNode(ROOT_PATH))
      rootNode = session.getRootNode().getNode(ROOT_PATH);
    else
      rootNode = session.getRootNode().addNode(ROOT_PATH);
  }

  public void testIfAuditServiceConfigured() throws Exception {

    assertNotNull(container.getComponentInstanceOfType(AuditService.class));
    assertNotNull(repository.getNodeTypeManager().getNodeType("exo:auditable"));
    assertNotNull(repository.getNodeTypeManager().getNodeType("exo:auditRecord"));
    assertNotNull(repository.getNodeTypeManager().getNodeType("exo:auditHistory"));
    assertNotNull(repository.getNodeTypeManager().getNodeType("exo:auditStorage"));
  }

  public void testAddInfoToAuditStorage() throws RepositoryException {
    AuditService service = (AuditService)container.getComponentInstanceOfType(AuditService.class);
    ExtendedNode node = null;
    try {
      node = (ExtendedNode)session.getRootNode().addNode("testaudit");
      node.addMixin("exo:auditable");
      service.createHistory(node);
      session.save();
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail("Fail to init AuditStorage"+e.getLocalizedMessage());
    }
    Property property1 = node.setProperty("property1","value1");
    service.addRecord(node,ExtendedEvent.NODE_ADDED);
    service.addRecord(property1,ExtendedEvent.PROPERTY_ADDED);
    
    Node auditHistory = session.getNodeByUUID(node.getProperty("exo:auditHistory").getString()); 
    assertNotNull(auditHistory);
    assertEquals(2,auditHistory.getNodes().getSize());
    AuditHistory history = service.getHistory(node);
  }
  
  
  public void testCreateAndRemoveStorage() throws RepositoryException {
    
    ExtendedNode node = null;
    try {
      node = (ExtendedNode)session.getRootNode().addNode("teststotage");
      node.addMixin("exo:auditable");
      service.createHistory(node);
      session.save();
    } catch (RepositoryException e) {
      fail("Fail to init AuditStorage");
    }
    Node auditHistory = session.getNodeByUUID(node.getProperty("exo:auditHistory").getString()); 
    assertNotNull("Audit history does'n created correctly",auditHistory);
    service.removeHistory(node);
    try {
      auditHistory = session.getNodeByUUID(node.getProperty("exo:auditHistory").getString());
      fail("Audit history does'n removed correctly");
    } catch (RepositoryException e) {
       //OK
    }
  }

  public void testIfAuditStorageCreated() {

    AuditService service = (AuditService)container.getComponentInstanceOfType(AuditService.class);
    ExtendedNode node = null;
    try {
      node = (ExtendedNode)session.getRootNode().addNode("auditablenode");
      node.addMixin("exo:auditable");
    } catch (RepositoryException e) {
      fail("Fail to add node or add mixin");
    }
    try {
      service.createHistory(node);
      session.save();
      assertNotNull(session.getNodeByUUID(AuditService.AUDIT_STORAGE_ID));
    } catch (RepositoryException e) {
      fail("Fail to create AUDIT_STORAGE");
    }
    
    try {
      assertNotNull(session.getNodeByUUID(node.getProperty("exo:auditHistory").getString()));
    } catch (RepositoryException e) {
      fail("Fail to create AUDITHISTORY");
    }
  }
  
  public void testAddAuditHistory() throws Exception {
    // Should not be autocreated
    ExtendedNode node = (ExtendedNode)rootNode.addNode("testAddAuditHistory");
    node.addMixin("exo:auditable");
    session.save();
    String auditHistoryUUID = node.getProperty("exo:auditHistory").getString();
    Node auditHistory = session.getNodeByUUID(auditHistoryUUID);
    
    assertTrue(auditHistory.isNodeType("exo:auditHistory"));
    
    // pointed to target node
    assertEquals(auditHistory.getProperty("exo:targetNode").getString(), node.getUUID());

    assertEquals("1", auditHistory.getProperty("exo:lastRecord").getString());

    assertEquals(1, auditHistory.getNodes().getSize());
    
    session.save();

  }

  public void testSetPropertyAudit() throws Exception {
    // Should not be autocreated
    ExtendedNode node = (ExtendedNode)rootNode.addNode("testSetPropertyAudit", "nt:unstructured");
    node.addMixin("exo:auditable");
    String auditHistoryUUID = node.getProperty("exo:auditHistory").getString();
    Node auditHistory = session.getNodeByUUID(auditHistoryUUID);
    
    // under audit
    node.setProperty("test", "testValue");
    
    assertTrue(auditHistory.isNodeType("exo:auditHistory"));
    
    // pointed to target node
    assertEquals(auditHistory.getProperty("exo:targetNode").getString(), node.getUUID());

    assertEquals("2", auditHistory.getProperty("exo:lastRecord").getString());

    assertEquals(2, auditHistory.getNodes().getSize());

    session.save();
  }
  
  public void testRemovePropertyAudit() throws Exception {
    // Should not be autocreated
    ExtendedNode node = (ExtendedNode)rootNode.addNode("testRemovePropertyAudit", "nt:unstructured");
    node.addMixin("exo:auditable");
    String auditHistoryUUID = node.getProperty("exo:auditHistory").getString();
    Node auditHistory = session.getNodeByUUID(auditHistoryUUID);
    
    // under audit
    node.setProperty("test", "testValue");
    
    assertTrue(auditHistory.isNodeType("exo:auditHistory"));
    
    node.getProperty("test").remove();
    
    
    // pointed to target node
    assertEquals(auditHistory.getProperty("exo:targetNode").getString(), node.getUUID());

    assertEquals("3", auditHistory.getProperty("exo:lastRecord").getString());

    assertEquals(3, auditHistory.getNodes().getSize());

    session.save();
  }

  public void testAuditHistory() throws Exception {
    // Should not be autocreated
    ExtendedNode node = (ExtendedNode)rootNode.addNode("testAuditHistory", "nt:unstructured");
    node.addMixin("exo:auditable");
    String auditHistoryUUID = node.getProperty("exo:auditHistory").getString();
    Node auditHistory = session.getNodeByUUID(auditHistoryUUID);
    
    // under audit
    node.setProperty("test", "testValue");
    assertTrue(auditHistory.isNodeType("exo:auditHistory"));
    node.getProperty("test").remove();
    session.save();
    
    AuditService service = (AuditService)container.getComponentInstanceOfType(AuditService.class);
    
    assertTrue(service.hasHistory(node));
    assertFalse(service.hasHistory(session.getRootNode()));
    
    AuditHistory history = service.getHistory(node);
    assertTrue(node.isSame(history.getAuditableNode()));
    assertEquals(3, history.getAuditRecords().size());
    // records are ordered
    assertEquals(ExtendedEvent.ADD_MIXIN, history.getAuditRecords().get(0).getEventType());
    assertEquals(ExtendedEvent.PROPERTY_ADDED, history.getAuditRecords().get(1).getEventType());
    assertEquals(new InternalQName(null, "test"), history.getAuditRecords().get(1).getPropertyName());
    assertEquals(ExtendedEvent.PROPERTY_REMOVED, history.getAuditRecords().get(2).getEventType());

  }

  /**
   * @throws RepositoryException
   * Prerequisites:
   *               <value>
                    <object type="org.exoplatform.services.jcr.impl.ext.action.ActionConfiguration">
                      <field  name="eventTypes"><string>addNode</string></field>
                      <field  name="path"><string>/AuditServiceTest/autoAdd</string></field>       
                      <field  name="isDeep"><boolean>true</boolean></field>       
                      <field  name="actionClassName"><string>org.exoplatform.services.jcr.ext.audit.AddAuditableAction</string></field>       
                    </object>
                  </value>

   * 
   */
  public void testAutoAddAuditable() throws Exception {
    Node node = rootNode.addNode(AUTO_ROOT_NAME, "nt:unstructured");
    assertTrue(node.isNodeType("exo:auditable"));
  }
  
  /**
   * @throws RepositoryException
   * Prerequisites:
                    <value>
                      <object type="org.exoplatform.services.jcr.impl.ext.action.ActionConfiguration">
                        <field  name="eventTypes"><string>removeNode</string></field>
                        <field  name="nodeTypes"><string>exo:auditable</string></field>
                        <field  name="isDeep"><boolean>false</boolean></field>
                        <field  name="actionClassName"><string>org.exoplatform.services.jcr.ext.audit.RemoveAuditableAction</string></field>
                      </object>
                    </value>

   * 
   */
  
  public void testRemoveAuditable() throws Exception {
    
    ExtendedNode node = (ExtendedNode)rootNode.addNode("testRemoveAudit", "nt:unstructured");
    node.addMixin("exo:auditable");
    session.save();
    assertTrue(node.isNodeType("exo:auditable"));
    String history = node.getProperty("exo:auditHistory").getString();
    assertNotNull(session.getNodeByUUID(history));
    node.remove();
    session.save();
    try {
      session.getNodeByUUID(history);
      fail("History doesnt removed");
    } catch (ItemNotFoundException e) {
      //ok
    }
  }
}
