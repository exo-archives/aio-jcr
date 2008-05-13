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
package org.exoplatform.services.jcr.ext.audit;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.observation.Event;

import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.command.action.ActionMatcher;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.ext.action.SessionActionCatalog;
import org.exoplatform.services.jcr.impl.ext.action.SessionEventMatcher;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.services.jcr.observation.ExtendedEventType;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Gennady Azarenkov
 * @version $Id: AuditServiceTest.java 12164 2007-01-22 08:39:22Z geaz $
 */

public class TestAuditService extends BaseStandaloneTest {

  protected static final String        ROOT_PATH = "AuditServiceTest";

  protected AuditService               service;

  protected SessionActionCatalog       catalog;

  protected Session                    exo1Session;

  protected Session                    adminSession;

  protected NodeImpl                   auditServiceTestRoot;

  protected Map<ActionMatcher, Action> oldActions;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    service = (AuditService) container.getComponentInstanceOfType(AuditService.class);
    catalog = (SessionActionCatalog) session.getContainer()
                                            .getComponentInstanceOfType(SessionActionCatalog.class);
    exo1Session = repository.login(new SimpleCredentials("exo1", "exo1".toCharArray()));
    adminSession = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
    NodeImpl rootAdmin = (NodeImpl) adminSession.getRootNode();
    rootAdmin.setPermission("admin", PermissionType.ALL); // exo
    rootAdmin.removePermission(SystemIdentity.ANY);
    rootAdmin.setPermission(SystemIdentity.ANY, new String[] { PermissionType.READ }); // exo
    rootAdmin.save();

    auditServiceTestRoot = (NodeImpl) rootAdmin.addNode(ROOT_PATH);
    auditServiceTestRoot.addMixin("exo:privilegeable");
    auditServiceTestRoot.setPermission(SystemIdentity.ANY, PermissionType.ALL);
    rootAdmin.save();

    auditServiceTestRoot = (NodeImpl) root.getNode(ROOT_PATH);
    // save actions list
    oldActions = new HashMap<ActionMatcher, Action>();

