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
package org.exoplatform.services.jcr.impl.core.nodetype;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.jcr.InvalidItemStateException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlPolicy;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionValue;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValuesList;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.log.ExoLogger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * Created by The eXo Platform SAS. <br/>Date: 26.11.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter
 *         Nedonosko</a>
 * @version $Id: NodeTypeDataManagerImpl.java 111 2008-11-11 11:11:11Z
 *          pnedonosko $
 */
public class NodeTypeDataManagerImpl implements NodeTypeDataManager {

  protected static final Log                                          LOG            = ExoLogger.getLogger("jcr.NodeTypeDataManagerImpl");

  private static final String                                         NODETYPES_FILE = "nodetypes.xml";

  protected final NamespaceRegistry                                   namespaceRegistry;

  protected final NodeTypeDataPersister                               persister;

  protected final LocationFactory                                     locationFactory;

  protected final String                                              accessControlPolicy;

  protected final NodeTypeDataHierarchyHolder                         hierarchy;

  protected final ItemDefinitionDataHolder                            defsHolder;

  /**
   * Listeners (soft references)
   */
  private final Map<NodeTypeManagerListener, NodeTypeManagerListener> listeners;

  public NodeTypeDataManagerImpl(RepositoryEntry config,
                                 LocationFactory locationFactory,
                                 NamespaceRegistry namespaceRegistry,
                                 NodeTypeDataPersister persister) throws RepositoryException {

    this.namespaceRegistry = namespaceRegistry;

    this.persister = persister;

    this.locationFactory = locationFactory;

    this.accessControlPolicy = config.getAccessControl();

    this.hierarchy = new NodeTypeDataHierarchyHolder();

    this.defsHolder = new ItemDefinitionDataHolder(this.hierarchy);
    this.listeners = Collections.synchronizedMap(new WeakHashMap<NodeTypeManagerListener, NodeTypeManagerListener>());
    initDefault();
  }

