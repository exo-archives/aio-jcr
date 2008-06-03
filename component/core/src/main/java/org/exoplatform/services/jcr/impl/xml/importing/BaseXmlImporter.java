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
import java.util.Map;
import java.util.Stack;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.core.nodetype.ExtendedItemDefinition;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeDefinitionImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataRemoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.xml.importing.dataflow.ImportItemData;
import org.exoplatform.services.jcr.impl.xml.importing.dataflow.ImportNodeData;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: BaseXmlImporter.java 14221 2008-05-14 08:27:41Z ksm $
 */
public abstract class BaseXmlImporter implements ContentImporter {

  protected final AccessManager       accessManager;

  protected QPath                     ancestorToSave;

  protected final PlainChangesLogImpl changesLog;

  protected final Map<String, Object> context;

  protected final ItemDataConsumer    dataConsumer;

  protected boolean                   isNeedReloadAncestorToSave = false;

  protected final LocationFactory     locationFactory;

  protected final NamespaceRegistry   namespaceRegistry;

  //protected final ExtendedNodeTypeManager ntManager; // TODO use interface
  protected final NodeTypeManagerImpl ntManager;

  protected final Stack<NodeData>     tree                       = new Stack<NodeData>();

  protected final ConversationState userState;

  protected final int                 uuidBehavior;

  protected final ValueFactoryImpl    valueFactory;

  private final Log                   log                        = ExoLogger.getLogger("jcr.ImporterBase");

  public BaseXmlImporter(NodeData parent,
                         QPath ancestorToSave,
                         int uuidBehavior,
                         ItemDataConsumer dataConsumer,
                         NodeTypeManagerImpl ntManager,
                         LocationFactory locationFactory,
                         ValueFactoryImpl valueFactory,
                         NamespaceRegistry namespaceRegistry,
                         AccessManager accessManager,
                         ConversationState userState,
                         Map<String, Object> context) {

    this.dataConsumer = dataConsumer;
    this.valueFactory = valueFactory;
    this.namespaceRegistry = namespaceRegistry;
    this.accessManager = accessManager;
    this.userState = userState;

    this.context = context;

    this.ntManager = ntManager;
    this.locationFactory = locationFactory;
    this.uuidBehavior = uuidBehavior;
    this.tree.push(parent);
    this.changesLog = new PlainChangesLogImpl();
    this.ancestorToSave = ancestorToSave;
  }

  /**
   * @return
   */
  public QPath getAncestorToSave() {
    return ancestorToSave;
  }

