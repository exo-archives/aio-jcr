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
package org.exoplatform.services.jcr.impl.xml.importing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NamespaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.core.nodetype.ExtendedItemDefinition;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.core.ItemImpl;
import org.exoplatform.services.jcr.impl.core.JCRName;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeDefinitionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.PropertyDefinitionImpl;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataRemoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.xml.XmlSaveType;
import org.exoplatform.services.jcr.impl.xml.importing.dataflow.ImportNodeData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Peter Nedonosko
 * peter.nedonosko@exoplatform.com.ua 22.09.2006
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ImporterBase.java 13421 2007-03-15 10:46:47Z geaz $
 */
public abstract class BaseXmlImporter implements ContentImporter {
  /**
   * 
   */
  private final Log                   log  = ExoLogger.getLogger("jcr.ImporterBase");

  private final XmlSaveType           saveType;

  protected final InvocationContext   context;

  protected final Stack<NodeData>     tree = new Stack<NodeData>();

  protected final PlainChangesLogImpl changesLog;

  protected LocationFactory           locationFactory;

  /**
   * The NodeTypeManager
   */
  protected final NodeTypeManagerImpl ntManager;

  protected final SessionImpl         session;

  protected final int                 uuidBehavior;

  // protected final boolean respectPropertyDefinitionsConstraints;

  public BaseXmlImporter(NodeImpl parent,
                         int uuidBehavior,
                         XmlSaveType saveType,
                         InvocationContext context) {

    this.saveType = saveType;
    this.context = context;
    // this.respectPropertyDefinitionsConstraints =
    // respectPropertyDefinitionsConstraints;
    this.session = parent.getSession();
    this.ntManager = (NodeTypeManagerImpl) session.getRepository().getNodeTypeManager();
    this.locationFactory = session.getLocationFactory();
    this.uuidBehavior = uuidBehavior;
    this.tree.push((NodeData) parent.getData());
    this.changesLog = new PlainChangesLogImpl(session.getId());
  }

  /**
   * @param parentData
   * @return
   */
  public int getNextChildOrderNum(NodeData parentData) {
    int max = -1;

    for (ItemState itemState : changesLog.getAllStates()) {
      ItemData stateData = itemState.getData();
      if (isParent(stateData, parentData) && stateData.isNode()) {
        int cur = ((NodeData) stateData).getOrderNumber();
        if (cur > max)
          max = cur;
      }
    }
    return ++max;
  }

  /**
   * @param parentData
   * @param name
   * @param skipIdentifier
   * @return
   * @throws PathNotFoundException
   * @throws IllegalPathException
   * @throws RepositoryException
   */
  public int getNodeIndex(NodeData parentData, InternalQName name, String skipIdentifier) throws PathNotFoundException,
                                                                                         IllegalPathException,
                                                                                         RepositoryException {

    int newIndex = 1;

    NodeDefinitionImpl nodedef = session.getWorkspace()
                                        .getNodeTypeManager()
                                        .findNodeDefinition(name,
                                                            parentData.getPrimaryTypeName(),
                                                            parentData.getMixinTypeNames());

    ItemData sameNameNode = null;
    try {
      sameNameNode = session.getTransientNodesManager().getItemData(parentData,
                                                                    new QPathEntry(name, 0));
    } catch (PathNotFoundException e) {
      // Ok no same name node;
      return newIndex;
    }

    List<ItemState> transientAddChilds = getItemStatesList(parentData,
                                                           name,
                                                           ItemState.ADDED,
                                                           skipIdentifier);
    List<ItemState> transientDeletedChilds = getItemStatesList(parentData,
                                                               new QPathEntry(name, 0),
                                                               ItemState.DELETED,
                                                               null);

    if (!nodedef.allowsSameNameSiblings()
        && ((sameNameNode != null) || (transientAddChilds.size() > 0))) {
      if ((sameNameNode != null) && (transientDeletedChilds.size() < 1)) {
        throw new ItemExistsException("The node  already exists in "
            + sameNameNode.getQPath().getAsString() + " and same name sibling is not allowed ");
      }
      if (transientAddChilds.size() > 0) {
        throw new ItemExistsException("The node  already exists in add state "
            + "  and same name sibling is not allowed ");

      }
    }

    newIndex += transientAddChilds.size();

    List<NodeData> existedChilds = session.getTransientNodesManager().getChildNodesData(parentData);

    // Calculate SNS index for dest root
    for (NodeData child : existedChilds) {
      // skeep deleted items
      if (transientDeletedChilds.size() != 0) {
        continue;
      }

      if (child.getQPath().getName().equals(name)) {
        newIndex++; // next sibling index
      }

    }

    // searching
    return newIndex;
  }