    for (Entry<ActionMatcher, Action> entry : catalog.getAllEntries().entrySet()) {
      oldActions.put(entry.getKey(), entry.getValue());
    }
    catalog.clear();

  }

  /**
   * Test automatically make node exo:auditable
   * 
   * @throws Exception
   */
  public void testAddAuditHistoryAction() throws Exception {
    // Should not be autocreated

    NodeImpl rootNode = (NodeImpl) session.getRootNode().getNode(ROOT_PATH);

    SessionEventMatcher addAuditableHandler = new SessionEventMatcher(Event.NODE_ADDED,
                                                                      new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                        new InternalQName("",
                                                                                                                          "testAddAuditHistoryAction")) },
                                                                      true,
                                                                      new String[] { session.getWorkspace()
                                                                                            .getName() },
                                                                      null);
    SessionEventMatcher removeHandler = new SessionEventMatcher(Event.NODE_REMOVED,
                                                                new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                  new InternalQName("",
                                                                                                                    "testAddAuditHistoryAction")) },
                                                                true,
                                                                new String[] { session.getWorkspace()
                                                                                      .getName() },
                                                                null);

    catalog.addAction(addAuditableHandler, new AddAuditableAction());
    catalog.addAction(removeHandler, new RemoveAuditableAction());

    ExtendedNode node = (ExtendedNode) rootNode.addNode("testAddAuditHistoryAction");
    // node.addMixin("exo:auditable");
    session.save();
    String auditHistoryUUID = node.getProperty("exo:auditHistory").getString();
    Node auditHistory = session.getNodeByUUID(auditHistoryUUID);

    assertTrue(auditHistory.isNodeType("exo:auditHistory"));

    // pointed to target node
    assertEquals(auditHistory.getProperty("exo:targetNode").getString(), node.getUUID());

    assertEquals("1", auditHistory.getProperty("exo:lastRecord").getString());

    assertEquals(1, auditHistory.getNodes().getSize());

    session.save();
    service.removeHistory(node);
    node.remove();
    session.save();

    try {
      session.getNodeByUUID(auditHistoryUUID);
      fail("History should be removed");
    } catch (ItemNotFoundException e) {

    }
  }

  /**
   * Test automatically create audit history after add exo:auditable mixin
   * 
   * @throws Exception
   */
  public void testAddAuditHistoryMixinAction() throws Exception {
    NodeImpl rootNode = (NodeImpl) session.getRootNode().getNode(ROOT_PATH);
    SessionEventMatcher addAuditableHandler = new SessionEventMatcher(ExtendedEvent.ADD_MIXIN,
                                                                      new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                        new InternalQName("",
                                                                                                                          "testAddAuditHistoryMixinAction")) },
                                                                      true,
                                                                      new String[] { session.getWorkspace()
                                                                                            .getName() },
                                                                      new InternalQName[] { AuditService.EXO_AUDITABLE });

    SessionEventMatcher propertiesHandler = new SessionEventMatcher(Event.PROPERTY_ADDED
                                                                        | Event.PROPERTY_CHANGED
                                                                        | Event.PROPERTY_REMOVED
                                                                        | Event.NODE_ADDED,
                                                                    new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                      new InternalQName("",
                                                                                                                        "testAddAuditHistoryMixinAction")) },
                                                                    true,
                                                                    new String[] { session.getWorkspace()
                                                                                          .getName() },
                                                                    new InternalQName[] { AuditService.EXO_AUDITABLE });
    SessionEventMatcher removeHandler = new SessionEventMatcher(Event.NODE_REMOVED,
                                                                new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                  new InternalQName("",
                                                                                                                    "testAddAuditHistoryMixinAction")) },
                                                                true,
                                                                new String[] { session.getWorkspace()
                                                                                      .getName() },
                                                                null

    );
    catalog.addAction(addAuditableHandler, new AddAuditableAction());
    catalog.addAction(propertiesHandler, new AuditAction());
    catalog.addAction(removeHandler, new RemoveAuditableAction());
    // Should not be autocreated
    ExtendedNode node = (ExtendedNode) rootNode.addNode("testAddAuditHistoryMixinAction",
                                                        "nt:unstructured");
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
    service.removeHistory(node);
    node.remove();
    session.save();

    try {
      session.getNodeByUUID(auditHistoryUUID);
      fail("History should be removed");
    } catch (ItemNotFoundException e) {

    }
  }

  /**
   * Test add informations to audit storage
   * 
   * @throws RepositoryException
   */
  public void testAddInfoToAuditStorage() throws RepositoryException {

    ExtendedNode node = null;
    try {
      node = (ExtendedNode) session.getRootNode().getNode(ROOT_PATH).addNode("testaudit");
      node.addMixin("exo:auditable");
      if (!service.hasHistory(node))
        service.createHistory(node);
      session.save();
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail("Fail to init AuditStorage" + e.getLocalizedMessage());
    }
    Property property1 = node.setProperty("property1", "value1");
    service.addRecord(node, Event.NODE_ADDED);
    service.addRecord(property1, Event.PROPERTY_ADDED);

    Node auditHistory = session.getNodeByUUID(node.getProperty("exo:auditHistory").getString());
    assertNotNull(auditHistory);
    assertEquals(2, auditHistory.getNodes().getSize());

    assertTrue(service.hasHistory(node));
    assertNotNull(service.getHistory(node));
  }

  public void testAuditHistory() throws Exception {
    NodeImpl rootNode = (NodeImpl) session.getRootNode().getNode(ROOT_PATH);

    SessionEventMatcher addAuditableHandler = new SessionEventMatcher(Event.NODE_ADDED,
                                                                      new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                        new InternalQName("",
                                                                                                                          "testAuditHistory")) },
                                                                      true,
                                                                      new String[] { session.getWorkspace()
                                                                                            .getName() },
                                                                      null);

    SessionEventMatcher propertiesHandler = new SessionEventMatcher(Event.PROPERTY_ADDED
                                                                        | Event.PROPERTY_CHANGED
                                                                        | Event.PROPERTY_REMOVED
                                                                        | Event.NODE_ADDED,
                                                                    new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                      new InternalQName("",
                                                                                                                        "testAuditHistory")) },
                                                                    true,
                                                                    new String[] { session.getWorkspace()
                                                                                          .getName() },
                                                                    new InternalQName[] { AuditService.EXO_AUDITABLE });

    SessionEventMatcher removeHandler = new SessionEventMatcher(Event.NODE_REMOVED,
                                                                new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                  new InternalQName("",
                                                                                                                    "testAuditHistory")) },
                                                                true,
                                                                new String[] { session.getWorkspace()
                                                                                      .getName() },
                                                                null

    );

    catalog.addAction(addAuditableHandler, new AddAuditableAction());
    catalog.addAction(propertiesHandler, new AuditAction());
    catalog.addAction(removeHandler, new RemoveAuditableAction());
    // Should not be autocreated
    ExtendedNode node = (ExtendedNode) rootNode.addNode("testAuditHistory", "nt:unstructured");
    // node.addMixin("exo:auditable");
    String auditHistoryUUID = node.getProperty("exo:auditHistory").getString();
    Node auditHistory = session.getNodeByUUID(auditHistoryUUID);

    // under audit
    node.setProperty("test", "testValue");
    assertTrue(auditHistory.isNodeType("exo:auditHistory"));
    node.getProperty("test").remove();
    session.save();

    assertTrue(service.hasHistory(node));
    assertFalse(service.hasHistory(session.getRootNode()));

    AuditHistory history = service.getHistory(node);
    assertTrue(node.isSame(history.getAuditableNode()));
    assertEquals(3, history.getAuditRecords().size());

    assertEquals(Event.PROPERTY_ADDED, history.getAuditRecords().get(1).getEventType());
    assertEquals(ExtendedEventType.PROPERTY_ADDED, history.getAuditRecords()
                                                          .get(1)
                                                          .getEventTypeName());
    assertEquals(new InternalQName(null, "test"), history.getAuditRecords()
                                                         .get(1)
                                                         .getPropertyName());
    assertEquals(Event.PROPERTY_REMOVED, history.getAuditRecords().get(2).getEventType());
    assertEquals(ExtendedEventType.PROPERTY_REMOVED, history.getAuditRecords()
                                                            .get(2)
                                                            .getEventTypeName());
    session.save();

  }

  /**
   * Test check permissions ion audit storage
   * 
   * @throws Exception
   */
  public void testCheckPermissions() throws Exception {
    // user
    NodeImpl rootNode = (NodeImpl) session.getRootNode().getNode(ROOT_PATH);
    ExtendedNode node = (ExtendedNode) rootNode.addNode("testAuditHistory", "nt:unstructured");
    node.addMixin("exo:auditable");
    if (!service.hasHistory(node))
      service.createHistory(node);
    session.save();
    service.addRecord(node, Event.NODE_ADDED);
    session.save();

    // user
    NodeImpl rootNode1 = (NodeImpl) exo1Session.getRootNode().getNode(ROOT_PATH);
    // Should not be autocreated
    ExtendedNode node1 = (ExtendedNode) rootNode1.addNode("testAuditHistory", "nt:unstructured");
    node1.addMixin("exo:auditable");
    if (!service.hasHistory(node1))
      service.createHistory(node1);
    exo1Session.save();
    service.addRecord(node1, Event.NODE_ADDED);
    exo1Session.save();

    try {
      service.removeHistory(node1);
      exo1Session.save();
    } catch (AccessDeniedException e) {
      fail();
    }

    // admin
    Node auditStorage = adminSession.getNodeByUUID(AuditService.AUDIT_STORAGE_ID);
    for (NodeIterator nIterator = auditStorage.getNodes(); nIterator.hasNext();) {
      nIterator.nextNode();

    }
    Node adminTestAuditHistoryNode = adminSession.getRootNode()
                                                 .getNode(ROOT_PATH)
                                                 .getNode("testAuditHistory");
    assertTrue(service.hasHistory(adminTestAuditHistoryNode));
    service.removeHistory(adminTestAuditHistoryNode);
    adminSession.save();
  }

  /**
   * Test creating and removing item from storage
   * 
   * @throws RepositoryException
   */
  public void testCreateAndRemoveStorage() throws RepositoryException {

    ExtendedNode node = null;
    try {
      node = (ExtendedNode) session.getRootNode().getNode(ROOT_PATH).addNode("teststotage");
      node.addMixin("exo:auditable");
      if (!service.hasHistory(node))
        service.createHistory(node);
      session.save();
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail("Fail to init AuditStorage" + e.getLocalizedMessage());
    }
    Node auditHistory = session.getNodeByUUID(node.getProperty("exo:auditHistory").getString());
    assertNotNull("Audit history does'n created correctly", auditHistory);
  }

  /**
   * Test repository configuration
   * 
   * @throws Exception
   */
  public void testIfAuditServiceConfigured() throws Exception {

    assertNotNull(container.getComponentInstanceOfType(AuditService.class));
    assertNotNull(repository.getNodeTypeManager().getNodeType("exo:auditable"));
    assertNotNull(repository.getNodeTypeManager().getNodeType("exo:auditRecord"));
    assertNotNull(repository.getNodeTypeManager().getNodeType("exo:auditHistory"));
    assertNotNull(repository.getNodeTypeManager().getNodeType("exo:auditStorage"));
  }

  /**
   * Test creating audit storage
   */
  public void testIfAuditStorageCreated() {

    AuditService service = (AuditService) container.getComponentInstanceOfType(AuditService.class);
    ExtendedNode node = null;
    try {
      node = (ExtendedNode) adminSession.getRootNode().getNode(ROOT_PATH).addNode("auditablenode");
      node.addMixin("exo:auditable");
    } catch (RepositoryException e) {
      fail("Fail to add node or add mixin");
    }
    try {
      service.createHistory(node);
      adminSession.save();
      assertNotNull(adminSession.getNodeByUUID(AuditService.AUDIT_STORAGE_ID));
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail("Fail to create AUDIT_STORAGE " + e.getLocalizedMessage());
    }

    try {
      assertNotNull(adminSession.getNodeByUUID(node.getProperty("exo:auditHistory").getString()));
    } catch (RepositoryException e) {
      fail("Fail to create AUDITHISTORY");
    }
  }

  /**
   * Test reading information from audit storage.
   * 
   * @throws Exception
   */
  public void testReadHistory() throws Exception {
    NodeImpl rootNode = (NodeImpl) session.getRootNode().getNode(ROOT_PATH);
    ExtendedNode node = (ExtendedNode) rootNode.addNode("testReadHistory", "nt:unstructured");
    node.addMixin("exo:auditable");
    if (!service.hasHistory(node))
      service.createHistory(node);
    session.save();
    service.addRecord(node, Event.NODE_ADDED);
    session.save();

    service.getHistory(node).getAuditRecords();
    Node node2 = exo1Session.getRootNode().getNode(ROOT_PATH).getNode("testReadHistory");
    service.getHistory(node2).getAuditRecords();

  }

  /**
   * Tests remove auditable action
   * 
   * @throws Exception
   */
  public void testRemoveAuditable() throws Exception {
    NodeImpl rootNode = (NodeImpl) adminSession.getRootNode().getNode(ROOT_PATH);
    SessionEventMatcher addAuditableHandler = new SessionEventMatcher(Event.NODE_ADDED,
                                                                      new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                        new InternalQName("",
                                                                                                                          "testRemoveAudit")) },
                                                                      true,
                                                                      new String[] { session.getWorkspace()
                                                                                            .getName() },
                                                                      null);

    SessionEventMatcher removeHandler = new SessionEventMatcher(Event.NODE_REMOVED,
                                                                new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                  new InternalQName("",
                                                                                                                    "testRemoveAudit")) },
                                                                true,
                                                                new String[] { session.getWorkspace()
                                                                                      .getName() },
                                                                null

    );
    catalog.addAction(addAuditableHandler, new AddAuditableAction());
    catalog.addAction(removeHandler, new RemoveAuditableAction());
    ExtendedNode node = (ExtendedNode) rootNode.addNode("testRemoveAudit", "nt:unstructured");
    adminSession.save();
    assertTrue(node.isNodeType("exo:auditable"));
    String history = node.getProperty("exo:auditHistory").getString();
    assertNotNull(session.getNodeByUUID(history));
    node.remove();
    adminSession.save();

    try {
      adminSession.getNodeByUUID(history);
      fail("History doesn't removed");
    } catch (ItemNotFoundException e) {
      // ok
    }
  }

  /**
   * Test check correct add audit information after changing property
   * 
   * @throws Exception
   */
  public void testRemovePropertyAudit() throws Exception {

    NodeImpl rootNode = (NodeImpl) session.getRootNode().getNode(ROOT_PATH);

    SessionEventMatcher addAuditableHandler = new SessionEventMatcher(Event.NODE_ADDED,
                                                                      new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                        new InternalQName("",
                                                                                                                          "testRemovePropertyAudit")) },
                                                                      true,
                                                                      new String[] { session.getWorkspace()
                                                                                            .getName() },
                                                                      null);

    SessionEventMatcher propertiesHandler = new SessionEventMatcher(Event.PROPERTY_ADDED
                                                                        | Event.PROPERTY_CHANGED
                                                                        | Event.PROPERTY_REMOVED
                                                                        | Event.NODE_ADDED,
                                                                    new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                      new InternalQName("",
                                                                                                                        "testRemovePropertyAudit")) },
                                                                    true,
                                                                    new String[] { session.getWorkspace()
                                                                                          .getName() },

                                                                    new InternalQName[] { AuditService.EXO_AUDITABLE });

    SessionEventMatcher removeHandler = new SessionEventMatcher(Event.NODE_REMOVED,
                                                                new QPath[] { QPath.makeChildPath(rootNode.getInternalPath(),
                                                                                                  new InternalQName("",
                                                                                                                    "testRemovePropertyAudit")) },
                                                                true,
                                                                new String[] { session.getWorkspace()
                                                                                      .getName() },
                                                                null

    );

    catalog.addAction(addAuditableHandler, new AddAuditableAction());
    catalog.addAction(propertiesHandler, new AuditAction());
    catalog.addAction(removeHandler, new RemoveAuditableAction());

    // Should not be autocreated
    ExtendedNode node = (ExtendedNode) rootNode.addNode("testRemovePropertyAudit",
                                                        "nt:unstructured");
    String auditHistoryUUID = node.getProperty("exo:auditHistory").getString();
    Node auditHistory = session.getNodeByUUID(auditHistoryUUID);

    // under audit
    node.setProperty("test", "testValue");

    assertTrue(auditHistory.isNodeType("exo:auditHistory"));

    node.getProperty("test").remove();

    // pointed to target node
    assertEquals(auditHistory.getProperty("exo:targetNode").getString(), node.getUUID());
    // PROPERTY_ADDED
    // PROPERTY_REMOVED
    // NODE_ADDED

    assertEquals("3", auditHistory.getProperty("exo:lastRecord").getString());

    assertEquals(3, auditHistory.getNodes().getSize());

    session.save();
    service.removeHistory(node);
    node.remove();
    session.save();

    try {
      session.getNodeByUUID(auditHistoryUUID);
      fail("History should be removed");
    } catch (ItemNotFoundException e) {
      // ok

    }

  }

  @Override
  protected void tearDown() throws Exception {
    exo1Session.logout();
    exo1Session = null;

    ((NodeImpl) adminSession.getRootNode()).setPermission(SystemIdentity.ANY, PermissionType.ALL); // exo
    try {
      Node auditStorage = adminSession.getNodeByUUID(AuditService.AUDIT_STORAGE_ID);
      adminSession.save();
      auditStorage.remove();
    } catch (ItemNotFoundException e) {
    }
    adminSession.save();
    adminSession.getRootNode().getNode(ROOT_PATH).remove();
    adminSession.save();
    adminSession.logout();
    adminSession = null;

    catalog.clear();
    // restore old actions
    for (Entry<ActionMatcher, Action> entry : oldActions.entrySet()) {
      catalog.addAction(entry.getKey(), entry.getValue());
    }

    super.tearDown();

  }
}
