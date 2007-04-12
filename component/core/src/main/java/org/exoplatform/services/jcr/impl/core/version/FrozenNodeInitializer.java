/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.version;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.OnParentVersionAction;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.core.value.BaseValue;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.util.UUIDGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 *
 * @author Gennady Azarenkov
 * @version $Id: FrozenNodeInitializer.java 13421 2007-03-15 10:46:47Z geaz $
 */

public class FrozenNodeInitializer extends ItemDataTraversingVisitor {

  private static Log log = ExoLogger.getLogger("jcr.FrozenNodeInitializer");

  private final Stack<NodeData> contextNodes;
  private final NodeTypeManagerImpl ntManager;
  private final PlainChangesLog changesLog;

  private final SessionDataManager dataManager;

  public FrozenNodeInitializer(NodeData frozen,
      SessionDataManager dataManager,
      NodeTypeManagerImpl ntManager,
      PlainChangesLog changesLog) throws RepositoryException {
    super(dataManager);
    this.dataManager = dataManager;
    this.ntManager = ntManager;
    this.changesLog = changesLog;
    this.contextNodes = new Stack<NodeData>();
    this.contextNodes.push(frozen);
  }

  protected void entering(PropertyData property, int level) throws RepositoryException {

    if (log.isDebugEnabled()) 
      log.debug("Entering property " + property.getQPath().getAsString());

    if (currentNode() == null)
      // skip if no parent - parent is COMPUTE, INITIALIZE
      return;

    PropertyData frozenProperty = null;
    InternalQName qname = property.getQPath().getName();
    //List <ValueData> values = property.getValues();

    List <ValueData> values = new ArrayList<ValueData>();
    for (ValueData valueData: property.getValues()) {
      values.add(((TransientValueData) valueData).createTransientCopy());
    }

    boolean mv = property.isMultiValued();

    if (qname.equals(Constants.JCR_PRIMARYTYPE) && level == 1) {
      frozenProperty = TransientPropertyData.createPropertyData(
          currentNode(), Constants.JCR_FROZENPRIMARYTYPE,
          PropertyType.NAME, mv, values);
    } else if (qname.equals(Constants.JCR_UUID) && level == 1) {
      frozenProperty = TransientPropertyData.createPropertyData(
          currentNode(), Constants.JCR_FROZENUUID,
          PropertyType.STRING, mv, values);
    } else if (qname.equals(Constants.JCR_MIXINTYPES) && level == 1) {
      frozenProperty = TransientPropertyData.createPropertyData(
          currentNode(), Constants.JCR_FROZENMIXINTYPES,
          PropertyType.STRING, mv, values);
    } else {
      NodeData parent = (NodeData) dataManager.getItemData(property.getParentUUID());

//      PropertyDefinition pdef = ntManager.findPropertyDefinitions(
//          qname,
//          parent.getPrimaryTypeName(),
//          parent.getMixinTypeNames()).getAnyDefinition();
      PropertyDefinition pdef = ntManager.findPropertyDefinition(
          qname,
          parent.getPrimaryTypeName(),
          parent.getMixinTypeNames());

      int action = pdef.getOnParentVersion();

      if (action == OnParentVersionAction.IGNORE) {
        return;
      } else if (action == OnParentVersionAction.ABORT) {
        throw new VersionException("Property is aborted " + property.getQPath().getAsString());
      } else if (action == OnParentVersionAction.COPY || action == OnParentVersionAction.VERSION
          || action == OnParentVersionAction.COMPUTE) {
        frozenProperty = TransientPropertyData.createPropertyData(
            currentNode(), qname,
            property.getType(), mv, values);
      } else if (action == OnParentVersionAction.INITIALIZE) {
        // 8.2.11.3 INITIALIZE
        // On checkin of N, a new P will be created and placed in version
        // storage as a child of VN. The new P will be initialized just as it would
        // be if created normally in a workspace
        if (pdef.isAutoCreated()) {
          if (pdef.getDefaultValues() != null && pdef.getDefaultValues().length>0) {
            // to use default values
            values.clear();
//            try {
              for (Value defValue: pdef.getDefaultValues()) {
                TransientValueData defData = ((BaseValue) defValue).getInternalData();
                values.add(defData.createTransientCopy());
//                if (defData.isByteArray()) {
//                  values.add(new TransientValueData(defData.getAsByteArray(), defData.getOrderNumber()));
//                } else {
//                  values.add(new TransientValueData(defData.getAsStream(), defData.getOrderNumber()));
//                }
              }
//            } catch (IOException e) {
//              throw new RepositoryException("Proeprty default value data read error " + e, e);
//            }
          } else if (ntManager.isNodeType(
              Constants.NT_HIERARCHYNODE, parent.getPrimaryTypeName(), parent.getMixinTypeNames())
              &&
              qname.equals(Constants.JCR_CREATED)) {
            // custom logic for nt:hierarchyNode jcr:created
            values.clear();
            values.add(new TransientValueData(
                dataManager.getTransactManager().getStorageDataManager().getCurrentTime()));
          }
        } // else... just as it would be if created normally in a workspace (sure with value data)
        frozenProperty = TransientPropertyData.createPropertyData(
            currentNode(), qname,
            property.getType(), mv, values);
      } else
        throw new RepositoryException("Unknown OnParentVersion value " + action);
    }
    changesLog.add(ItemState.createAddedState(frozenProperty));
  }

