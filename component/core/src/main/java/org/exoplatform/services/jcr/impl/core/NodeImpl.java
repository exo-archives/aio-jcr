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
package org.exoplatform.services.jcr.impl.core;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.Item;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.MergeException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.OnParentVersionAction;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.ItemDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionData;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.Identifier;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.itemfilters.ItemFilter;
import org.exoplatform.services.jcr.impl.core.itemfilters.NamePatternFilter;
import org.exoplatform.services.jcr.impl.core.lock.LockImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeDefinitionImpl;
import org.exoplatform.services.jcr.impl.core.value.BaseValue;
import org.exoplatform.services.jcr.impl.core.version.ItemDataMergeVisitor;
import org.exoplatform.services.jcr.impl.core.version.VersionHistoryImpl;
import org.exoplatform.services.jcr.impl.core.version.VersionImpl;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataRemoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.ValueDataConvertor;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;
import org.exoplatform.services.jcr.impl.dataflow.session.TransactionableDataManager;
import org.exoplatform.services.jcr.impl.dataflow.version.VersionHistoryDataHelper;
import org.exoplatform.services.jcr.impl.util.EntityCollection;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: NodeImpl.java 14520 2008-05-20 13:42:15Z pnedonosko $
 */

public class NodeImpl extends ItemImpl implements ExtendedNode {

  protected static final Log      LOG = ExoLogger.getLogger("jcr.NodeImpl");

  protected final LocationFactory sysLocFactory;

  protected NodeDefinitionData    definition;

  protected NodeDefinition        nodeDefinition;