  public PlainChangesLog getChanges() {
    Collections.sort(changesLog.getAllStates(), new PathSorter());

    if (log.isDebugEnabled()) {
      String str = "";
      for (int i = 0; i < changesLog.getAllStates().size(); i++)
        str += " " + ItemState.nameFromValue(changesLog.getAllStates().get(i).getState()) + "\t\t"
            + changesLog.getAllStates().get(i).getData().getIdentifier() + "\t" + "isPersisted="
            + changesLog.getAllStates().get(i).isPersisted() + "\t" + "isEventFire="
            + changesLog.getAllStates().get(i).isEventFire() + "\t" + "isInternallyCreated="
            + changesLog.getAllStates().get(i).isInternallyCreated() + "\t"
            + changesLog.getAllStates().get(i).getData().getQPath().getAsString() + "\n";
      log.debug(str);
    }
    if (isNeedReloadAncestorToSave) {
      PlainChangesLogImpl newChangesLog = new PlainChangesLogImpl();
      for (ItemState itemState : changesLog.getAllStates()) {
        newChangesLog.add(new ItemState(itemState.getData(),
                                        itemState.getState(),
                                        itemState.isEventFire(),
                                        ancestorToSave,
                                        itemState.isInternallyCreated(),
                                        itemState.isPersisted()));
      }
      changesLog.clear();
      changesLog.addAll(newChangesLog.getAllStates());
    }
    return changesLog;
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

    NodeDefinitionImpl nodedef = ntManager.findNodeDefinition(name,
                                                              parentData.getPrimaryTypeName(),
                                                              parentData.getMixinTypeNames());

    ItemData sameNameNode = null;
    try {
      sameNameNode = dataConsumer.getItemData(parentData, new QPathEntry(name, 0));
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

    List<NodeData> existedChilds = dataConsumer.getChildNodesData(parentData);

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

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.impl.xml.importing.Importer#registerNamespace(java.lang.String,
   *      java.lang.String)
   */
  public void registerNamespace(String prefix, String uri) {
    try {
      namespaceRegistry.getPrefix(uri);
    } catch (NamespaceException e) {
      try {
        namespaceRegistry.registerNamespace(prefix, uri);
      } catch (NamespaceException e1) {
        throw new RuntimeException(e1);
      } catch (RepositoryException e1) {
        throw new RuntimeException(e1);
      }
    } catch (RepositoryException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * @param ancestorToSave
   */
  public void setAncestorToSave(QPath newAncestorToSave) {
    if (!ancestorToSave.equals(newAncestorToSave)) {
      isNeedReloadAncestorToSave = true;
    }
    this.ancestorToSave = newAncestorToSave;
  }

  /**
   * @param parentNodeType
   * @param parentMixinNames
   * @param name
   * @return
   * @throws RepositoryException
   * @throws ConstraintViolationException
   */
  protected InternalQName findNodeType(InternalQName parentNodeType,
                                       InternalQName[] parentMixinNames,
                                       String name) throws RepositoryException,
                                                   ConstraintViolationException {

    List<ExtendedNodeType> nodeTypes = ntManager.getNodeTypes(parentNodeType, parentMixinNames);
    String residualNodeTypeName = null;
    for (ExtendedNodeType extendedNodeType : nodeTypes) {
      NodeDefinition[] nodeDefs = extendedNodeType.getChildNodeDefinitions();
      for (int i = 0; i < nodeDefs.length; i++) {
        NodeDefinition nodeDef = nodeDefs[i];
        if (nodeDef.getName().equals(name)) {
          if (nodeDef.getDefaultPrimaryType() != null)
            return locationFactory.parseJCRName(nodeDef.getDefaultPrimaryType().getName())
                                  .getInternalName();
        } else if (nodeDef.getName().equals(ExtendedItemDefinition.RESIDUAL_SET)) {
          if (nodeDef.getDefaultPrimaryType() != null)
            residualNodeTypeName = nodeDef.getDefaultPrimaryType().getName();
        }
      }
    }

    if (residualNodeTypeName == null)
      throw new ConstraintViolationException("Can not define node-type for node " + name
          + ", parent node type " + parentNodeType.getAsString());
    return locationFactory.parseJCRName(residualNodeTypeName).getInternalName();
  }

  /**
   * @return
   */
  protected NodeData getParent() {
    return tree.peek();
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

    List<ExtendedNodeType> parenNt = ntManager.getNodeTypes(parentNodeType, parentMixinNames);

    for (ExtendedNodeType extendedNodeType : parenNt) {
      if (extendedNodeType.isChildNodePrimaryTypeAllowed(nodeTypeName)) {
        return true;
      }
    }

    return false;

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
   * @param currentNodeInfo
   * @param identifier
   * @throws PathNotFoundException
   * @throws IllegalPathException
   * @throws RepositoryException
   */
  protected void reloadChangesInfoAfterUC(ImportNodeData currentNodeInfo, String identifier) throws PathNotFoundException,
                                                                                            IllegalPathException,
                                                                                            RepositoryException {
    boolean reloadSNS = uuidBehavior == ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING
        || uuidBehavior == ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING;
    QPath newPath = null;
    if (reloadSNS) {
      NodeData currentParentData = getParent();
      // current node already in list
      int nodeIndex = getNodeIndex(currentParentData,
                                   currentNodeInfo.getQName(),
                                   currentNodeInfo.getIdentifier());
      newPath = QPath.makeChildPath(currentParentData.getQPath(),
                                    currentNodeInfo.getQName(),
                                    nodeIndex);
      currentNodeInfo.setQPath(newPath);
    }

    String oldIdentifer = currentNodeInfo.getIdentifier();
    // update parentIdentifer
    for (ItemState state : changesLog.getAllStates()) {
      ItemData data = state.getData();
      if (data.getParentIdentifier().equals(oldIdentifer)) {
        ((ImportItemData) data).setParentIdentifer(identifier);
        if (reloadSNS)
          ((ImportItemData) data).setQPath(QPath.makeChildPath(newPath, data.getQPath().getName()));

      }

    }

    currentNodeInfo.setIdentifier(identifier);
  }

  /**
   * @param identifier
   * @return
   * @throws RepositoryException
   */
  protected String validateUuidCollision(final String identifier) throws RepositoryException {
    String newIdentifer = identifier;
    if (identifier != null) {
      try {
        NodeData sameUuidItem = (NodeData) dataConsumer.getItemData(identifier);
        if (sameUuidItem != null) {
          switch (uuidBehavior) {
          case ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW:
            // Incoming referenceable nodes are assigned newly created UUIDs
            // upon addition to the workspace. As a result UUID collisions
            // never occur.

            // reset UUID and it will be autocreated in session
            newIdentifer = null;
            break;
          case ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING:
            removeExisted(sameUuidItem);
            break;
          case ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING:
            removeExisted(sameUuidItem);
            ItemData parentOfsameUuidItem = dataConsumer.getItemData(sameUuidItem.getParentIdentifier());
            tree.push(ImportNodeData.createCopy((TransientNodeData) parentOfsameUuidItem));
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
    return newIdentifer;
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

  private void removeExisted(NodeData sameUuidItem) throws RepositoryException,
                                                   ConstraintViolationException,
                                                   PathNotFoundException {

    if (!ntManager.isNodeType(Constants.MIX_REFERENCEABLE,
                              sameUuidItem.getPrimaryTypeName(),
                              sameUuidItem.getMixinTypeNames())) {
      throw new RepositoryException("An incoming referenceable node has the same "
          + " UUID as a identifier of non mix:referenceable"
          + " node already existing in the workspace!");
    }

    // If an incoming referenceable node has the same UUID as a node
    // already existing in the workspace then the already existing
    // node (and its subtree) is removed from wherever it may be in
    // the workspace before the incoming node is added. Note that this
    // can result in nodes disappearing from locations in the
    // workspace that are remote from the location to which the
    // incoming subtree is being written.
    // parentNodeData = (NodeData) sameUuidItem.getParent().getData();

    QPath sameUuidPath = sameUuidItem.getQPath();

    if (ancestorToSave.equals(sameUuidPath) || ancestorToSave.isDescendantOf(sameUuidPath, false)) {
      throw new ConstraintViolationException("The imported document contains a element"
          + " with jcr:uuid attribute the same as the  parent of the import target.");
    }

    setAncestorToSave(QPath.getCommonAncestorPath(ancestorToSave, sameUuidPath));

    ItemDataRemoveVisitor visitor = new ItemDataRemoveVisitor(dataConsumer,
                                                              getAncestorToSave(),
                                                              ntManager,
                                                              accessManager,
                                                              userState);
    sameUuidItem.accept(visitor);

    changesLog.addAll(visitor.getRemovedStates());
  }

  /**
   * Class helps sort ItemStates list. After sorting the delete states has to be
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