  protected void entering(NodeData node, int level) throws RepositoryException {

    // this node is not taken in account
    if (level == 0) {  
      if (log.isDebugEnabled())
        log.debug("Entering node " + node.getQPath().getAsString() + ", level=0");
      return;
    }

    // ignored parent
    if (currentNode() == null) {
      contextNodes.push(null);
      if (log.isDebugEnabled())
        log.debug("Entering node " + node.getQPath().getAsString() + ", HAS NULL PARENT");
      return;
    }

    InternalQName qname = node.getQPath().getName();

    NodeData parent = (NodeData) dataManager.getItemData(node.getParentUUID());
    NodeDefinition pdef = ntManager.findNodeDefinition(qname, parent.getPrimaryTypeName(), parent.getMixinTypeNames());
    int action = pdef.getOnParentVersion();

    if (log.isDebugEnabled())
      log.debug("Entering node " + node.getQPath().getAsString() + ", " + OnParentVersionAction.nameFromValue(action));

    NodeData frozenNode = null; // (NodeImpl) node;
    if (action == OnParentVersionAction.IGNORE) {
      contextNodes.push(null);
    } else if (action == OnParentVersionAction.ABORT) {
      throw new VersionException("Node is aborted " + node.getQPath().getAsString());
    } else if (action == OnParentVersionAction.COPY) {
      //frozenNode = TransientNodeData.createNodeData(currentNode(),
      //    qname, node.getPrimaryTypeName(), node.getQPath().getIndex());

      QPath frozenPath = QPath.makeChildPath(currentNode().getQPath(), qname, node.getQPath().getIndex());
      frozenNode = new TransientNodeData(frozenPath,
          UUIDGenerator.generate(),
          node.getPersistedVersion(),
          node.getPrimaryTypeName(),
          node.getMixinTypeNames(),
          node.getOrderNumber(),
          currentNode().getUUID(), // parent
          node.getACL());

      contextNodes.push(frozenNode);
      changesLog.add(ItemState.createAddedState(frozenNode));
    } else if (action == OnParentVersionAction.VERSION) {
      if (ntManager.isNodeType(Constants.MIX_VERSIONABLE, node.getPrimaryTypeName(), node.getMixinTypeNames())) {
        frozenNode = TransientNodeData.createNodeData(currentNode(),
            qname,
            Constants.NT_VERSIONEDCHILD,
            node.getQPath().getIndex());

        PropertyData pt = TransientPropertyData.createPropertyData(frozenNode,
            Constants.JCR_PRIMARYTYPE,
            PropertyType.NAME,
            false,
            new TransientValueData(Constants.NT_VERSIONEDCHILD));

        // QPath versionHistoryPath = QPath.makeChildPath(node.getQPath(),
        // Constants.JCR_VERSIONHISTORY);
        // ValueData vh =
        // ((PropertyData)dataManager.getItemData(versionHistoryPath)).getValues().get(0);
        
        ValueData vh = ((PropertyData) dataManager.getItemData(node,
            new QPathEntry(Constants.JCR_VERSIONHISTORY, 0))).getValues().get(0);

        PropertyData pd = TransientPropertyData.createPropertyData(frozenNode,
            Constants.JCR_CHILDVERSIONHISTORY,
            PropertyType.REFERENCE,
            false,
            vh);

        contextNodes.push(null);
        changesLog.add(ItemState.createAddedState(frozenNode));
        changesLog.add(ItemState.createAddedState(pt));
        changesLog.add(ItemState.createAddedState(pd));
      } else { // behaviour of COPY
        //frozenNode = TransientNodeData.createNodeData(currentNode(),
        //    qname, node.getPrimaryTypeName(), node.getQPath().getIndex());

        QPath frozenPath = QPath.makeChildPath(currentNode().getQPath(), qname, node.getQPath().getIndex());
        frozenNode = new TransientNodeData(frozenPath,
            UUIDGenerator.generate(),
            node.getPersistedVersion(),
            node.getPrimaryTypeName(),
            node.getMixinTypeNames(),
            node.getOrderNumber(),
            currentNode().getUUID(), // parent
            node.getACL());

        contextNodes.push(frozenNode);
        changesLog.add(ItemState.createAddedState(frozenNode));
      }

    } else if (action == OnParentVersionAction.INITIALIZE) {
      // 8.2.11.3 INITIALIZE
      // On checkin of N, a new node C will be created and placed in version
      // storage as a child of VN. This new C will be initialized just as it
      // would be if created normally in a workspace. No state information
      // of the current C in the workspace is preserved.
      frozenNode = TransientNodeData.createNodeData(currentNode(),
          qname, node.getPrimaryTypeName(), node.getQPath().getIndex());
      contextNodes.push(null);
      changesLog.add(ItemState.createAddedState(frozenNode));
    } else if (action == OnParentVersionAction.COMPUTE) {
      // 8.2.11.4 COMPUTE
      // On checkin of N, a new node C will be created and placed in version
      // storage as a child of VN. This new C will be initialized by some
      // procedure defined for that type of child node.
      // [PN] 10.04.06 Creatimg simply an new node with same name and same node type
      frozenNode = TransientNodeData.createNodeData(currentNode(),
          qname, node.getPrimaryTypeName(), node.getQPath().getIndex());
      contextNodes.push(null);
      changesLog.add(ItemState.createAddedState(frozenNode));
    } else {
      throw new RepositoryException("Unknown onParentVersion type " + action);
    }
  }

//  private NodeImpl createChildNode(NodeData nodeParent, NodeData nodeData) throws RepositoryException {
//
//    //boolean isReferenceable = node.isNodeType("mix:referenceable");
////    TransientNodeData nodeData = (TransientNodeData) node.getData();
//    InternalQName nodeName = nodeData.getQPath().getName();
//    InternalQName ptName = nodeData.getPrimaryTypeName();
//    InternalQName[] mtNames = nodeData.getMixinTypeNames();
//    //String uuid = isReferenceable ? node.getUUID() : null;
//
////    toLog("====== FrozenInit createChildNode() " + nodeParent.getSession().getWorkspace().getName()
////       + " destParent: " + nodeParent.getPath()
////       + " src:" + node.getPath() + " name:" + nodeName.getAsString());
//
//    NodeImpl newNode = nodeParent.createChildNodeInmemory(nodeName, ptName, mtNames,
//        null, node.getIndex(), false, false, true);
//    return newNode;
//  }

//  private NodeImpl createChildNode(NodeImpl nodeParent, String name,
//      String primaryTypeName) throws RepositoryException {
//
//    SessionImpl nodeSession = (SessionImpl) nodeParent.getSession();
//    InternalQName nodeName = nodeSession.getLocationFactory().parseJCRName(name).getInternalName();
//    InternalQName ptName = nodeSession.getLocationFactory().parseJCRName(primaryTypeName).getInternalName();
//
////    toLog("====== FrozenInit createChildNode() NEW " + nodeParent.getSession().getWorkspace().getName()
////        + " destParent: " + nodeParent.getPath()
////        + " src:" + nodeParent.getPath() + " name:" + nodeName.getAsString());
//
//    NodeImpl newNode = nodeParent.createChildNodeInmemory(nodeName, ptName, null,
//        null, -1, false, false, true);
//    return newNode;
//  }

  protected void leaving(PropertyData property, int level) throws RepositoryException {
  }

  protected void leaving(NodeData node, int level) throws RepositoryException {
    contextNodes.pop();
  }

  private NodeData currentNode() {
    return contextNodes.peek();
  }

  public SessionDataManager getDataManager() {
    return dataManager;
  }

}