  private void initDefault() throws RepositoryException {
    long start = System.currentTimeMillis();
    try {
      InputStream xml = NodeTypeManagerImpl.class.getResourceAsStream(NODETYPES_FILE);
      if (xml != null) {
        registerNodeTypes(xml, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
      } else {
        String msg = "Resource file '" + NODETYPES_FILE
            + "' with NodeTypes configuration does not found. Can not create node type manager";
        LOG.error(msg);
        throw new RepositoryException(msg);
      }
    } catch (Exception e) {
      String msg = "Error of initialization default types. Resource file with NodeTypes configuration '"
          + NODETYPES_FILE + "'. " + e;
      LOG.error(msg);
      throw new RepositoryException(msg, e);
    } finally {
      LOG.info("Initialization of default nodetypes done. " + (System.currentTimeMillis() - start)
          + " ms.");
    }
  }

  /**
   * {@inheritDoc}
   */
  public NodeTypeData registerNodeType(NodeTypeValue ntvalue, int alreadyExistsBehaviour) throws RepositoryException {

    if (accessControlPolicy.equals(AccessControlPolicy.DISABLE)) {
      List<String> nsupertypes = ntvalue.getDeclaredSupertypeNames();
      if (nsupertypes != null && nsupertypes.contains("exo:privilegeable")
          || ntvalue.getName().equals("exo:privilegeable")) {
        // skip this node, so it's not necessary at this runtime
        // + "' -- it's not necessary at this runtime";
        LOG.warn("Node type " + ntvalue.getName()
            + " is not register due to DISABLE control policy");
        return null;
      }
    }

    // We have to validate node value before registering it
    ntvalue.validateNodeType();

    // declaring NT name
    InternalQName ntName = locationFactory.parseJCRName(ntvalue.getName()).getInternalName();

    List<String> stlist = ntvalue.getDeclaredSupertypeNames();
    InternalQName[] supertypes = new InternalQName[stlist.size()];
    for (int i = 0; i < stlist.size(); i++) {
      supertypes[i] = locationFactory.parseJCRName(stlist.get(i)).getInternalName();
    }

    List<PropertyDefinitionValue> pdlist = ntvalue.getDeclaredPropertyDefinitionValues();
    PropertyDefinitionData[] props = new PropertyDefinitionData[pdlist.size()];
    for (int i = 0; i < pdlist.size(); i++) {
      PropertyDefinitionValue v = pdlist.get(i);

      PropertyDefinitionData pd = new PropertyDefinitionData(locationFactory.parseJCRName(v.getName())
                                                                            .getInternalName(),
                                                             ntName,
                                                             v.isAutoCreate(),
                                                             v.isMandatory(),
                                                             v.getOnVersion(),
                                                             v.isReadOnly(),
                                                             v.getRequiredType(),
                                                             v.getValueConstraints()
                                                              .toArray(new String[v.getValueConstraints()
                                                                                   .size()]),
                                                             v.getDefaultValueStrings()
                                                              .toArray(new String[v.getDefaultValueStrings()
                                                                                   .size()]),
                                                             v.isMultiple());
      props[i] = pd;
    }

    List<NodeDefinitionValue> ndlist = ntvalue.getDeclaredChildNodeDefinitionValues();
    NodeDefinitionData[] nodes = new NodeDefinitionData[ndlist.size()];
    for (int i = 0; i < ndlist.size(); i++) {
      NodeDefinitionValue v = ndlist.get(i);

      List<String> rnts = v.getRequiredNodeTypeNames();
      InternalQName[] requiredNTs = new InternalQName[rnts.size()];
      for (int ri = 0; i < rnts.size(); ri++) {
        requiredNTs[i] = locationFactory.parseJCRName(rnts.get(ri)).getInternalName();
      }
      NodeDefinitionData nd = new NodeDefinitionData(locationFactory.parseJCRName(v.getName())
                                                                    .getInternalName(),
                                                     ntName,
                                                     v.isAutoCreate(),
                                                     v.isMandatory(),
                                                     v.getOnVersion(),
                                                     v.isReadOnly(),
                                                     requiredNTs,
                                                     locationFactory.parseJCRName(v.getDefaultNodeTypeName())
                                                                    .getInternalName(),
                                                     v.isSameNameSiblings());
      nodes[i] = nd;
    }

    NodeTypeData ntdata = new NodeTypeData(ntName,
                                           locationFactory.parseJCRName(ntvalue.getPrimaryItemName())
                                                          .getInternalName(),
                                           ntvalue.isMixin(),
                                           ntvalue.isOrderableChild(),
                                           supertypes,
                                           props,
                                           nodes);

    registerNodeType(ntdata, alreadyExistsBehaviour);
    
    return ntdata;
  }
  
  public Collection<NodeTypeData> registerNodeTypes(Collection<NodeTypeValue> ntvalues, int alreadyExistsBehaviour) throws RepositoryException {
    // 1. validate collection and self/new referencing, TODO
    
    // 2. traverse and reg
    Collection<NodeTypeData> nts = new ArrayList<NodeTypeData>();
    for (NodeTypeValue v: ntvalues) {
      nts.add(registerNodeType(v, alreadyExistsBehaviour));
    }
    
    return nts;
  }
  
  /**
   * {@inheritDoc}
   */
  public Collection<NodeTypeData> registerNodeTypes(InputStream xml, int alreadyExistsBehaviour) throws RepositoryException {

    try {
      IBindingFactory factory = BindingDirectory.getFactory(NodeTypeValuesList.class);
      IUnmarshallingContext uctx = factory.createUnmarshallingContext();
      NodeTypeValuesList nodeTypeValuesList = (NodeTypeValuesList) uctx.unmarshalDocument(xml, null);
      ArrayList ntvList = nodeTypeValuesList.getNodeTypeValuesList();

      long start = System.currentTimeMillis();
      Collection<NodeTypeData> nts = new ArrayList<NodeTypeData>();
      for (int i = 0; i < ntvList.size(); i++) {
        if (ntvList.get(i) != null) {
          NodeTypeValue nodeTypeValue = (NodeTypeValue) ntvList.get(i);
          nts.add(registerNodeType(nodeTypeValue, alreadyExistsBehaviour));
        } else {
          // Hm! Smth is wrong in xml document
          LOG.error("Empty nodeTypeValue in xml document, index: " + i + ", skiping...");
        }
      }
      LOG.info("Nodetypes registered from xml definitions (count: " + ntvList.size() + "). "
          + (System.currentTimeMillis() - start) + " ms.");
      
      return nts;
    } catch (JiBXException e) {
      throw new RepositoryException("Error in config initialization " + e, e);
    }
  }  

  /**
   * {@inheritDoc}
   */
  public void registerNodeType(NodeTypeData nodeType, int alreadyExistsBehaviour) throws RepositoryException {

    if (nodeType == null) {
      throw new RepositoryException("NodeTypeData object " + nodeType + " is null");
    }

    long start = System.currentTimeMillis();

    if (accessControlPolicy.equals(AccessControlPolicy.DISABLE)
        && nodeType.getName().equals("exo:privilegeable")) {
      throw new RepositoryException("NodeType exo:privilegeable is DISABLED");
    }

    InternalQName qname = nodeType.getName();
    if (qname == null) {
      throw new RepositoryException("NodeType implementation class "
          + nodeType.getClass().getName() + " is not supported in this method");
    }

    if (findNodeType(qname) != null) {
      if (alreadyExistsBehaviour == ExtendedNodeTypeManager.FAIL_IF_EXISTS) {
        throw new RepositoryException("NodeType " + nodeType.getName() + " is already registered");
      } else
        LOG.warn("NodeType " + nodeType.getName() + " is already registered");
      return;
    }

    hierarchy.addNodeType(nodeType);

    if (persister.isInitialized()) {
      try {
        if (!persister.hasNodeTypeData(nodeType.getName())) {
          persister.addNodeType(nodeType);
          persister.saveChanges();
        }
      } catch (InvalidItemStateException e) {
        LOG.warn("Error of storing node type " + nodeType.getName()
            + ". May be node type already registered .", e);
      }
      if (LOG.isDebugEnabled())
        LOG.debug("NodeType " + nodeType.getName() + " initialized. "
            + (System.currentTimeMillis() - start) + " ms");
    } else {
      if (LOG.isDebugEnabled())
        LOG.debug("NodeType " + nodeType.getName()
            + " registered but not initialized (storage is not initialized). "
            + (System.currentTimeMillis() - start) + " ms");
    }
  }

  public void registerNodeTypes(Collection<NodeTypeData> nodeTypes, int alreadyExistsBehaviour) throws RepositoryException {
    // 1. validate collection and self/new referencing, TODO
    
    // 2. traverse and reg
    for (NodeTypeData nt: nodeTypes) {
      registerNodeType(nt, alreadyExistsBehaviour);
    }
  }
  
  /**
   * Validate NodeTypeData and return new instance or throw an exception. The
   * new instance will be a guarany of valid NodeType. Check according the
   * JSR-170/JSR-283 spec.
   * 
   * @param nodeType NodeTypeData to be checked
   * @return valid NodeTypeData
   * @throws RepositoryException
   */
  protected NodeTypeData validateNodeType(NodeTypeData nodeType) throws RepositoryException {
    // TODO possible remove
    InternalQName name = nodeType.getName();
    InternalQName primaryItemName = nodeType.getPrimaryItemName();
    boolean mixin = nodeType.isMixin();
    boolean hasOrderableChildNodes = nodeType.hasOrderableChildNodes();
    InternalQName[] declaredSupertypeNames = nodeType.getDeclaredSupertypeNames();
    PropertyDefinitionData[] declaredPropertyDefinitions = nodeType.getDeclaredPropertyDefinitions();
    NodeDefinitionData[] declaredChildNodeDefinitions = nodeType.getDeclaredChildNodeDefinitions();

    if (nodeType == null) {
      throw new RepositoryException("NodeType object " + nodeType + " is null");
    }

    long start = System.currentTimeMillis();

    if (accessControlPolicy.equals(AccessControlPolicy.DISABLE)
        && nodeType.getName().equals("exo:privilegeable")) {
      throw new RepositoryException("NodeType exo:privilegeable is DISABLED");
    }
    for (int i = 0; i < declaredSupertypeNames.length; i++) {
      if (findNodeType(declaredSupertypeNames[i]) == null) {
        throw new RepositoryException("Super type " + declaredSupertypeNames[i].getAsString()
            + " not registred");
      }
    }
    for (int i = 0; i < declaredPropertyDefinitions.length; i++) {
      if (declaredPropertyDefinitions[i].getDeclaringNodeType().equals(name)) {
        throw new RepositoryException("Invalid declared  node type in property definitions with name "
            + declaredPropertyDefinitions[i].getName().getAsString() + " not registred");
      }
    }
    for (int i = 0; i < declaredChildNodeDefinitions.length; i++) {
      if (declaredChildNodeDefinitions[i].getDeclaringNodeType().equals(name)) {
        throw new RepositoryException("Invalid declared  node type in child node definitions with name "
            + declaredChildNodeDefinitions[i].getName().getAsString() + " not registred");
      }
      if (findNodeType(declaredChildNodeDefinitions[i].getDefaultPrimaryType()) == null) {
        throw new RepositoryException("Default primary type"
            + declaredSupertypeNames[i].getAsString() + " not registred");
      }
      for (int j = 0; j < declaredChildNodeDefinitions[i].getRequiredPrimaryTypes().length; j++) {
        if (findNodeType(declaredChildNodeDefinitions[i].getRequiredPrimaryTypes()[j]) == null) {
          throw new RepositoryException("Required primary type"
              + declaredSupertypeNames[i].getAsString() + " not registred");
        }

      }
    }

    if (name == null) {
      throw new RepositoryException("NodeType implementation class "
          + nodeType.getClass().getName() + " is not supported in this method");
    }

    return new NodeTypeData(name,
                            primaryItemName,
                            mixin,
                            hasOrderableChildNodes,
                            declaredSupertypeNames,
                            declaredPropertyDefinitions,
                            declaredChildNodeDefinitions);
  }

  // impl

  public NodeDefinitionData getChildNodeDefinition(InternalQName nodeName,
                                                   InternalQName nodeTypeName,
                                                   InternalQName parentTypeName) {

    // TODO residual
    return defsHolder.getChildNodeDefinition(parentTypeName, nodeName, nodeTypeName);
  }

  public NodeDefinitionData findChildNodeDefinition(InternalQName nodeName,
                                                    InternalQName... nodeTypeNames) {

    // TODO residual
    return defsHolder.getDefaultChildNodeDefinition(nodeName, nodeTypeNames);
  }

  public NodeTypeData findNodeType(InternalQName typeName) {
    return hierarchy.getNodeType(typeName);
  }
  
  public PropertyDefinitionDatas getPropertyDefinitions(InternalQName propertyName,
                                                         InternalQName... nodeTypeNames) {

    // TODO residual
    return defsHolder.getPropertyDefinitions(propertyName, nodeTypeNames);
  }
  
  public PropertyDefinitionDatas findPropertyDefinitions(InternalQName propertyName,
                                                         InternalQName primaryNodeType,
                                                         InternalQName... mixinTypes) {
    
    if (mixinTypes != null) {
      InternalQName[] nts = new InternalQName[mixinTypes.length + 1];
      nts[0] = primaryNodeType;
      for (int i=0; i<mixinTypes.length; i++) {
        nts[i + 1] = mixinTypes[i];  
      }
      return getPropertyDefinitions(propertyName, nts);
    } else
      return getPropertyDefinitions(propertyName, primaryNodeType);
  }


  public Collection<NodeTypeData> getAllNodeTypes() {
    return hierarchy.getAllNodeTypes();
  }

  public boolean isNodeType(InternalQName testTypeName, InternalQName... typesNames) {
    return hierarchy.isNodeType(testTypeName, typesNames);
  }

  public boolean isOrderableChildNodesSupported(InternalQName... nodeTypeNames) {

    for (InternalQName name : nodeTypeNames) {
      NodeTypeData nt = hierarchy.getNodeType(name);

      if (nt != null && nt.hasOrderableChildNodes())
        return true;

      Set<InternalQName> supers = hierarchy.getSupertypes(nt.getName());
      for (InternalQName suName : supers) {
        NodeTypeData su = hierarchy.getNodeType(suName);
        if (su != null && su.hasOrderableChildNodes())
          return true;
      }
    }

    return false;
  }

  /**
   * Add a <code>NodeTypeRegistryListener</code>
   * 
   * @param listener the new listener to be informed on (un)registration of node
   *          types
   */
  public void addListener(NodeTypeManagerListener listener) {
    if (!listeners.containsKey(listener)) {
      listeners.put(listener, listener);
    }
  }

  /**
   * Remove a <code>NodeTypeRegistryListener</code>.
   * 
   * @param listener an existing listener
   */
  public void removeListener(NodeTypeManagerListener listener) {
    listeners.remove(listener);
  }

  /**
   * Notify the listeners that a node type <code>ntName</code> has been
   * registered.
   * 
   * @param ntName NT name.
   */
  private void notifyRegistered(InternalQName ntName) {
    // copy listeners to array to avoid ConcurrentModificationException
    NodeTypeManagerListener[] la = listeners.values()
                                            .toArray(new NodeTypeManagerListener[listeners.size()]);
    for (int i = 0; i < la.length; i++) {
      if (la[i] != null) {
        la[i].nodeTypeRegistered(ntName);
      }
    }
  }

  /**
   * Notify the listeners that a node type <code>ntName</code> has been
   * re-registered.
   * 
   * @param ntName NT name.
   */
  private void notifyReRegistered(InternalQName ntName) {
    // copy listeners to array to avoid ConcurrentModificationException
    NodeTypeManagerListener[] la = listeners.values()
                                            .toArray(new NodeTypeManagerListener[listeners.size()]);
    for (int i = 0; i < la.length; i++) {
      if (la[i] != null) {
        la[i].nodeTypeReRegistered(ntName);
      }
    }
  }

  /**
   * Notify the listeners that a node type <code>ntName</code> has been
   * unregistered.
   * 
   * @param ntName NT name.
   */
  private void notifyUnregistered(InternalQName ntName) {
    // copy listeners to array to avoid ConcurrentModificationException
    NodeTypeManagerListener[] la = listeners.values()
                                            .toArray(new NodeTypeManagerListener[listeners.size()]);
    for (int i = 0; i < la.length; i++) {
      if (la[i] != null) {
        la[i].nodeTypeUnregistered(ntName);
      }
    }
  }
}