  /**
   * @return
   */
  public XmlSaveType getSaveType() {
    return saveType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.Importer#registerNamespace(java.lang.String,
   *      java.lang.String)
   */
  public void registerNamespace(String prefix, String uri) {
    try {
      session.getWorkspace().getNamespaceRegistry().getPrefix(uri);
    } catch (NamespaceException e) {
      try {
        session.getWorkspace().getNamespaceRegistry().registerNamespace(prefix, uri);
      } catch (NamespaceException e1) {
        throw new RuntimeException(e1);
      } catch (RepositoryException e1) {
        throw new RuntimeException(e1);
      }
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.Importer#save()
   */
  public void save() throws RepositoryException {
    Collections.sort(changesLog.getAllStates(), new PathSorter());

    if (log.isDebugEnabled()) {
      String str = "";
      for (int i = 0; i < changesLog.getAllStates().size(); i++)
        str += " " + ItemState.nameFromValue(changesLog.getAllStates().get(i).getState()) + "\t\t"
            + changesLog.getAllStates().get(i).getData().getIdentifier()
            + "\t"
            + "isPersisted="
            + changesLog.getAllStates().get(i).isPersisted()
            + "\t"
            // + "parentId = " +
            // itemStatesList.get(i).getData().getParentIdentifier() + "\t"
            + "isEventFire=" + changesLog.getAllStates().get(i).isEventFire() + "\t"
            + "isInternallyCreated=" + changesLog.getAllStates().get(i).isInternallyCreated()
            + "\t" + changesLog.getAllStates().get(i).getData().getQPath().getAsString() + "\n";
      log.debug(str);
    }

    try {
      switch (saveType) {
      case SESSION:
        for (ItemState itemState : changesLog.getAllStates()) {
          if (itemState.isAdded())
            session.getTransientNodesManager().update(itemState, false); // TODO
          // pool=false
          else if (itemState.isDeleted())
            session.getTransientNodesManager().delete(itemState.getData());
        }
        break;
      case WORKSPACE:
        session.getTransientNodesManager().getTransactManager().save(changesLog);
        break;
      }
      /*
       * A ConstraintViolationException is thrown either immediately or on save
       * if the new subtree cannot be added to the node at parentAbsPath due to
       * node-type or other implementation-specific constraints. Implementations
       * may differ on when this validation is performed.
       */

    } catch (ItemNotFoundException e) {
      throw new ConstraintViolationException(e);
    } catch (InvalidItemStateException e) {
      throw new ConstraintViolationException(e);
    }

  }

  /**
   * @param parentData
   * @param name
   * @param state
   * @param skipIdentifier
   * @return
   */
  private List<ItemState> getItemStatesList(NodeData parentData,
                                            InternalQName name,
                                            int state,
                                            String skipIdentifier) {
    List<ItemState> states = new ArrayList<ItemState>();
    for (ItemState itemState : changesLog.getAllStates()) {
      ItemData stateData = itemState.getData();
      if (isParent(stateData, parentData) && stateData.getQPath().getName().equals(name)) {
        if ((state != 0) && (state != itemState.getState())
            || stateData.getIdentifier().equals(skipIdentifier)) {
          continue;
        }
        states.add(itemState);

      }
    }
    return states;
  }

  /**
   * @param parentNodeType
   * @param parentMixinNames
   * @param nodeTypeName
   * @return
   * @throws NoSuchNodeTypeException
   * @throws RepositoryException
   */
  protected boolean isChildNodePrimaryTypeAllowed(InternalQName parentNodeType,
                                                  InternalQName[] parentMixinNames,
                                                  String nodeTypeName) throws NoSuchNodeTypeException,
                                                                      RepositoryException {

    List<ExtendedNodeType> parenNt = getAllNodeTypes(parentNodeType, parentMixinNames);

    for (ExtendedNodeType extendedNodeType : parenNt) {
      if (extendedNodeType.isChildNodePrimaryTypeAllowed(nodeTypeName)) {
        return true;
      }
    }

    return false;

  }

  protected InternalQName findNodeType(InternalQName parentNodeType,
                                     InternalQName[] parentMixinNames,
                                     String name) throws RepositoryException,
                                                 ConstraintViolationException {

    List<ExtendedNodeType> nodeTypes = getAllNodeTypes(parentNodeType, parentMixinNames);
    String residualNodeTypeName = null;
    for (ExtendedNodeType extendedNodeType : nodeTypes) {
      NodeDefinition[] nodeDefs = extendedNodeType.getChildNodeDefinitions();
      for (int i = 0; i < nodeDefs.length; i++) {
        NodeDefinition nodeDef = nodeDefs[i];
        if (nodeDef.getName().equals(name)) {
          return locationFactory.parseJCRName(nodeDef.getDefaultPrimaryType().getName())
                                .getInternalName();
        } else if (nodeDef.getName().equals(ExtendedItemDefinition.RESIDUAL_SET)) {
          residualNodeTypeName = nodeDef.getDefaultPrimaryType().getName();
        }
      }
    }

    if (residualNodeTypeName == null)
      throw new ConstraintViolationException("Can not define node type for " + name);
    return locationFactory.parseJCRName(residualNodeTypeName).getInternalName();
  }

  private List<ExtendedNodeType> getAllNodeTypes(InternalQName parentNodeType,
                                                 InternalQName[] parentMixinNames) throws NoSuchNodeTypeException,
                                                                                  RepositoryException {
    List<ExtendedNodeType> parenNt = new ArrayList<ExtendedNodeType>();

    parenNt.add(ntManager.getNodeType(parentNodeType));

    if (parentMixinNames != null) {
      for (int i = 0; i < parentMixinNames.length; i++) {
        parenNt.add(ntManager.getNodeType(parentMixinNames[i]));
      }
    }
    return parenNt;
  }

  /**
   * @param d1 - The first ItemData.
   * @param d2 - The second ItemData.
   * @return True if parent of both ItemData the same.
   */
  private boolean isParent(ItemData data, ItemData parent) {
    String id1 = data.getParentIdentifier();
    String id2 = parent.getIdentifier();
    if (id1 == id2)
      return true;
    if (id1 == null && id2 != null)
      return false;
    return id1.equals(id2);
  }

  /**
   * @param parentData
   * @param name
   * @param skipIdentifier
   * @return
   * @deprecated
   */
  @Deprecated
  protected ItemData getLocalItemData(NodeData parentData, QPathEntry name, String skipIdentifier) {
    ItemData item = null;
    List<ItemState> states = getItemStatesList(parentData, name, 0, skipIdentifier);
    // get last state
    ItemState state = states.get(states.size() - 1);
    if (!state.isDeleted()) {
      item = state.getData();

    }
    return item;
  }

  /**
   * @return
   */
  protected NodeData getParent() {
    return tree.peek();
  }

  /**
   * @param propertyName
   * @param nodeTypes
   * @return
   */
  protected PropertyDefinition getPropertyDefinition(InternalQName propertyName,
                                                     List<ExtendedNodeType> nodeTypes) {
    PropertyDefinition pdResidual = null;
    for (ExtendedNodeType nt : nodeTypes) {
      PropertyDefinitions pds = nt.getPropertyDefinitions(propertyName);
      PropertyDefinition pd = pds.getAnyDefinition();
      if (pd != null) {
        if (((PropertyDefinitionImpl) pd).isResidualSet()) {
          pdResidual = pd;
        } else {
          return pd;
        }
      }
    }
    return pdResidual;
  }

  /**
   * @param name
   * @param nodeTypes
   * @return
   */
  protected boolean isNodeType(InternalQName name, List<ExtendedNodeType> nodeTypes) {
    for (ExtendedNodeType nt : nodeTypes) {
      if (nt.isNodeType(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param path
   * @param state
   * @param itemState
   */
  protected void replaceFirstState(QPath path, int state, ItemState itemState) {
    for (int i = 0; i < changesLog.getAllStates().size(); i++) {
      if (changesLog.getAllStates().get(i).getState() == state
          && changesLog.getAllStates().get(i).getData().getQPath().equals(path)) {
        changesLog.getAllStates().set(i, itemState);
        break;
      }
    }
    throw new IllegalStateException("replace fail");
  }

  /**
   * @param identifier
   * @return
   * @throws RepositoryException
   */
  protected String validateUuidCollision(String identifier) throws RepositoryException {

    NodeData parentNodeData = getParent();
    ItemDataRemoveVisitor visitor = null;
    List<ItemState> removedStates = null;
    if (identifier != null) {
      try {
        ItemImpl sameUuidItem = session.getTransientNodesManager().getItemByIdentifier(identifier,
                                                                                       false); // TODO
        // pool=false
        if (sameUuidItem != null) {
          switch (uuidBehavior) {
          case ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW:
            // Incoming referenceable nodes are assigned newly created UUIDs
            // upon addition to the workspace. As a result UUID collisions
            // never occur.

            // reset UUID and it will be autocreated in session
            identifier = null;
            break;
          case ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING:
            if (!sameUuidItem.isNode()
                || !((NodeImpl) sameUuidItem).isNodeType("mix:referenceable")) {
              throw new RepositoryException("An incoming referenceable node has the same "
                  + "UUID as a identifier of non mix:referenceable"
                  + " node already existing in the workspace!");
            }
            // If an incoming referenceable node has the same UUID as a node
            // already existing in the workspace then the already existing
            // node (and its subtree) is removed from wherever it may be in
            // the workspace before the incoming node is added. Note that this
            // can result in nodes disappearing from locations in the
            // workspace that are remote from the location to which the
            // incoming subtree is being written.

            visitor = new ItemDataRemoveVisitor(session, true);
            sameUuidItem.getData().accept(visitor);
            removedStates = visitor.getRemovedStates();
            changesLog.addAll(removedStates);

            // sameUuidNode = null;
            break;
          case ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING:
            if (!sameUuidItem.isNode()
                || !((NodeImpl) sameUuidItem).isNodeType("mix:referenceable")) {
              throw new RepositoryException("An incoming referenceable node has the same "
                  + "UUID as a identifier of non mix:referenceable"
                  + " node already existing in the workspace!");
            }
            // If an incoming referenceable node has the same UUID as a node
            // already existing in the workspace, then the already existing
            // node is replaced by the incoming node in the same position as
            // the existing node. Note that this may result in the incoming
            // subtree being disaggregated and spread around to different
            // locations in the workspace. In the most extreme case this
            // behavior may result in no node at all being added as child of
            // parentAbsPath. This will occur if the topmost element of the
            // incoming XML has the same UUID as an existing node elsewhere in
            // the workspace.

            // replace in same location
            parentNodeData = (NodeData) ((NodeImpl) sameUuidItem.getParent()).getData();
            visitor = new ItemDataRemoveVisitor(session, true);
            sameUuidItem.getData().accept(visitor);
            removedStates = visitor.getRemovedStates();
            changesLog.addAll(removedStates);
            tree.push(ImportNodeData.createCopy((TransientNodeData) parentNodeData));
            break;
          case ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW:
            // If an incoming referenceable node has the same UUID as a node
            // already existing in the workspace then a SAXException is thrown
            // by the ContentHandler during deserialization.
            throw new ItemExistsException("An incoming referenceable node has the same "
                + "UUID as a node already existing in the workspace!");
          default:
          }
        }
      } catch (ItemNotFoundException e) {
        // node not found, it's ok - willing create one new
      }

    }
    return identifier;
  }

  /**
   * Class helps sort ItemStates list. After sorting the delete states is to be
   * on top of the list
   */
  private class PathSorter implements Comparator<ItemState> {
    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(final ItemState i1, final ItemState i2) {
      int sign = 0;
      if (i1.getState() != i2.getState()) {
        if (i2.isDeleted())
          sign = 1;
        else
          sign = -1;
      }
      return sign;
    }
  }
}