  /**
   * NodeImpl constructor.
   * 
   * @param data Node data
   * @param session Session
   * @throws RepositoryException if error occurs during the Node data loading
   */
  public NodeImpl(NodeData data, SessionImpl session) throws RepositoryException {
    super(data, session);
    this.sysLocFactory = session.getSystemLocationFactory();
    loadData(data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void loadData(ItemData data) throws RepositoryException,
                                     InvalidItemStateException,
                                     ConstraintViolationException {

    if (data == null)
      throw new InvalidItemStateException("Data is null for " + this.getPath()
          + " Probably was deleted by another session and can not be loaded from container ");

    if (!data.isNode())
      throw new RepositoryException("Load data failed: Node expected");

    NodeData nodeData = (NodeData) data;

    // TODO do we need this three checks here?
    if (nodeData.getPrimaryTypeName() == null)
      throw new RepositoryException("Load data: NodeData has no primaryTypeName. Null value found. "
          + (nodeData.getQPath() != null ? nodeData.getQPath().getAsString() : "[null path node]")
          + " " + nodeData);

    if (nodeData.getMixinTypeNames() == null)
      throw new RepositoryException("Load data: NodeData has no mixinTypeNames. Null value found. "
          + (nodeData.getQPath() != null ? nodeData.getQPath().getAsString() : "[null path node]"));

    if (nodeData.getACL() == null)
      throw new RepositoryException("ACL is NULL " + nodeData.getQPath().getAsString());

    this.data = nodeData;
    this.location = session.getLocationFactory().createJCRPath(getData().getQPath());

    initDefinition();
  }

  /**
   * Init NodeDefinition.
   * 
   * @throws RepositoryException if error occurs
   * @throws ConstraintViolationException if definition not found
   */
  private void initDefinition() throws RepositoryException, ConstraintViolationException {

    if (this.isRoot()) { // root - no parent
      this.definition = new NodeDefinitionData(null,
                                               null,
                                               true,
                                               true,
                                               OnParentVersionAction.ABORT,
                                               true,
                                               new InternalQName[] { Constants.NT_BASE },
                                               null,
                                               false);
      return;
    }

    NodeData parent = (NodeData) dataManager.getItemData(getParentIdentifier());

    this.definition = session.getWorkspace()
                             .getNodeTypesHolder()
                             .findChildNodeDefinition(getInternalName(),
                                                      parent.getPrimaryTypeName(),
                                                      parent.getMixinTypeNames());

    if (definition == null)
      throw new ConstraintViolationException("Node definition not found for " + getPath());
  }

  /**
   * @see javax.jcr.Node#getNode
   */
  public Node getNode(String relPath) throws PathNotFoundException, RepositoryException {

    checkValid();

    JCRPath itemPath = locationFactory.parseRelPath(relPath);

    ItemImpl node = dataManager.getItem(nodeData(), itemPath.getInternalPath().getEntries(), true);
    if (node == null || !node.isNode())
      throw new PathNotFoundException("Node not found "
          + (isRoot() ? "" : getLocation().getAsString(false)) + "/" + itemPath.getAsString(false));
    return (NodeImpl) node;
  }

  /**
   * @see javax.jcr.Node#getNodes
   */
  public NodeIterator getNodes() throws RepositoryException {

    long start = System.currentTimeMillis();
    if (LOG.isDebugEnabled())
      LOG.debug("getNodes() >>>>>");

    checkValid();
    try {
      return new EntityCollection(childNodes());
    } finally {
      if (LOG.isDebugEnabled())
        LOG.debug("getNodes() <<<<< " + ((System.currentTimeMillis() - start) / 1000d) + "sec");
    }
  }

  /**
   * @return list with actual nodes, that stored in persisten storage
   */
  public List<NodeImpl> childNodes() throws RepositoryException, AccessDeniedException {

    List<NodeImpl> storedNodes = dataManager.getChildNodes(nodeData(), true);
    Collections.sort(storedNodes, new NodesOrderComparator());
    return storedNodes;
  }

  private static class NodesOrderComparator implements Comparator<NodeImpl> {
    public int compare(NodeImpl n1, NodeImpl n2) {
      return n1.getOrderNumber() - n2.getOrderNumber();
    }
  }

  /**
   * @see javax.jcr.Node#getNodes
   */
  public NodeIterator getNodes(String namePattern) throws RepositoryException {

    long start = System.currentTimeMillis();
    if (LOG.isDebugEnabled())
      LOG.debug("getNodes(String) >>>>>");

    checkValid();

    try {
      ItemFilter filter = new NamePatternFilter(namePattern);
      ArrayList<NodeImpl> list = new ArrayList<NodeImpl>();
      for (NodeImpl item : childNodes()) {
        if (filter.accept(item))
          list.add(item);
      }
      return new EntityCollection(list);
    } finally {
      if (LOG.isDebugEnabled())
        LOG.debug("getNodes(String) <<<<< " + ((System.currentTimeMillis() - start) / 1000d)
            + "sec");
    }
  }

  /**
   * @see javax.jcr.Node#getProperty
   */
  public Property getProperty(String relPath) throws PathNotFoundException, RepositoryException {

    checkValid();

    JCRPath itemPath = locationFactory.parseRelPath(relPath);

    if (LOG.isDebugEnabled())
      LOG.debug("getProperty() " + getLocation().getAsString(false) + " " + relPath);

    ItemImpl prop = dataManager.getItem(nodeData(), itemPath.getInternalPath().getEntries(), true);
    if (prop == null || prop.isNode())
      throw new PathNotFoundException("Property not found " + itemPath.getAsString(false));

    return (Property) prop;
  }

  protected PropertyImpl property(InternalQName name) throws IllegalPathException,
                                                     PathNotFoundException,
                                                     RepositoryException {
    PropertyImpl prop = (PropertyImpl) dataManager.getItem(nodeData(),
                                                           new QPathEntry(name, 1),
                                                           false);
    if (prop == null || prop.isNode())
      throw new PathNotFoundException("Property not found " + name);
    return prop;
  }

  private boolean hasProperty(InternalQName name) {
    try {
      ItemData pdata = dataManager.getItemData(nodeData(), new QPathEntry(name, 1));

      if (pdata != null && !pdata.isNode())
        return true;
    } catch (RepositoryException e) {
    }
    return false;
  }

  /**
   * @see javax.jcr.Node#getProperties
   */
  public PropertyIterator getProperties() throws RepositoryException {

    long start = System.currentTimeMillis();
    if (LOG.isDebugEnabled())
      LOG.debug("getProperties() >>>>>");

    checkValid();

    try {
      return new EntityCollection(childProperties());
    } finally {
      if (LOG.isDebugEnabled())
        LOG.debug("getProperties() <<<<< " + ((System.currentTimeMillis() - start) / 1000d) + "sec");
    }
  }

  /**
   * @return list with actual nodes, that stored in persistent storage
   */
  public List<PropertyImpl> childProperties() throws RepositoryException, AccessDeniedException {

    List<PropertyImpl> storedProperties = dataManager.getChildProperties(nodeData(), true);
    Collections.sort(storedProperties, new PropertiesOrderComparator());
    return storedProperties;
  }

  private static class PropertiesOrderComparator implements Comparator<PropertyImpl> {
    public int compare(PropertyImpl p1, PropertyImpl p2) {
      int r = 0;
      try {
        InternalQName qname1 = p1.getLocation().getName().getInternalName();
        InternalQName qname2 = p2.getLocation().getName().getInternalName();
        if (qname1.equals(Constants.JCR_PRIMARYTYPE)) {
          r = Integer.MIN_VALUE;
        } else if (qname2.equals(Constants.JCR_PRIMARYTYPE)) {
          r = Integer.MAX_VALUE;
        } else if (qname1.equals(Constants.JCR_MIXINTYPES)) {
          r = Integer.MIN_VALUE + 1;
        } else if (qname2.equals(Constants.JCR_MIXINTYPES)) {
          r = Integer.MAX_VALUE - 1;
        } else if (qname1.equals(Constants.JCR_UUID)) {
          r = Integer.MIN_VALUE + 2;
        } else if (qname2.equals(Constants.JCR_UUID)) {
          r = Integer.MAX_VALUE - 2;
        } else {
          r = qname1.getAsString().compareTo(qname2.getAsString());
        }
      } catch (Exception e) {
        LOG.error("PropertiesOrderComparator error: " + e, e);
      }
      return r;
    }
  }

  /**
   * @see javax.jcr.Node#getProperties
   */
  public PropertyIterator getProperties(String namePattern) throws RepositoryException {

    long start = System.currentTimeMillis();
    if (LOG.isDebugEnabled())
      LOG.debug("getProperties(String) >>>>>");

    checkValid();

    try {
      ItemFilter filter = new NamePatternFilter(namePattern);
      ArrayList<PropertyImpl> list = new ArrayList<PropertyImpl>();
      for (PropertyImpl item : childProperties()) {
        if (filter.accept(item))
          list.add(item);
      }

      return new EntityCollection(list);
    } finally {
      if (LOG.isDebugEnabled())
        LOG.debug("getProperties(String) <<<<< " + ((System.currentTimeMillis() - start) / 1000d)
            + "sec");
    }
  }

  /**
   * @see javax.jcr.Node#getIndex
   */
  public int getIndex() throws RepositoryException {

    checkValid();

    return getInternalPath().getIndex();
  }

  /**
   * @see javax.jcr.Node#getPrimaryItem
   */
  public Item getPrimaryItem() throws ItemNotFoundException, RepositoryException {

    checkValid();

    NodeTypeDataManager nodeTypeDataManager = session.getWorkspace().getNodeTypesHolder();

    // load nodeDatas
    List<NodeTypeData> nodeTypes = new ArrayList<NodeTypeData>();
    nodeTypes.add(nodeTypeDataManager.findNodeType(nodeData().getPrimaryTypeName()));
    InternalQName[] mixinNames = nodeData().getMixinTypeNames();
    for (int i = 0; i < mixinNames.length; i++) {
      nodeTypes.add(nodeTypeDataManager.findNodeType(mixinNames[i]));
    }

    // Searching default
    for (NodeTypeData ntData : nodeTypes) {
      if (ntData.getPrimaryItemName() != null) {
        Item primaryItem = dataManager.getItem(nodeData(),
                                               new QPathEntry(ntData.getName(), 0),
                                               true);
        if (primaryItem != null)
          return primaryItem;

      }
    }

    throw new ItemNotFoundException("Primary item not found for " + getPath());
  }

  /**
   * @see javax.jcr.Node#getUUID
   */
  public String getUUID() throws UnsupportedRepositoryOperationException, RepositoryException {

    checkValid();

    if (isNodeType(Constants.MIX_REFERENCEABLE)) {
      return this.getInternalIdentifier();
    }

    throw new UnsupportedRepositoryOperationException("Node " + getPath() + " is not referenceable");

  }

  /**
   * Returns saved only references (allowed by specs)
   * 
   * @see javax.jcr.Node#getReferences
   */
  public PropertyIterator getReferences() throws RepositoryException {

    checkValid();

    return new EntityCollection(dataManager.getReferences(getInternalIdentifier()));
  }

  /**
   * @see javax.jcr.Node#hasNode
   */
  public boolean hasNode(String relPath) throws RepositoryException {

    checkValid();

    JCRPath itemPath = locationFactory.parseRelPath(relPath);

    ItemData node = dataManager.getItemData(nodeData(), itemPath.getInternalPath().getEntries());
    return node != null && node.isNode();
  }

  /**
   * @see javax.jcr.Node#hasNodes
   */
  public boolean hasNodes() throws RepositoryException {

    checkValid();

    return dataManager.getChildNodesData(nodeData()).size() > 0;
  }

  /**
   * @see javax.jcr.Node#hasProperty
   */
  public boolean hasProperty(String relPath) throws RepositoryException {

    checkValid();

    JCRPath itemPath = locationFactory.parseRelPath(relPath);

    ItemData prop = dataManager.getItemData(nodeData(), itemPath.getInternalPath().getEntries());
    return prop != null && !prop.isNode();
  }

  /**
   * @see javax.jcr.Node#hasProperties
   */
  public boolean hasProperties() throws RepositoryException {

    checkValid();

    return dataManager.listChildPropertiesData(nodeData()).size() > 0;
  }

  // // -------------- Writting

  /**
   * @see javax.jcr.Node#addNode
   */
  public Node addNode(String relPath) throws PathNotFoundException,
                                     ConstraintViolationException,
                                     RepositoryException,
                                     VersionException,
                                     LockException {

    checkValid();
    if (JCRPath.THIS_RELPATH.equals(relPath))
      throw new RepositoryException("Can't add node to the path '" + relPath + "'");

    // Parent can be not the same as this node
    JCRPath itemPath = locationFactory.parseRelPath(relPath);

    // Check if there no final index
    if (itemPath.isIndexSetExplicitly())
      throw new RepositoryException("The relPath provided must not have an index on its final element. "
          + itemPath.getAsString(false));

    ItemImpl parentItem = dataManager.getItem(nodeData(), itemPath.makeParentPath()
                                                                  .getInternalPath()
                                                                  .getEntries(), false);

    if (parentItem == null)
      throw new PathNotFoundException("Parent not found for " + itemPath.getAsString(true));
    if (!parentItem.isNode())
      throw new ConstraintViolationException("Parent item is not a node " + parentItem.getPath());

    NodeImpl parent = (NodeImpl) parentItem;
    InternalQName name = itemPath.getName().getInternalName();

    // find node type
    NodeDefinitionData nodeDef = session.getWorkspace()
                                        .getNodeTypesHolder()
                                        .findChildNodeDefinition(name,
                                                                 nodeData().getPrimaryTypeName(),
                                                                 nodeData().getMixinTypeNames());

    if (nodeDef == null)
      throw new ConstraintViolationException("Can not define node type for " + name.getAsString());
    InternalQName primaryTypeName = nodeDef.getName();
    if (primaryTypeName.equals(Constants.JCR_ANY_NAME))
      primaryTypeName = nodeDef.getDefaultPrimaryType();
    // try to make new node
    return doAddNode(parent, name, primaryTypeName);

  }

  //
  // @Deprecated
  // private JCRName findNodeType(String name) throws RepositoryException,
  // ConstraintViolationException {
  //
  // NodeType[] nodeTypes = getAllNodeTypes();
  // String residualNodeTypeName = null;
  // for (int j = 0; j < nodeTypes.length; j++) {
  // NodeDefinition[] nodeDefs = nodeTypes[j].getChildNodeDefinitions();
  // for (int i = 0; i < nodeDefs.length; i++) {
  // NodeDefinition nodeDef = nodeDefs[i];
  // if (nodeDef.getName().equals(name)) {
  // return
  // sysLocFactory.parseJCRName(nodeDef.getDefaultPrimaryType().getName());
  // } else if (nodeDef.getName().equals(ExtendedItemDefinition.RESIDUAL_SET)) {
  // residualNodeTypeName = nodeDef.getDefaultPrimaryType().getName();
  // }
  // }
  // }
  // if (residualNodeTypeName == null)
  // throw new ConstraintViolationException("Can not define node type for " +
  // name);
  // return sysLocFactory.parseJCRName(residualNodeTypeName);
  // }

  /**
   * @see javax.jcr.Node#addNode
   */
  public Node addNode(String relPath, String nodeTypeName) throws ItemExistsException,
                                                          PathNotFoundException,
                                                          NoSuchNodeTypeException,
                                                          ConstraintViolationException,
                                                          RepositoryException,
                                                          VersionException,
                                                          LockException {

    checkValid();

    if (JCRPath.THIS_RELPATH.equals(relPath))
      throw new RepositoryException("Can't add node to the path '" + relPath + "'");

    // Parent can be not the same as this node
    JCRPath itemPath = locationFactory.parseRelPath(relPath);

    // Check if there no final index
    if (itemPath.isIndexSetExplicitly())
      throw new RepositoryException("The relPath provided must not have an index on its final element. "
          + itemPath.getAsString(false));

    ItemImpl parentItem = dataManager.getItem(nodeData(), itemPath.makeParentPath()
                                                                  .getInternalPath()
                                                                  .getEntries(), false);

    if (parentItem == null)
      throw new PathNotFoundException("Parent not found for " + itemPath.getAsString(true));
    if (!parentItem.isNode())
      throw new ConstraintViolationException("Parent item is not a node " + parentItem.getPath());
    NodeImpl parent = (NodeImpl) parentItem;

    InternalQName name = itemPath.getName().getInternalName();
    InternalQName ptName = locationFactory.parseJCRName(nodeTypeName).getInternalName();

    // try to make new node
    return doAddNode(parent, name, ptName);
  }

  public void validateChildNode(InternalQName name, InternalQName primaryTypeName) throws ItemExistsException,
                                                                                  RepositoryException,
                                                                                  ConstraintViolationException,
                                                                                  VersionException,
                                                                                  LockException {

    // Check if nodeType exists and not mixin
    String ptStr = sysLocFactory.createJCRName(primaryTypeName).getAsString();
    NodeTypeDataManager nodeTypeDataManager = session.getWorkspace().getNodeTypesHolder();
    if (nodeTypeDataManager.findNodeType(primaryTypeName).isMixin())
      throw new ConstraintViolationException("Add Node failed: Node Type <" + ptStr
          + "> is MIXIN type!");

    // Check if new node's node type is allowed by its parent definition

    if (!nodeTypeDataManager.isChildNodePrimaryTypeAllowed(primaryTypeName,
                                                           nodeData().getPrimaryTypeName(),
                                                           nodeData().getMixinTypeNames())) {
      throw new ConstraintViolationException("Can't add node " + name.getAsString() + " to "
          + getPath() + " node type " + primaryTypeName.getAsString()
          + " is not allowed as child's node type for parent node type ");

    }
    // Check if node is not protected
    if (session.getWorkspace()
               .getNodeTypesHolder()
               .findChildNodeDefinition(name,
                                        nodeData().getPrimaryTypeName(),
                                        nodeData().getMixinTypeNames())
               .isProtected())
      throw new ConstraintViolationException("Can't add protected node " + name.getAsString()
          + " to " + getPath());

    // Check if versionable ancestor is not checked-in
    if (!checkedOut())
      throw new VersionException("Node " + getPath() + " or its nearest ancestor is checked-in");

    if (!checkLocking())
      // Check locking
      throw new LockException("Node " + getPath() + " is locked ");

  }

  private NodeImpl doAddNode(NodeImpl parentNode, InternalQName name, InternalQName primaryTypeName) throws ItemExistsException,
                                                                                                    RepositoryException,
                                                                                                    ConstraintViolationException,
                                                                                                    VersionException,
                                                                                                    LockException {

    validateChildNode(name, primaryTypeName);
    // /////// VALIDATION /////////
    //
    // if (nodeType(primaryTypeName).isMixin())
    // throw new ConstraintViolationException("Can not add node to " +
    // parentNode.getPath()
    // + " node type " + primaryTypeName.getAsString() + " is mixin ");
    //
    // String ptStr =
    // sysLocFactory.createJCRName(primaryTypeName).getAsString();
    // // Check if new node's node type is allowed by its parent definition
    // NodeType[] types = getAllNodeTypes();
    // for (int i = 0; i < types.length; i++) {
    // ExtendedNodeType t = (ExtendedNodeType) types[i];
    // if (t.isChildNodePrimaryTypeAllowed(ptStr)) {
    // break;
    // } else if (i == types.length - 1) {
    // throw new ConstraintViolationException("Can't add node " +
    // name.getAsString() + " to "
    // + getPath() + " node type " + primaryTypeName.getAsString()
    // + " is not allowed as child's node type for parent node type ");
    // }
    // }
    // NodeDefinitionData def = session.getWorkspace()
    // .getNodeTypesHolder()
    // .findChildNodeDefinition(name,
    // parentNode.nodeData()
    // .getPrimaryTypeName(),
    // parentNode.nodeData()
    // .getMixinTypeNames());
    // if (def == null)
    // throw new ConstraintViolationException("Can't add node " +
    // name.getAsString() + " to "
    // + getPath() + ". Node type " + primaryTypeName.getAsString()
    // + " is not allowed as child's node  " + ptStr +
    // " for parent node type ");
    //
    // // Check if node is not protected
    // if (def.isProtected())
    // throw new ConstraintViolationException("Can't add protected node " +
    // name.getAsString()
    // + " to " + parentNode.getPath());
    //
    // // Check if versionable ancestor is not checked-in
    // if (!checkedOut())
    // throw new VersionException("Node " + getPath() +
    // " or its nearest ancestor is checked-in");
    //
    // // Check locking
    // if (!checkLocking())
    // throw new LockException("Node " + getPath() + " is locked ");

    // //////// END VALIDATION ////////

    // Initialize data
    InternalQName[] mixinTypeNames = new InternalQName[0];
    String identifier = IdGenerator.generate();

    List<NodeData> siblings = dataManager.getChildNodesData(parentNode.nodeData());
    int orderNum = parentNode.getNextChildOrderNum(siblings);
    int index = parentNode.getNextChildIndex(name, siblings, parentNode.nodeData());

    QPath path = QPath.makeChildPath(parentNode.getInternalPath(), name, index);

    AccessControlList acl = parentNode.getACL();

    // create new nodedata, [PN] fix of use index as persisted version
    NodeData nodeData = new TransientNodeData(path,
                                              identifier,
                                              -1,
                                              primaryTypeName,
                                              mixinTypeNames,
                                              orderNum,
                                              parentNode.getInternalIdentifier(),
                                              acl);

    // Create new Node
    ItemState state = ItemState.createAddedState(nodeData, false);
    NodeImpl node = (NodeImpl) dataManager.update(state, true);

    addAutoCreatedItems(node.nodeData(), primaryTypeName);

    if (LOG.isDebugEnabled())
      LOG.debug("new node : " + node.getPath() + " name: " + " primaryType: "
          + node.getPrimaryNodeType().getName() + " index: " + node.getIndex() + " parent: "
          + parentNode);

    // launch event
    session.getActionHandler().postAddNode(node);

    return node;

  }

  public int getNextChildOrderNum(List<NodeData> siblings) {
    int max = -1;
    for (NodeData sibling : siblings) {
      int cur = sibling.getOrderNumber();
      if (cur > max)
        max = cur;
    }
    return ++max;
  }

  private int getNextChildIndex(InternalQName nameToAdd,
                                List<NodeData> siblings,
                                NodeData parentNode) throws RepositoryException,
                                                    ItemExistsException {

    int ind = 0;
    for (NodeData sibling : siblings) {
      if (sibling.getQPath().getName().equals(nameToAdd)) {
        NodeDefinitionData def = session.getWorkspace()
                                        .getNodeTypesHolder()
                                        .findChildNodeDefinition(nameToAdd,
                                                                 parentNode.getPrimaryTypeName(),
                                                                 parentNode.getMixinTypeNames());
        if (LOG.isDebugEnabled())
          LOG.debug("Calculate index for " + nameToAdd + " " + sibling.getQPath().getAsString());

        if (def.isAllowsSameNameSiblings())
          ind++;
        else
          throw new ItemExistsException("The node " + nameToAdd + " already exists in " + getPath()
              + " and same name sibling is not allowed ");
      }
    }
    return ind + 1;

  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, Value[] values, int type) throws ValueFormatException,
                                                                    VersionException,
                                                                    LockException,
                                                                    ConstraintViolationException,
                                                                    RepositoryException {

    checkValid();

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            values,
                            true,
                            type);
  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, Value[] values) throws ValueFormatException,
                                                          VersionException,
                                                          LockException,
                                                          ConstraintViolationException,
                                                          RepositoryException {

    checkValid();

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            values,
                            true,
                            PropertyType.UNDEFINED);
  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, String[] values) throws ValueFormatException,
                                                           VersionException,
                                                           LockException,
                                                           ConstraintViolationException,
                                                           RepositoryException {

    checkValid();

    Value[] val = null;
    if (values != null) {
      val = new Value[values.length];
      for (int i = 0; i < values.length; i++) {
        val[i] = valueFactory.createValue(values[i]);
      }
    }
    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            val,
                            true,
                            PropertyType.UNDEFINED);
  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, String[] values, int type) throws ValueFormatException,
                                                                     VersionException,
                                                                     LockException,
                                                                     ConstraintViolationException,
                                                                     RepositoryException {

    checkValid();

    Value[] val = null;
    if (values != null) {
      val = new Value[values.length];
      for (int i = 0; i < values.length; i++) {
        val[i] = valueFactory.createValue(values[i], type);
      }
    }

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            val,
                            true,
                            type);
  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, Value value) throws ValueFormatException,
                                                       VersionException,
                                                       LockException,
                                                       ConstraintViolationException,
                                                       RepositoryException {

    checkValid();

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            value,
                            false,
                            PropertyType.UNDEFINED);
  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, Value value, int type) throws ValueFormatException,
                                                                 VersionException,
                                                                 LockException,
                                                                 ConstraintViolationException,
                                                                 RepositoryException {

    checkValid();

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            value,
                            false,
                            type);

  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, String value, int type) throws ValueFormatException,
                                                                  VersionException,
                                                                  LockException,
                                                                  ConstraintViolationException,
                                                                  RepositoryException {

    checkValid();

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            valueFactory.createValue(value, type),
                            false,
                            type);
  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, String value) throws ValueFormatException,
                                                        VersionException,
                                                        LockException,
                                                        ConstraintViolationException,
                                                        RepositoryException {

    checkValid();

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            valueFactory.createValue(value),
                            false,
                            PropertyType.UNDEFINED);

  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, InputStream value) throws ValueFormatException,
                                                             VersionException,
                                                             LockException,
                                                             ConstraintViolationException,
                                                             RepositoryException {

    checkValid();

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            valueFactory.createValue(value),
                            false,
                            PropertyType.UNDEFINED);
  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, boolean value) throws ValueFormatException,
                                                         VersionException,
                                                         LockException,
                                                         ConstraintViolationException,
                                                         RepositoryException {

    checkValid();

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            valueFactory.createValue(value),
                            false,
                            PropertyType.UNDEFINED);

  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, Calendar value) throws ValueFormatException,
                                                          VersionException,
                                                          LockException,
                                                          ConstraintViolationException,
                                                          RepositoryException {

    checkValid();

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            valueFactory.createValue(value),
                            false,
                            PropertyType.UNDEFINED);

  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, double value) throws ValueFormatException,
                                                        VersionException,
                                                        LockException,
                                                        ConstraintViolationException,
                                                        RepositoryException {

    checkValid();

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            valueFactory.createValue(value),
                            false,
                            PropertyType.UNDEFINED);
  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, long value) throws ValueFormatException,
                                                      VersionException,
                                                      LockException,
                                                      ConstraintViolationException,
                                                      RepositoryException {

    checkValid();

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            valueFactory.createValue(value),
                            false,
                            PropertyType.UNDEFINED);

  }

  /**
   * @see javax.jcr.Node#setProperty
   */
  public Property setProperty(String name, Node value) throws ValueFormatException,
                                                      VersionException,
                                                      LockException,
                                                      ConstraintViolationException,
                                                      RepositoryException {

    checkValid();

    return doUpdateProperty(this,
                            locationFactory.parseJCRName(name).getInternalName(),
                            valueFactory.createValue(value),
                            false,
                            PropertyType.UNDEFINED);

  }

  /**
   * @see javax.jcr.Node#getPrimaryNodeType
   */
  public NodeType getPrimaryNodeType() throws RepositoryException {

    checkValid();
    ExtendedNodeTypeManager nodeTypeManager = (ExtendedNodeTypeManager) session.getWorkspace()
                                                                               .getNodeTypeManager();
    return nodeTypeManager.findNodeType(nodeData().getPrimaryTypeName());
  }

  /**
   * @see javax.jcr.Node#getMixinNodeTypes
   */
  public NodeType[] getMixinNodeTypes() throws RepositoryException {

    checkValid();

    // TODO should not be null
    if (nodeData().getMixinTypeNames() == null)
      throw new RepositoryException("Data Container implementation error getMixinTypeNames == null");
    ExtendedNodeTypeManager nodeTypeManager = (ExtendedNodeTypeManager) session.getWorkspace()
                                                                               .getNodeTypeManager();
    NodeType[] mixinNodeTypes = new NodeType[nodeData().getMixinTypeNames().length];
    for (int i = 0; i < mixinNodeTypes.length; i++) {
      mixinNodeTypes[i] = nodeTypeManager.findNodeType(nodeData().getMixinTypeNames()[i]);
    }

    return mixinNodeTypes;
  }

  /**
   * @see javax.jcr.Node#isNodeType
   */
  public boolean isNodeType(String nodeTypeName) throws RepositoryException {
    return isNodeType(locationFactory.parseJCRName(nodeTypeName).getInternalName());
  }

  /**
   * TODO have it private
   * 
   * @param qName
   * @return
   * @throws RepositoryException
   */
  public boolean isNodeType(InternalQName qName) throws RepositoryException {

    return session.getWorkspace().getNodeTypesHolder().isNodeType(qName,
                                                                  nodeData().getPrimaryTypeName(),
                                                                  nodeData().getMixinTypeNames());
  }

  /**
   * {@inheritDoc}
   */
  public NodeDefinition getDefinition() throws RepositoryException {

    checkValid();

    if (nodeDefinition == null) {

      NodeTypeDataManager nodeTypesHolder = session.getWorkspace().getNodeTypesHolder();
      ExtendedNodeTypeManager nodeTypeManager = (ExtendedNodeTypeManager) session.getWorkspace()
                                                                                 .getNodeTypeManager();

      if (this.isRoot()) { // root - no parent
        if (nodeDefinition == null) {
          NodeType required = nodeTypeManager.getNodeType(locationFactory.createJCRName(Constants.NT_BASE)
                                                                         .getAsString());
          this.nodeDefinition = new NodeDefinitionImpl(null,
                                                       null,
                                                       new NodeType[] { required },
                                                       null,
                                                       true,
                                                       true,
                                                       OnParentVersionAction.ABORT,
                                                       false,
                                                       false);
        }
      } else {

        NodeData parent = (NodeData) dataManager.getItemData(getParentIdentifier());

        this.definition = nodeTypesHolder.findChildNodeDefinition(getInternalName(),
                                                                  parent.getPrimaryTypeName(),
                                                                  parent.getMixinTypeNames());

        if (definition == null)
          throw new ConstraintViolationException("Node definition not found for " + getPath());
        // TODO same functionality in NodeTypeImpl
        InternalQName[] rnames = definition.getRequiredPrimaryTypes();
        NodeType[] rnts = new NodeType[rnames.length];
        for (int j = 0; j < rnames.length; j++) {
          rnts[j] = nodeTypeManager.findNodeType(rnames[j]);
        }

        String name = locationFactory.createJCRName(definition.getName() != null ? definition.getName()
                                                                                : Constants.JCR_ANY_NAME)
                                     .getAsString();
        NodeType defType = definition.getDefaultPrimaryType() != null ? nodeTypeManager.findNodeType(definition.getDefaultPrimaryType())
                                                                     : null;
        NodeType declaringNodeType = nodeTypeManager.findNodeType(definition.getDeclaringNodeType());
        nodeDefinition = new NodeDefinitionImpl(name,
                                                declaringNodeType,
                                                rnts,
                                                defType,
                                                definition.isAutoCreated(),
                                                definition.isMandatory(),
                                                definition.getOnParentVersion(),
                                                definition.isProtected(),
                                                definition.isAllowsSameNameSiblings());
      }
    }

    return nodeDefinition;

  }

  /**
   * @see javax.jcr.Node#getCorrespondingNodePath
   */
  public String getCorrespondingNodePath(String workspaceName) throws ItemNotFoundException,
                                                              NoSuchWorkspaceException,
                                                              AccessDeniedException,
                                                              RepositoryException {

    checkValid();

    SessionImpl corrSession = ((RepositoryImpl) session.getRepository()).internalLogin(session.getUserState(),
                                                                                       workspaceName);
    // .login(session.getCredentials(), workspaceName);

    return corrSession.getLocationFactory()
                      .createJCRPath(getCorrespondingNodeData(corrSession).getQPath())
                      .getAsString(false);
  }

  public NodeData getCorrespondingNodeData(SessionImpl corrSession) throws ItemNotFoundException,
                                                                   NoSuchWorkspaceException,
                                                                   AccessDeniedException,
                                                                   RepositoryException {

    final QPath myPath = nodeData().getQPath();
    final SessionDataManager corrDataManager = corrSession.getTransientNodesManager();

    if (this.isNodeType(Constants.MIX_REFERENCEABLE)) {
      NodeData corrNode = (NodeData) corrDataManager.getItemData(getUUID());
      if (corrNode != null)
        return corrNode;
    } else {
      NodeData ancestor = (NodeData) dataManager.getItemData(Constants.ROOT_UUID);
      for (int i = 1; i < myPath.getDepth(); i++) {
        ancestor = (NodeData) dataManager.getItemData(ancestor, myPath.getEntries()[i]);
        if (corrSession.getWorkspace()
                       .getNodeTypesHolder()
                       .isNodeType(Constants.MIX_REFERENCEABLE,
                                   ancestor.getPrimaryTypeName(),
                                   ancestor.getMixinTypeNames())) {
          NodeData corrAncestor = (NodeData) corrDataManager.getItemData(ancestor.getIdentifier());
          if (corrAncestor == null)
            throw new ItemNotFoundException("No corresponding path for ancestor "
                + ancestor.getQPath().getAsString() + " in " + corrSession.getWorkspace().getName());

          NodeData corrNode = (NodeData) corrDataManager.getItemData(corrAncestor,
                                                                     myPath.getRelPath(myPath.getDepth()
                                                                         - i));
          if (corrNode != null)
            return corrNode;
        }
      }
    }
    NodeData corrNode = (NodeData) corrDataManager.getItemData(myPath);
    if (corrNode != null)
      return corrNode;

    throw new ItemNotFoundException("No corresponding path for " + getPath() + " in "
        + corrSession.getWorkspace().getName());
  }

  public Node getCorrespondingNode(SessionImpl correspSession) throws ItemNotFoundException,
                                                              NoSuchWorkspaceException,
                                                              AccessDeniedException,
                                                              RepositoryException {

    if (this.isNodeType(Constants.MIX_REFERENCEABLE)) {
      try {
        return correspSession.getNodeByUUID(getUUID());
      } catch (ItemNotFoundException e) {
      }
    } else {
      for (int i = getDepth(); i >= 0; i--) {
        NodeImpl ancestor = (NodeImpl) getAncestor(i);
        if (ancestor.isNodeType(Constants.MIX_REFERENCEABLE)) {
          NodeImpl correspAncestor = (NodeImpl) correspSession.getNodeByUUID(ancestor.getUUID());
          JCRPath.PathElement[] relJCRPath = getLocation().getRelPath(getDepth() - i);
          try {
            return correspAncestor.getNode(getRelPath(relJCRPath));
          } catch (ItemNotFoundException e) {
          }
        }
      }
    }
    try {
      return (NodeImpl) correspSession.getItem(getPath());
    } catch (PathNotFoundException e) {
      throw new ItemNotFoundException("No corresponding path for " + getPath() + " in "
          + correspSession.getWorkspace().getName());
    }
  }

  private String getRelPath(JCRPath.PathElement[] relPath) {
    String path = "";
    for (int i = 0; i < relPath.length; i++) {
      path += relPath[i].getAsString(false);
      if (i < relPath.length - 1)
        path += "/";
    }
    return path;
  }

  /**
   * @see javax.jcr.Node#update
   */
  public void update(String srcWorkspaceName) throws NoSuchWorkspaceException,
                                             AccessDeniedException,
                                             InvalidItemStateException,
                                             LockException,
                                             RepositoryException {

    checkValid();

    // Check pending changes
    if (session.hasPendingChanges())
      throw new InvalidItemStateException("Session has pending changes ");

    // Check locking
    if (!checkLocking())
      throw new LockException("Node " + getPath() + " is locked ");

    SessionChangesLog changes = new SessionChangesLog(session.getId());

    String srcPath;
    try {
      srcPath = getCorrespondingNodePath(srcWorkspaceName);

      ItemDataRemoveVisitor remover = new ItemDataRemoveVisitor(session.getTransientNodesManager(),
                                                                null,
                                                                session.getWorkspace()
                                                                       .getNodeTypesHolder(),
                                                                session.getAccessManager(),
                                                                session.getUserState());
      nodeData().accept(remover);

      changes.addAll(remover.getRemovedStates());
    } catch (ItemNotFoundException e) {
      LOG.debug("No corresponding node in workspace: " + srcWorkspaceName);
      return;
    }

    TransactionableDataManager pmanager = session.getTransientNodesManager().getTransactManager();

    session.getWorkspace().clone(srcWorkspaceName, srcPath, this.getPath(), true, changes);
    pmanager.save(changes);

    NodeData thisParent = (NodeData) session.getTransientNodesManager()
                                            .getItemData(getParentIdentifier());
    QPathEntry[] qpath = getInternalPath().getEntries();
    NodeData thisNew = (NodeData) pmanager.getItemData(thisParent, qpath[qpath.length - 1]);
    // reload node impl with old uuid to a new one data
    session.getTransientNodesManager().getItemsPool().reload(getInternalIdentifier(), thisNew);
  }

  /**
   * @see javax.jcr.Node#addMixin
   */
  public void addMixin(String mixinName) throws NoSuchNodeTypeException,
                                        ConstraintViolationException,
                                        VersionException,
                                        LockException,
                                        RepositoryException {

    checkValid();

    if (LOG.isDebugEnabled())
      LOG.debug("Node.addMixin " + mixinName + " " + getPath());

    InternalQName name = locationFactory.parseJCRName(mixinName).getInternalName();

    // Does the node already has the mixin
    for (InternalQName mixin : nodeData().getMixinTypeNames()) {
      if (name.equals(mixin)) {
        // we already have this mixin type
        LOG.warn("Node already of mixin type " + mixinName + " " + getPath());
        return;
      }
    }

    NodeTypeData type = session.getWorkspace().getNodeTypesHolder().findNodeType(name);

    // Mixin or not
    if (type == null || !type.isMixin())
      throw new NoSuchNodeTypeException("Nodetype " + mixinName + " not found or not mixin type.");

    // Validate
    if (session.getWorkspace().getNodeTypesHolder().isNodeType(type.getName(),
                                                               nodeData().getPrimaryTypeName(),
                                                               nodeData().getMixinTypeNames()))
      throw new ConstraintViolationException("Can not add mixin type " + mixinName + " to "
          + getPath());

    if (definition.isProtected())
      throw new ConstraintViolationException("Can not add mixin type. Node is protected "
          + getPath());

    // Check if versionable ancestor is not checked-in
    if (!checkedOut())
      throw new VersionException("Node " + getPath() + " or its nearest ancestor is checked-in");

    // Check locking
    if (!checkLocking())
      throw new LockException("Node " + getPath() + " is locked ");

    doAddMixin(type);
  }

  public void doAddMixin(NodeTypeData type) throws NoSuchNodeTypeException,
                                           ConstraintViolationException,
                                           VersionException,
                                           LockException,
                                           RepositoryException {

    // Add both to mixinNodeTypes and to jcr:mixinTypes property

    // Prepare mixin values
    InternalQName[] mixinTypes = nodeData().getMixinTypeNames();
    List<InternalQName> newMixin = new ArrayList<InternalQName>(mixinTypes.length + 1);
    List<ValueData> values = new ArrayList<ValueData>(mixinTypes.length + 1);

    for (int i = 0; i < mixinTypes.length; i++) {
      InternalQName cn = mixinTypes[i];
      newMixin.add(cn);
      values.add(new TransientValueData(cn));
    }
    newMixin.add(type.getName());
    values.add(new TransientValueData(type.getName()));

    TransientPropertyData prop = (TransientPropertyData) dataManager.getItemData(((NodeData) getData()),
                                                                                 new QPathEntry(Constants.JCR_MIXINTYPES,
                                                                                                0));
    ItemState state;

    if (prop != null) {// there was mixin prop
      prop = new TransientPropertyData(prop.getQPath(),
                                       prop.getIdentifier(),
                                       prop.getPersistedVersion(),
                                       prop.getType(),
                                       prop.getParentIdentifier(),
                                       prop.isMultiValued());

      prop.setValues(values);

      state = ItemState.createUpdatedState(prop);
    } else {
      prop = TransientPropertyData.createPropertyData(this.nodeData(),
                                                      Constants.JCR_MIXINTYPES,
                                                      PropertyType.NAME,
                                                      true,
                                                      values);
      state = ItemState.createAddedState(prop);
    }

    // Should register jcr:mixinTypes and autocreated items if node is not added
    updateMixin(newMixin);
    dataManager.update(state, false);

    addAutoCreatedItems(type.getName());

    // launch event
    session.getActionHandler().postAddMixin(this, type.getName());

    if (LOG.isDebugEnabled())
      LOG.debug("Node.addMixin Property " + prop.getQPath().getAsString() + " values "
          + mixinTypes.length);
  }

  /**
   * @see javax.jcr.Node#removeMixin
   */
  public void removeMixin(String mixinName) throws NoSuchNodeTypeException,
                                           ConstraintViolationException,
                                           RepositoryException {

    checkValid();

    InternalQName[] mixinTypes = nodeData().getMixinTypeNames();
    InternalQName name = locationFactory.parseJCRName(mixinName).getInternalName();

    // find mixin
    InternalQName removedName = null;
    // Prepare mixin values
    List<InternalQName> newMixin = new ArrayList<InternalQName>();
    List<ValueData> values = new ArrayList<ValueData>();

    for (InternalQName mt : mixinTypes) {
      if (mt.equals(name)) {
        removedName = mt;
      } else {
        newMixin.add(mt);
        values.add(new TransientValueData(mt));
      }
    }

    // no mixin found
    if (removedName == null)
      throw new NoSuchNodeTypeException("No mixin type found " + mixinName + " for node "
          + getPath());

    // A ConstraintViolationException will be thrown either
    // immediately or on save if the removal of a mixin is not
    // allowed. Implementations are free to enforce any policy
    // they like with regard to mixin removal and may differ on
    // when this validation is done.

    // Check if versionable ancestor is not checked-in
    if (!checkedOut())
      throw new VersionException("Node " + getPath() + " or its nearest ancestor is checked-in");

    // Check locking
    if (!checkLocking())
      throw new LockException("Node " + getPath() + " is locked ");

    session.getActionHandler().preRemoveMixin(this, name);

    TransientPropertyData prop = (TransientPropertyData) dataManager.getItemData(nodeData(),
                                                                                 new QPathEntry(Constants.JCR_MIXINTYPES,
                                                                                                0));

    prop.setValues(values);

    NodeTypeDataManager ntmanager = session.getWorkspace().getNodeTypesHolder();

    // remove mix:versionable stuff
    if (ntmanager.isNodeType(Constants.MIX_VERSIONABLE, removedName))
      removeVersionable();

    // remove mix:lockable stuff
    if (ntmanager.isNodeType(Constants.MIX_LOCKABLE, removedName))
      removeLockable();

    // Set mixin property and locally
    updateMixin(newMixin);

    // Remove mixin nt definition node/properties from this node
    QPath ancestorToSave = nodeData().getQPath();

    for (PropertyDefinitionData pd : ntmanager.getAllPropertyDefinitions(removedName)) {
      ItemData p = dataManager.getItemData(nodeData(), new QPathEntry(pd.getName(), 1));
      if (p != null && !p.isNode())
        // remove it
        dataManager.delete(p, ancestorToSave);
    }

    for (NodeDefinitionData nd : ntmanager.getAllChildNodeDefinitions(removedName)) {
      ItemData n = dataManager.getItemData(nodeData(), new QPathEntry(nd.getName(), 1));
      if (n != null && n.isNode()) {
        // remove node with subtree
        ItemDataRemoveVisitor remover = new ItemDataRemoveVisitor(dataManager, ancestorToSave);
        n.accept(remover);
        for (ItemState deleted : remover.getRemovedStates()) {
          dataManager.delete(deleted.getData(), ancestorToSave);
        }
      }
    }

    if (newMixin.size() > 0) {
      dataManager.update(new ItemState(prop, ItemState.UPDATED, true, ancestorToSave), false);
    } else {
      dataManager.delete(prop, ancestorToSave);
    }
  }

  private void updateMixin(List<InternalQName> newMixin) throws RepositoryException {
    InternalQName[] mixins = new InternalQName[newMixin.size()];
    newMixin.toArray(mixins);
    ((TransientNodeData) data).setMixinTypeNames(mixins);
    dataManager.update(new ItemState(data, ItemState.MIXIN_CHANGED, false, null), false);
  }

  /**
   * {@inheritDoc}
   */
  public boolean canAddMixin(String mixinName) throws RepositoryException {

    checkValid();

    NodeTypeData type = session.getWorkspace()
                               .getNodeTypesHolder()
                               .findNodeType(locationFactory.parseJCRName(mixinName)
                                                            .getInternalName());

    if (type == null)
      throw new NoSuchNodeTypeException("Nodetype not found (mixin) " + mixinName);

    if (session.getWorkspace().getNodeTypesHolder().isNodeType(type.getName(),
                                                               nodeData().getPrimaryTypeName(),
                                                               nodeData().getMixinTypeNames()))
      return false;

    if (definition.isProtected())
      return false;

    if (!checkedOut())
      return false;

    if (!checkLocking())
      return false;

    return true;
  }

  protected void removeLockable() throws RepositoryException {
    if (session.getLockManager().holdsLock(nodeData())) {
      // locked, should be unlocked

      if (!session.getLockManager().isLockHolder(this))
        throw new LockException("There are no permission to unlock the node " + getPath());

      // remove mix:lockable properties (as the node is locked)
      doUnlock();
    }
  }

  private static class NodeDataOrderComparator implements Comparator<NodeData> {
    public int compare(NodeData n1, NodeData n2) {
      return n1.getOrderNumber() - n2.getOrderNumber();
    }
  }

  protected void doOrderBefore(QPath srcPath, QPath destPath) throws RepositoryException {
    if (!getPrimaryNodeType().hasOrderableChildNodes())
      throw new UnsupportedRepositoryOperationException("child node ordering not supported on node "
          + getPath());

    if (srcPath.equals(destPath))
      return;

    // check existence
    if (dataManager.getItemData(srcPath) == null) {
      throw new ItemNotFoundException(getPath() + " has no child node with name "
          + srcPath.getName().getAsString());
    }

    if (destPath != null && dataManager.getItemData(destPath) == null) {

      throw new ItemNotFoundException(getPath() + " has no child node with name "
          + destPath.getName().getAsString());
    }

    if (!checkedOut())
      throw new VersionException(" cannot change child node ordering of a checked-in node ");

    if (destPath != null && srcPath.getDepth() != destPath.getDepth())
      throw new ItemNotFoundException("Source and destenation is not relative paths of depth one, "
          + "i.e. is not a childs of same parent node");

    List<NodeData> siblings = dataManager.getChildNodesData(nodeData());
    Collections.sort(siblings, new NodeDataOrderComparator());
    if (siblings.size() < 2)
      throw new UnsupportedRepositoryOperationException("Nothing to order Count of child nodes "
          + siblings.size());

    // calculating source and destination position
    int srcInd = -1, destInd = -1;
    for (int i = 0; i < siblings.size(); i++) {
      NodeData nodeData = siblings.get(i);
      if (srcInd == -1) {
        if (nodeData.getQPath().getName().equals(srcPath.getName())
            && (nodeData.getQPath().getIndex() == srcPath.getIndex() || srcPath.getIndex() == 0
                && nodeData.getQPath().getIndex() == 1)) {
          srcInd = i;
        }
      }
      if (destInd == -1 && destPath != null) {
        if (nodeData.getQPath().getName().equals(destPath.getName())
            && (nodeData.getQPath().getIndex() == destPath.getIndex() || destPath.getIndex() == 0
                && nodeData.getQPath().getIndex() == 1)) {
          destInd = i;
          if (srcInd != -1) {
            break;
          }
        }
      } else {
        if (srcInd != -1) {
          break;
        }
      }
    }

    // check if resulting order would be different to current order
    if (destInd == -1) {
      if (srcInd == siblings.size() - 1) {
        // no change, we're done
        return;
      }
    } else {
      if ((destInd - srcInd) == 1) {
        // no change, we're done
        return;
      }
    }

    // reorder list
    if (destInd == -1) {
      siblings.add(siblings.remove(srcInd));
    } else {
      if (srcInd < destInd) {
        siblings.add(destInd, siblings.get(srcInd));
        siblings.remove(srcInd);
      } else {
        siblings.add(destInd, siblings.remove(srcInd));
      }
    }

    int sameNameIndex = 0;
    List<ItemState> changes = new ArrayList<ItemState>();
    ItemState deleteState = null;
    for (int j = 0; j < siblings.size(); j++) {
      NodeData sdata = siblings.get(j);

      // calculating same name index
      if (sdata.getQPath().getName().equals(srcPath.getName()))
        ++sameNameIndex;

      // skeep unchanged
      if (sdata.getOrderNumber() == j)
        continue;

      NodeData newData = sdata;
      // change same name index
      if (sdata.getQPath().getName().equals(srcPath.getName())
          && sdata.getQPath().getIndex() != sameNameIndex) {
        newData = ((TransientNodeData) sdata).cloneAsSibling(sameNameIndex);
      }
      // crate update
      ((TransientNodeData) newData).setOrderNumber(j);

      /*
       * 8.3.7.8 Re-ordering a set of Child Nodes. When an orderBefore(A, B) is
       * performed, an implementation must generate a NODE_REMOVED for node A
       * and a NODE_ADDED for node A. Note that the paths associated with these
       * two events will either differ by the last index number (if the movement
       * of A causes it to be re-ordered with respect to its same-name siblings)
       * or be identical (if A does not have same-name siblings or if the
       * movement of A does not change its order relative to its same-name
       * siblings). Additionally, an implementation should generate appropriate
       * events reflecting the "shifting" over of the node B and any nodes that
       * come after it in the child node ordering. Each such shifted node would
       * also produce a NODE_REMOVED and NODE_ADDED event pair with paths
       * differing at most by a final index.
       */
      if (sdata.getQPath().equals(srcPath)) {
        deleteState = new ItemState(sdata, ItemState.DELETED, true, null, false, false);
        changes.add(new ItemState(newData, ItemState.UPDATED, true, null, false, true));
      } else {
        ItemState state = ItemState.createUpdatedState(newData);
        state.eraseEventFire();
        changes.add(state);
      }
    }
    // delete state first
    dataManager.getChangesLog().add(deleteState);
    dataManager.getChangesLog().addAll(changes);
  }

  /**
   * @see javax.jcr.Node#orderBefore
   */
  public void orderBefore(String srcName, String destName) throws UnsupportedRepositoryOperationException,
                                                          ConstraintViolationException,
                                                          ItemNotFoundException,
                                                          RepositoryException {

    checkValid();

    if (!getPrimaryNodeType().hasOrderableChildNodes())
      throw new UnsupportedRepositoryOperationException("Node does not support child ordering "
          + getPrimaryNodeType().getName());

    JCRPath sourcePath = locationFactory.createJCRPath(getLocation(), srcName);
    JCRPath destenationPath = destName != null ? locationFactory.createJCRPath(getLocation(),
                                                                               destName) : null;
    QPath srcPath = sourcePath.getInternalPath();
    QPath destPath = destenationPath != null ? destenationPath.getInternalPath() : null;

    doOrderBefore(srcPath, destPath);
  }

  private int getOrderNumber() {
    return nodeData().getOrderNumber();
  }

  // //////////////////////// OPTIONAL

  // VERSIONING

  public Version checkin() throws VersionException,
                          UnsupportedRepositoryOperationException,
                          InvalidItemStateException,
                          RepositoryException {

    checkValid();

    if (!this.isNodeType(Constants.MIX_VERSIONABLE))
      throw new UnsupportedRepositoryOperationException("Node.checkin() is not supported for not mix:versionable node ");

    if (!this.checkedOut()) {
      return (Version) dataManager.getItemByIdentifier(property(Constants.JCR_BASEVERSION).getString(),
                                                       false);
    }

    if (session.getTransientNodesManager().hasPendingChanges(getInternalPath()))
      throw new InvalidItemStateException("Node has pending changes " + getPath());

    if (hasProperty(Constants.JCR_MERGEFAILED))
      throw new VersionException("Node has jcr:mergeFailed " + getPath());

    if (!parent().checkLocking())
      throw new LockException("Node " + parent().getPath() + " is locked ");

    // the new version identifier
    String verIdentifier = IdGenerator.generate();

    SessionChangesLog changesLog = new SessionChangesLog(session.getId());

    VersionHistoryImpl vh = versionHistory(false);
    vh.addVersion(this.nodeData(), verIdentifier, changesLog);

    changesLog.add(ItemState.createUpdatedState(updatePropertyData(Constants.JCR_ISCHECKEDOUT,
                                                                   new TransientValueData(false))));

    changesLog.add(ItemState.createUpdatedState(updatePropertyData(Constants.JCR_BASEVERSION,
                                                                   new TransientValueData(new Identifier(verIdentifier)))));

    changesLog.add(ItemState.createUpdatedState(updatePropertyData(Constants.JCR_PREDECESSORS,
                                                                   new ArrayList<ValueData>())));

    dataManager.getTransactManager().save(changesLog);

    VersionImpl version = (VersionImpl) dataManager.getItemByIdentifier(verIdentifier, true);

    session.getActionHandler().postCheckin(this);
    return version;
  }

  public void checkout() throws RepositoryException, UnsupportedRepositoryOperationException {

    checkValid();

    if (!this.isNodeType(Constants.MIX_VERSIONABLE))
      throw new UnsupportedRepositoryOperationException("Node.checkout() is not supported for not mix:versionable node ");

    if (checkedOut())
      return;

    SessionChangesLog changesLog = new SessionChangesLog(session.getId());

    changesLog.add(ItemState.createUpdatedState(updatePropertyData(Constants.JCR_ISCHECKEDOUT,
                                                                   new TransientValueData(true))));

    ValueData baseVersion = ((PropertyData) dataManager.getItemData(nodeData(),
                                                                    new QPathEntry(Constants.JCR_BASEVERSION,
                                                                                   0))).getValues()
                                                                                       .get(0);

    changesLog.add(ItemState.createUpdatedState(updatePropertyData(Constants.JCR_PREDECESSORS,
                                                                   baseVersion)));

    dataManager.getTransactManager().save(changesLog);

    session.getActionHandler().postCheckout(this);
  }

  // /**
  // * @deprecated use isCheckedOut
  // * @return
  // * @throws UnsupportedRepositoryOperationException
  // * @throws RepositoryException
  // */
  // public boolean isCheckedOut_Old() throws
  // UnsupportedRepositoryOperationException,
  // RepositoryException {
  //
  // checkValid();
  //
  // if (isRoot())
  // return true;
  //
  // if (this.isNodeType(Constants.MIX_VERSIONABLE)) {
  // return ((Property) dataManager.getItem(nodeData(), new
  // QPathEntry(Constants.JCR_ISCHECKEDOUT,
  // 0), false)).getBoolean();
  // }
  //
  // NodeImpl ancestor = parent();
  // while (!ancestor.isRoot()) {
  // if (ancestor.isNodeType(Constants.MIX_VERSIONABLE))
  // return ancestor.isCheckedOut();
  // else
  // ancestor = ancestor.parent();
  // }
  // if (ancestor.isNodeType(Constants.MIX_VERSIONABLE))
  // return ancestor.isCheckedOut();
  // else
  // return true;
  // }

  public boolean isCheckedOut() throws UnsupportedRepositoryOperationException, RepositoryException {

    checkValid();

    return checkedOut();
  }

  /**
   * Tell if this node or its nearest versionable ancestor is checked-out.
   * 
   * @return
   * @throws UnsupportedRepositoryOperationException
   * @throws RepositoryException
   */
  public boolean checkedOut() throws UnsupportedRepositoryOperationException, RepositoryException {

    // if (isRoot())
    // return true;

    NodeData vancestor = getVersionableAncestor();
    if (vancestor != null) {
      PropertyData isCheckedOut = (PropertyData) dataManager.getItemData(vancestor,
                                                                         new QPathEntry(Constants.JCR_ISCHECKEDOUT,
                                                                                        1));
      try {
        return ValueDataConvertor.readBoolean(isCheckedOut.getValues().get(0));
      } catch (IOException e) {
        throw new RepositoryException("Can't read property "
            + locationFactory.createJCRPath(vancestor.getQPath()).getAsString(false)
            + "/jcr:isCheckedOut. " + e, e);
      }
    }

    return true;
  }

  /**
   * Get nearest versionable ancestor NodeData. If the node is mix:versionable
   * this NodeData will be returned.
   * 
   * @return NodeData of versionable ancestor or null if no versionable ancestor
   *         exists.
   * @throws RepositoryException
   */
  public NodeData getVersionableAncestor() throws RepositoryException {
    NodeData node = nodeData();
    NodeTypeDataManager ntman = session.getWorkspace().getNodeTypesHolder();

    while (node.getParentIdentifier() != null) {
      if (ntman.isNodeType(Constants.MIX_VERSIONABLE,
                           node.getPrimaryTypeName(),
                           node.getMixinTypeNames())) {
        // mix:versionable has own jcr:isCheckedOut state
        return node;
      } else {
        // check on deeper ancestor
        NodeData ancestor = (NodeData) dataManager.getItemData(node.getParentIdentifier());
        if (ancestor == null)
          throw new RepositoryException("Parent not found for "
              + locationFactory.createJCRPath(node.getQPath()).getAsString(false) + ". Parent id "
              + node.getParentIdentifier());
        else
          node = ancestor;
      }
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Node#getVersionHistory()
   */
  public VersionHistory getVersionHistory() throws UnsupportedRepositoryOperationException,
                                           RepositoryException {

    checkValid();

    return versionHistory(true);
  }

  /**
   * For internal use. Doesn't check the InvalidItemStateException and may
   * return unpooled VersionHistory object.
   */
  public VersionHistoryImpl versionHistory(boolean pool) throws UnsupportedRepositoryOperationException,
                                                        RepositoryException {

    if (!this.isNodeType(Constants.MIX_VERSIONABLE))
      throw new UnsupportedRepositoryOperationException("Node is not mix:versionable " + getPath());

    PropertyData vhProp = (PropertyData) dataManager.getItemData(nodeData(),
                                                                 new QPathEntry(Constants.JCR_VERSIONHISTORY,
                                                                                1));
    if (vhProp == null)
      throw new UnsupportedRepositoryOperationException("Node does not have jcr:versionHistory "
          + getPath());

    try {
      return (VersionHistoryImpl) dataManager.getItemByIdentifier(new String(vhProp.getValues()
                                                                                   .get(0)
                                                                                   .getAsByteArray()),
                                                                  pool);
    } catch (IOException e) {
      throw new RepositoryException("Error of version history ID read " + e, e);
    }
  }

  public Version getBaseVersion() throws UnsupportedRepositoryOperationException,
                                 RepositoryException {

    checkValid();

    if (!this.isNodeType(Constants.MIX_VERSIONABLE))
      throw new UnsupportedRepositoryOperationException("Node is not versionable " + getPath());

    PropertyData bvProp = (PropertyData) dataManager.getItemData(nodeData(),
                                                                 new QPathEntry(Constants.JCR_BASEVERSION,
                                                                                1));
    try {
      return (Version) session.getNodeByUUID(ValueDataConvertor.readString(bvProp.getValues()
                                                                                 .get(0)));
    } catch (IOException e) {
      throw new RepositoryException("jcr:baseVersion property error " + e, e);
    }
  }

  public void restore(Version version, boolean removeExisting) throws VersionException,
                                                              ItemExistsException,
                                                              UnsupportedRepositoryOperationException,
                                                              LockException,
                                                              RepositoryException,
                                                              InvalidItemStateException {

    checkValid();

    if (!this.isNodeType(Constants.MIX_VERSIONABLE))
      throw new UnsupportedRepositoryOperationException("Node is not versionable " + getPath());

    if (session.hasPendingChanges())
      throw new InvalidItemStateException("Session has pending changes ");

    if (((VersionImpl) version).getInternalName().equals(Constants.JCR_ROOTVERSION))
      throw new VersionException("It is illegal to call restore() on jcr:rootVersion");

    if (!versionHistory(false).isVersionBelongToThis(version))
      throw new VersionException("Bad version " + version.getPath());

    // Check locking
    if (!checkLocking())
      throw new LockException("Node " + getPath() + " is locked ");

    NodeData destParent = (NodeData) dataManager.getItemData(nodeData().getParentIdentifier());
    ((VersionImpl) version).restore(this.getSession(),
                                    destParent,
                                    nodeData().getQPath().getName(),
                                    removeExisting);//log.info(dataManager.dump(
    // ))
  }

  public void restore(String versionName, boolean removeExisting) throws VersionException,
                                                                 ItemExistsException,
                                                                 UnsupportedRepositoryOperationException,
                                                                 LockException,
                                                                 RepositoryException,
                                                                 InvalidItemStateException {

    VersionImpl version = (VersionImpl) versionHistory(false).version(versionName, false);
    restore(version, removeExisting);
  }

  public void restore(Version version, String relPath, boolean removeExisting) throws VersionException,
                                                                              ItemExistsException,
                                                                              UnsupportedRepositoryOperationException,
                                                                              LockException,
                                                                              RepositoryException,
                                                                              InvalidItemStateException {

    if (JCRPath.THIS_RELPATH.equals(relPath)) {

      // restore at this position
      this.restore(version, removeExisting);
      // throw new RepositoryException("Can't restore node to the path '" +
      // relPath + "'");
    } else {

      // restore at relPath
      checkValid();

      if (session.hasPendingChanges())
        throw new InvalidItemStateException("Session has pending changes ");

      if (((VersionImpl) version).getInternalName().equals(Constants.JCR_ROOTVERSION))
        throw new VersionException("It is illegal to call restore() on jcr:rootVersion");

      QPath destPath = locationFactory.parseRelPath(relPath).getInternalPath();
      NodeImpl destParent = (NodeImpl) dataManager.getItem(nodeData(), destPath.makeParentPath()
                                                                               .getEntries(), false);
      if (destParent == null)
        throw new PathNotFoundException("Parent not found for " + relPath);

      if (!destParent.isNode())
        throw new ConstraintViolationException("Parent item is not a node. Rel path " + relPath);

      NodeImpl destNode = (NodeImpl) dataManager.getItem(destParent.nodeData(),
                                                         new QPathEntry(destPath.getName(),
                                                                        destPath.getIndex()),
                                                         false);

      if (destNode != null) {
        // Dest node exists

        if (!destNode.isNode())
          throw new ConstraintViolationException("Item at relPath is not a node "
              + destNode.getPath());

        if (!destNode.isNodeType(Constants.MIX_VERSIONABLE))
          throw new UnsupportedRepositoryOperationException("Node at relPath is not versionable "
              + destNode.getPath());

        if (!destNode.versionHistory(false).isVersionBelongToThis(version))
          throw new VersionException("Bad version " + version.getPath());

        // Check locking
        if (!destNode.parent().checkLocking())
          throw new LockException("Node " + destNode.getPath() + " is locked ");
      } else {
        // Dest node not found
        if (!destParent.checkedOut())
          throw new VersionException("Parent of a node at relPath is versionable and checked-in "
              + destParent.getPath());
      }

      ((VersionImpl) version).restore(session,
                                      destParent.nodeData(),
                                      destPath.getName(),
                                      removeExisting);
    }
  }

  public void restoreByLabel(String versionLabel, boolean removeExisting) throws VersionException,
                                                                         ItemExistsException,
                                                                         UnsupportedRepositoryOperationException,
                                                                         LockException,
                                                                         RepositoryException,
                                                                         InvalidItemStateException {

    checkValid();

    VersionImpl version = (VersionImpl) getVersionHistory().getVersionByLabel(versionLabel);
    restore(version, removeExisting);

  }

  public NodeIterator merge(String srcWorkspace, boolean bestEffort) throws UnsupportedRepositoryOperationException,
                                                                    NoSuchWorkspaceException,
                                                                    AccessDeniedException,
                                                                    MergeException,
                                                                    RepositoryException,
                                                                    InvalidItemStateException {

    checkValid();

    if (session.hasPendingChanges())
      throw new InvalidItemStateException("Session has pending changes ");

    Map<String, String> failed = new HashMap<String, String>();

    // get corresponding node
    SessionImpl corrSession = ((RepositoryImpl) session.getRepository()).internalLogin(session.getUserState(),
                                                                                       srcWorkspace);
    // .login(session.getCredentials(), srcWorkspace);

    ItemDataMergeVisitor visitor = new ItemDataMergeVisitor(this.session,
                                                            corrSession,
                                                            failed,
                                                            bestEffort);
    this.nodeData().accept(visitor);

    SessionChangesLog changes = visitor.getMergeChanges();

    EntityCollection failedIter = createMergeFailed(failed, changes);

    if (changes.getSize() > 0)
      dataManager.getTransactManager().save(changes);

    return failedIter;
  }

  private EntityCollection createMergeFailed(Map<String, String> failed, SessionChangesLog changes) throws RepositoryException {

    EntityCollection res = new EntityCollection();

    TransientPropertyData mergeFailed = (TransientPropertyData) dataManager.getItemData(nodeData(),
                                                                                        new QPathEntry(Constants.JCR_MERGEFAILED,
                                                                                                       0));

    List<ValueData> mergeFailedRefs = null;
    int state = 0;
    if (mergeFailed != null) {
      mergeFailed = mergeFailed.clone();
      mergeFailedRefs = mergeFailed.getValues();
      state = ItemState.UPDATED;
    } else {
      mergeFailedRefs = new ArrayList<ValueData>();
      mergeFailed = TransientPropertyData.createPropertyData((NodeData) getData(),
                                                             Constants.JCR_MERGEFAILED,
                                                             PropertyType.REFERENCE,
                                                             true,
                                                             mergeFailedRefs);
      state = ItemState.ADDED;
    }

    nextFail: for (String identifier : failed.keySet()) {
      NodeImpl versionable = (NodeImpl) session.getNodeByUUID(identifier);
      res.add(versionable);
      String offendingIdentifier = failed.get(identifier);

      for (ValueData vd : mergeFailedRefs) {
        try {
          String mfIdentifier = new String(vd.getAsByteArray());
          if (mfIdentifier.equals(offendingIdentifier)) {
            // offending version is alredy in jcr:mergeFailed, skip it
            continue nextFail;
          }
        } catch (IOException e) {
          throw new RepositoryException("jcr:mergeFailed read error " + e, e);
        }
      }

      mergeFailedRefs.add(new TransientValueData(offendingIdentifier));
    }

    changes.add(new ItemState(mergeFailed, state, true, getInternalPath(), true));

    return res;
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Node#doneMerge(javax.jcr.version.Version)
   */
  public void doneMerge(Version version) throws VersionException,
                                        InvalidItemStateException,
                                        UnsupportedRepositoryOperationException,
                                        RepositoryException {

    PlainChangesLog changesLog = new PlainChangesLogImpl(session.getId());

    VersionImpl base = (VersionImpl) getBaseVersion();
    base.addPredecessor(version.getUUID(), changesLog);
    removeMergeFailed(version, changesLog);

    dataManager.getTransactManager().save(changesLog);
  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Node#cancelMerge(javax.jcr.version.Version)
   */
  public void cancelMerge(Version version) throws VersionException,
                                          InvalidItemStateException,
                                          UnsupportedRepositoryOperationException,
                                          RepositoryException {

    checkValid();

    PlainChangesLog changesLog = new PlainChangesLogImpl(session.getId());

    removeMergeFailed(version, changesLog);

    dataManager.getTransactManager().save(changesLog);
  }

  private void removeMergeFailed(Version version, PlainChangesLog changesLog) throws RepositoryException {

    TransientPropertyData mergeFailed = (TransientPropertyData) dataManager.getItemData(nodeData(),
                                                                                        new QPathEntry(Constants.JCR_MERGEFAILED,
                                                                                                       0));

    if (mergeFailed == null)
      return;

    List<ValueData> mf = new ArrayList<ValueData>();
    for (ValueData mfvd : mergeFailed.getValues()) {
      try {
        String mfIdentifier = new String(mfvd.getAsByteArray());
        if (!mfIdentifier.equals(version.getUUID()))
          mf.add(mfvd);
      } catch (IOException e) {
        throw new RepositoryException("Remove jcr:mergeFailed error " + e, e);
      }
    }

    if (mf.size() > 0) {
      PropertyData mergeFailedRef = TransientPropertyData.createPropertyData(nodeData(),
                                                                             Constants.JCR_MERGEFAILED,
                                                                             PropertyType.REFERENCE,
                                                                             true,
                                                                             mf);
      changesLog.add(ItemState.createUpdatedState(mergeFailedRef));
    } else {
      // Once the last reference in jcr:mergeFailed has been either moved
      // to jcr:predecessors (with doneMerge) or just removed from
      // jcr:mergeFailed (with cancelMerge) the jcr:mergeFailed
      // property is automatically remove
      changesLog.add(ItemState.createDeletedState(mergeFailed.clone(), true));
    }
  }

  // Locks

  public Lock lock(boolean isDeep, long timeOut) throws UnsupportedRepositoryOperationException,
                                                LockException,
                                                AccessDeniedException,
                                                RepositoryException {
    checkValid();

    if (!isNodeType(Constants.MIX_LOCKABLE))
      throw new LockException("Node is not lockable " + getPath());

    if (dataManager.hasPendingChanges(getInternalPath()))
      throw new InvalidItemStateException("Node has pending unsaved changes " + getPath());

    Lock newLock = session.getLockManager().addPendingLock(this, isDeep, false, timeOut);

    PlainChangesLog changesLog = new PlainChangesLogImpl(new ArrayList<ItemState>(),
                                                         session.getId(),
                                                         ExtendedEvent.LOCK);

    PropertyData propData = TransientPropertyData.createPropertyData(nodeData(),
                                                                     Constants.JCR_LOCKOWNER,
                                                                     PropertyType.STRING,
                                                                     false,
                                                                     new TransientValueData(session.getUserID()));
    changesLog.add(ItemState.createAddedState(propData));

    propData = TransientPropertyData.createPropertyData(nodeData(),
                                                        Constants.JCR_LOCKISDEEP,
                                                        PropertyType.BOOLEAN,
                                                        false,
                                                        new TransientValueData(isDeep));
    changesLog.add(ItemState.createAddedState(propData));

    dataManager.getTransactManager().save(changesLog);

    session.getActionHandler().postLock(this);
    return newLock;

  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Node#lock(boolean, boolean)
   */
  public Lock lock(boolean isDeep, boolean isSessionScoped) throws UnsupportedRepositoryOperationException,
                                                           LockException,
                                                           AccessDeniedException,
                                                           RepositoryException {

    checkValid();

    if (!isNodeType(Constants.MIX_LOCKABLE))
      throw new LockException("Node is not lockable " + getPath());

    if (dataManager.hasPendingChanges(getInternalPath()))
      throw new InvalidItemStateException("Node has pending unsaved changes " + getPath());

    Lock newLock = session.getLockManager().addPendingLock(this, isDeep, isSessionScoped, -1);

    PlainChangesLog changesLog = new PlainChangesLogImpl(new ArrayList<ItemState>(),
                                                         session.getId(),
                                                         ExtendedEvent.LOCK);

    PropertyData propData = TransientPropertyData.createPropertyData(nodeData(),
                                                                     Constants.JCR_LOCKOWNER,
                                                                     PropertyType.STRING,
                                                                     false,
                                                                     new TransientValueData(session.getUserID()));
    changesLog.add(ItemState.createAddedState(propData));

    propData = TransientPropertyData.createPropertyData(nodeData(),
                                                        Constants.JCR_LOCKISDEEP,
                                                        PropertyType.BOOLEAN,
                                                        false,
                                                        new TransientValueData(isDeep));
    changesLog.add(ItemState.createAddedState(propData));

    dataManager.getTransactManager().save(changesLog);

    session.getActionHandler().postLock(this);
    return newLock;

  }

  /*
   * (non-Javadoc)
   * @see javax.jcr.Node#getLock()
   */
  public Lock getLock() throws UnsupportedRepositoryOperationException,
                       LockException,
                       AccessDeniedException,
                       RepositoryException {

    checkValid();

    LockImpl lock = session.getLockManager().getLock(this);
    if (lock == null)
      throw new LockException("Lock not found " + getPath());
    return lock;

  }

  /**
   * @see javax.jcr.Node#unlock()
   */
  public void unlock() throws UnsupportedRepositoryOperationException,
                      LockException,
                      AccessDeniedException,
                      RepositoryException {

    checkValid();

    if (!session.getLockManager().holdsLock((NodeData) this.getData()))
      throw new LockException("The node not locked " + getPath());

    if (!session.getLockManager().isLockHolder(this))
      throw new LockException("There are no permission to unlock the node " + getPath());

    if (dataManager.hasPendingChanges(getInternalPath()))
      throw new InvalidItemStateException("Node has pending unsaved changes " + getPath());

    doUnlock();

    session.getActionHandler().postUnlock(this);
  }

  /**
   * Remove mix:lockable properties.
   * 
   * @throws RepositoryException
   */
  protected void doUnlock() throws RepositoryException {

    PlainChangesLog changesLog = new PlainChangesLogImpl(new ArrayList<ItemState>(),
                                                         session.getId(),
                                                         ExtendedEvent.UNLOCK);

    ItemData lockOwner = dataManager.getItemData(nodeData(),
                                                 new QPathEntry(Constants.JCR_LOCKOWNER, 0));

    changesLog.add(ItemState.createDeletedState(lockOwner));

    ItemData lockIsDeep = dataManager.getItemData(nodeData(),
                                                  new QPathEntry(Constants.JCR_LOCKISDEEP, 0));

    changesLog.add(ItemState.createDeletedState(lockIsDeep));

    dataManager.getTransactManager().save(changesLog);
  }

  /**
   * {@inheritDoc}
   */
  public boolean holdsLock() throws RepositoryException {

    checkValid();
    return session.getLockManager().holdsLock((NodeData) getData());
  }

  /**
   * {@inheritDoc}
   */
  public boolean isLocked() throws RepositoryException {

    checkValid();
    return session.getLockManager().isLocked((NodeData) this.getData());
  }

  boolean checkLocking() throws RepositoryException {
    return (!isLocked() || session.getLockManager().isLockHolder(this) || session.getUserID()
                                                                                 .equals(SystemIdentity.SYSTEM));
  }

  // ////////////////// Item implementation ////////////////////

  /**
   * @see javax.jcr.Item#accept
   */
  public void accept(ItemVisitor visitor) throws RepositoryException {
    checkValid();

    visitor.visit(this);
  }

  /**
   * @see javax.jcr.Item#isNode
   */
  public boolean isNode() {
    return true;
  }

  /**
   * Add autocreated items to this node. No checks will be passed for
   * autocreated items.
   */
  public void addAutoCreatedItems(InternalQName nodeTypeName) throws RepositoryException,
                                                             ConstraintViolationException {
    addAutoCreatedItems(nodeData(), nodeTypeName);
  }

  public void addAutoCreatedItems(NodeData parent, InternalQName nodeTypeName) throws RepositoryException,
                                                                              ConstraintViolationException {

    NodeTypeData type = session.getWorkspace().getNodeTypesHolder().findNodeType(nodeTypeName);

    NodeTypeDataManager ntmanager = session.getWorkspace().getNodeTypesHolder();

    // Add autocreated child properties
    for (PropertyDefinitionData pdef : ntmanager.getAllPropertyDefinitions(type.getName())) {

      // if (propDefs[i] == null) // TODO it is possible for not mandatory
      // propDef
      // continue;

      if (pdef.isAutoCreated()) {

        ItemData pdata = dataManager.getItemData(parent, new QPathEntry(pdef.getName(), 0));
        if (pdata == null || pdata.isNode()) {

          List<ValueData> listAutoCreateValue = autoCreatedValue(parent, type, pdef);

          if (listAutoCreateValue != null)
            dataManager.update(ItemState.createAddedState(TransientPropertyData.createPropertyData(parent,
                                                                                                   pdef.getName(),
                                                                                                   pdef.getRequiredType(),
                                                                                                   pdef.isMultiple(),
                                                                                                   listAutoCreateValue)),
                               false);
        } else {
          // TODO if autocreated property exists it's has wrong data (e.g. ACL)
          // - throw an exception
          if (LOG.isDebugEnabled()) {
            LOG.debug("Skipping existed property " + pdef.getName() + " in " + getPath()
                + "   during the automatic creation of items for " + nodeTypeName.getAsString()
                + " nodetype or mixin type");
          }
        }
      }
    }

    // Add autocreated child nodes
    for (NodeDefinitionData ndef : ntmanager.getAllChildNodeDefinitions(type.getName())) {
      if (ndef.isAutoCreated()) {
        TransientNodeData childNodeData = TransientNodeData.createNodeData(parent, ndef.getName(),
        // TODO default NT may be null, or check it in NT manager
                                                                           ndef.getDefaultPrimaryType(),
                                                                           IdGenerator.generate());
        dataManager.update(ItemState.createAddedState(childNodeData), false);
        addAutoCreatedItems(childNodeData, childNodeData.getPrimaryTypeName());

      }
    }

    // versionable
    if (session.getWorkspace()
               .getNodeTypesHolder()
               .isNodeType(Constants.MIX_VERSIONABLE, new InternalQName[] { type.getName() })) {
      PlainChangesLogImpl changes = new PlainChangesLogImpl();
      // using VH helper as for one new VH, all changes in changes log
      new VersionHistoryDataHelper(parent, changes, dataManager, session.getWorkspace()
                                                                        .getNodeTypesHolder());
      for (ItemState istate : changes.getAllStates()) {
        dataManager.update(istate, false);
      }
    }
  }

  private List<ValueData> autoCreatedValue(NodeData parent,
                                           NodeTypeData type,
                                           PropertyDefinitionData def) throws RepositoryException {
    NodeTypeDataManager typeDataManager = session.getWorkspace().getNodeTypesHolder();
    List<ValueData> vals = new ArrayList<ValueData>();

    if (typeDataManager.isNodeType(Constants.NT_BASE, new InternalQName[] { type.getName() })
        && def.getName().equals(Constants.JCR_PRIMARYTYPE)) {
      vals.add(new TransientValueData(parent.getPrimaryTypeName()));

    } else if (typeDataManager.isNodeType(Constants.MIX_REFERENCEABLE,
                                          new InternalQName[] { type.getName() })
        && def.getName().equals(Constants.JCR_UUID)) {
      vals.add(new TransientValueData(parent.getIdentifier()));

    } else if (typeDataManager.isNodeType(Constants.NT_HIERARCHYNODE,
                                          new InternalQName[] { type.getName() })
        && def.getName().equals(Constants.JCR_CREATED)) {
      vals.add(new TransientValueData(dataManager.getTransactManager()
                                                 .getStorageDataManager()
                                                 .getCurrentTime()));

    } else if (typeDataManager.isNodeType(Constants.EXO_OWNEABLE,
                                          new InternalQName[] { type.getName() })
        && def.getName().equals(Constants.EXO_OWNER)) {
      String owner = session.getUserID();
      vals.add(new TransientValueData(owner));
      setACL(new AccessControlList(owner, getACL().getPermissionEntries()));

    } else if (typeDataManager.isNodeType(Constants.EXO_PRIVILEGEABLE,
                                          new InternalQName[] { type.getName() })
        && def.getName().equals(Constants.EXO_PERMISSIONS)) {
      for (AccessControlEntry ace : parent.getACL().getPermissionEntries()) {
        vals.add(new TransientValueData(ace));
      }

    } else {
      String[] propVal = def.getDefaultValues();
      // there can be null in definition but should not be null value
      if (propVal != null) {
        for (String v : propVal) {
          if (v != null)
            if (def.getRequiredType() == PropertyType.UNDEFINED)
              vals.add(((BaseValue) valueFactory.createValue(v)).getInternalData());
            else
              vals.add(((BaseValue) valueFactory.createValue(v, def.getRequiredType())).getInternalData());
          else {
            vals.add(null);
          }
        }
      } else
        return null;
    }

    return vals;
  }

  public String[] getMixinTypeNames() throws RepositoryException {
    NodeType[] mixinTypes = getMixinNodeTypes();
    String[] mtNames = new String[mixinTypes.length];
    for (int i = 0; i < mtNames.length; i++)
      mtNames[i] = mixinTypes[i].getName();
    return mtNames;
  }

  public void validateMandatoryChildren() throws ConstraintViolationException,
                                         AccessDeniedException,
                                         RepositoryException {

    Collection<ItemDefinitionData> mandatoryItemDefs = session.getWorkspace()
                                                              .getNodeTypesHolder()
                                                              .getManadatoryItemDefs(nodeData().getPrimaryTypeName(),
                                                                                     nodeData().getMixinTypeNames());
    for (ItemDefinitionData itemDefinitionData : mandatoryItemDefs) {

      if (getSession().getTransientNodesManager()
                      .getItemData(nodeData(), new QPathEntry(itemDefinitionData.getName(), 0)) == null)
        throw new ConstraintViolationException("Mandatory item " + itemDefinitionData.getName()
            + " not found. Node [" + getPath() + " primary type: "
            + this.getPrimaryNodeType().getName() + "]");

    }
  }

  // ----------------------------- ExtendedNode -----------------------------

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.core.ExtendedNode#setPermissions(java.util
   * .Map)
   */
  public void setPermissions(Map permissions) throws RepositoryException,
                                             AccessDeniedException,
                                             AccessControlException {

    if (!isNodeType(Constants.EXO_PRIVILEGEABLE))
      throw new AccessControlException("Node is not exo:privilegeable " + getPath());

    if (permissions.size() == 0)
      throw new RepositoryException("Permission map size cannot be 0");

    checkPermission(PermissionType.CHANGE_PERMISSION);

    List<AccessControlEntry> aces = new ArrayList<AccessControlEntry>();
    for (Iterator<String> i = permissions.keySet().iterator(); i.hasNext();) {
      String identity = i.next();
      if (identity == null)
        throw new RepositoryException("Identity cannot be null");

      String[] perm = (String[]) permissions.get(identity);
      if (perm == null)
        throw new RepositoryException("Permissions cannot be null");

      for (int j = 0; j < perm.length; j++) {
        AccessControlEntry ace = new AccessControlEntry(identity, perm[j]);
        aces.add(ace);
      }
    }
    AccessControlList acl = new AccessControlList(getACL().getOwner(), aces);
    updatePermissions(acl);
    setACL(acl);

  }

  private void updatePermissions(AccessControlList acl) throws RepositoryException {
    List<ValueData> permValues = new ArrayList<ValueData>();

    List<AccessControlEntry> aces = acl.getPermissionEntries(); // new
    for (AccessControlEntry ace : aces) {
      ValueData vd = new TransientValueData(ace);
      permValues.add(vd);
    }

    TransientPropertyData permProp = (TransientPropertyData) dataManager.getItemData(nodeData(),
                                                                                     new QPathEntry(Constants.EXO_PERMISSIONS,
                                                                                                    0));

    permProp = new TransientPropertyData(permProp.getQPath(),
                                         permProp.getIdentifier(),
                                         permProp.getPersistedVersion(),
                                         permProp.getType(),
                                         permProp.getParentIdentifier(),
                                         permProp.isMultiValued());

    permProp.setValues(permValues);

    dataManager.update(new ItemState(data, ItemState.MIXIN_CHANGED, false, null, true), false);
    dataManager.update(ItemState.createUpdatedState(permProp, true), false);

  }

  /**
   * Return a copy of ACL for this node for information purpose only.
   * 
   * @see org.exoplatform.services.jcr.core.ExtendedNode#getACL()
   */
  public AccessControlList getACL() throws RepositoryException {

    checkValid();

    List<AccessControlEntry> listEntry = new ArrayList<AccessControlEntry>();

    for (AccessControlEntry aEntry : nodeData().getACL().getPermissionEntries()) {
      listEntry.add(new AccessControlEntry(aEntry.getIdentity(), aEntry.getPermission()));
    }

    return new AccessControlList(nodeData().getACL().getOwner(), listEntry);
  }

  private void setACL(AccessControlList acl) {
    ((NodeData) data).setACL(acl);
  }

  /**
   * Clears Access Control List
   * 
   * @see org.exoplatform.services.jcr.core.ExtendedNode#clearACL()
   */
  public void clearACL() throws RepositoryException, AccessControlException {

    if (!isNodeType(Constants.EXO_PRIVILEGEABLE))
      throw new AccessControlException("Node is not exo:privilegeable " + getPath());

    checkPermission(PermissionType.CHANGE_PERMISSION);

    List<AccessControlEntry> aces = new ArrayList<AccessControlEntry>();
    for (String perm : PermissionType.ALL) {
      AccessControlEntry ace = new AccessControlEntry(SystemIdentity.ANY, perm);
      aces.add(ace);
    }
    AccessControlList acl = new AccessControlList(getACL().getOwner(), aces);

    setACL(acl);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.core.ExtendedNode#removePermission(java.lang
   * .String)
   */
  public void removePermission(String identity) throws RepositoryException, AccessControlException {

    if (!isNodeType(Constants.EXO_PRIVILEGEABLE))
      throw new AccessControlException("Node is not exo:privilegeable " + getPath());

    checkPermission(PermissionType.CHANGE_PERMISSION);

    AccessControlList acl = new AccessControlList(getACL().getOwner(),
                                                  getACL().getPermissionEntries());
    acl.removePermissions(identity);
    updatePermissions(acl);
    setACL(acl);

  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.core.ExtendedNode#removePermission(java.lang
   * .String,java.lang. String)
   */
  public void removePermission(String identity, String permission) throws RepositoryException,
                                                                  AccessControlException {

    if (!isNodeType(Constants.EXO_PRIVILEGEABLE))
      throw new AccessControlException("Node is not exo:privilegeable " + getPath());

    checkPermission(PermissionType.CHANGE_PERMISSION);

    AccessControlList acl = new AccessControlList(getACL().getOwner(),
                                                  getACL().getPermissionEntries());
    acl.removePermissions(identity, permission);
    updatePermissions(acl);
    setACL(acl);

  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.core.ExtendedNode#setPermission(java.lang.
   * String, java.lang.String[])
   */
  public void setPermission(String identity, String[] permission) throws RepositoryException,
                                                                 AccessControlException {

    if (!isNodeType(Constants.EXO_PRIVILEGEABLE))
      throw new AccessControlException("Node is not exo:privilegeable " + getPath());

    if (identity == null)
      throw new RepositoryException("Identity cannot be null");

    if (permission == null)
      throw new RepositoryException("Permission cannot be null");

    // check if changing permission allowed
    checkPermission(PermissionType.CHANGE_PERMISSION);
    AccessControlList acl = new AccessControlList(getACL().getOwner(),
                                                  getACL().getPermissionEntries());
    acl.removePermissions(identity);
    acl.addPermissions(identity, permission);
    updatePermissions(acl);
    setACL(acl);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.exoplatform.services.jcr.core.ExtendedNode#checkPermission(java.lang
   * .String)
   */
  public void checkPermission(String actions) throws AccessControlException, RepositoryException {

    checkValid();

    if (!session.getAccessManager().hasPermission(getACL(),
                                                  actions,
                                                  session.getUserState().getIdentity()))
      throw new AccessControlException("Permission denied " + getPath() + " : " + actions);
  }

  // ----------------------------- Object -----------------------------

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NodeImpl) {
      NodeImpl otherNode = (NodeImpl) obj;

      if (!otherNode.isValid() || !this.isValid())
        return false;

      try {
        if (otherNode.isNodeType("mix:referenceable") && this.isNodeType("mix:referenceable")) {
          // by UUID
          // getProperty("jcr:uuid") is more correct, but may decrease
          // performance
          return getInternalIdentifier().equals(otherNode.getInternalIdentifier());
        }
        // by path
        return getLocation().equals(otherNode.getLocation());
      } catch (RepositoryException e) {
        return false;
      }
    }
    return false;
  }

  // ////////////////// NEW METHODS (since 1.5) ////////////////
  // @Deprecated
  // private ExtendedNodeType nodeType(InternalQName qName) throws
  // NoSuchNodeTypeException,
  // RepositoryException {
  //    
  // return session.getWorkspace().getNodeTypeManager()).getNodeTypesHolder().
  // findNodeType(qName);
  // }

  // @Deprecated
  // public ExtendedNodeType[] getAllNodeTypes() throws RepositoryException {
  // ExtendedNodeType primaryType = nodeType(nodeData().getPrimaryTypeName());
  //
  // InternalQName[] mixinNames = nodeData().getMixinTypeNames();
  // ExtendedNodeType[] nodeTypes = new ExtendedNodeType[mixinNames.length + 1];
  // nodeTypes[0] = primaryType;
  // for (int i = 1; i <= mixinNames.length; i++) {
  // nodeTypes[i] = nodeType(mixinNames[i - 1]);
  // }
  //
  // return nodeTypes;
  //
  // return nodeTypes(nodeData());
  // }

  protected NodeData nodeData() {
    return (NodeData) data;
  }

  // ===================== helpers =====================

  protected PropertyData updatePropertyData(InternalQName name, ValueData value) throws RepositoryException {

    PropertyData existed = (PropertyData) dataManager.getItemData(nodeData(), new QPathEntry(name,
                                                                                             0));

    if (existed == null)
      throw new RepositoryException("Property data is not found " + name.getAsString()
          + " for node " + nodeData().getQPath().getAsString());

    TransientPropertyData tdata = new TransientPropertyData(QPath.makeChildPath(getInternalPath(),
                                                                                name),
                                                            existed.getIdentifier(),
                                                            existed.getPersistedVersion(),
                                                            existed.getType(),
                                                            existed.getParentIdentifier(),
                                                            existed.isMultiValued());

    tdata.setValue(value);
    return tdata;

  }

  protected PropertyData updatePropertyData(InternalQName name, List<ValueData> values) throws RepositoryException {

    PropertyData existed = (PropertyData) dataManager.getItemData(nodeData(), new QPathEntry(name,
                                                                                             0));

    if (existed == null)
      throw new RepositoryException("Property data is not found " + name.getAsString()
          + " for node " + nodeData().getQPath().getAsString());

    TransientPropertyData tdata = new TransientPropertyData(QPath.makeChildPath(getInternalPath(),
                                                                                name),
                                                            existed.getIdentifier(),
                                                            existed.getPersistedVersion(),
                                                            existed.getType(),
                                                            existed.getParentIdentifier(),
                                                            existed.isMultiValued());

    if (!existed.isMultiValued())
      throw new ValueFormatException("An existed property is single-valued " + name.getAsString());

    tdata.setValues(values);
    return tdata;
  }
}
