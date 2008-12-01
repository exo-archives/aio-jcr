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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.InvalidItemStateException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

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
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 26.11.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: NodeTypeDataManagerImpl.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class NodeTypeDataManagerImpl implements NodeTypeDataManager {

  protected static final Log                       LOG            = ExoLogger.getLogger("jcr.NodeTypeDataManagerImpl");

  private static final String                      NODETYPES_FILE = "nodetypes.xml";

  protected final NamespaceRegistry                namespaceRegistry;

  protected final NodeTypeDataPersister2            persister;

  protected final LocationFactory                  locationFactory;

  protected final String                           accessControlPolicy;

  protected final Map<InternalQName, NodeTypeData> nodeTypes = new ConcurrentHashMap<InternalQName, NodeTypeData>();
  
  protected final ItemDefinitionDataHolder defsHolder;

  public NodeTypeDataManagerImpl(RepositoryEntry config,
                                 LocationFactory locationFactory,
                                 NamespaceRegistry namespaceRegistry,
                                 NodeTypeDataPersister2 persister) throws RepositoryException {

    this.namespaceRegistry = namespaceRegistry;
    this.persister = persister;

    this.locationFactory = locationFactory;

    this.accessControlPolicy = config.getAccessControl();

    this.defsHolder = new ItemDefinitionDataHolder(new NodeTypeDataHierarchyHolder());
    
    initDefault();
  }

  private void initDefault() throws RepositoryException {
    long start = System.currentTimeMillis();
    try {
      InputStream xml = NodeTypeManagerImpl2.class.getResourceAsStream(NODETYPES_FILE);
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
  public void registerNodeTypes(InputStream xml, int alreadyExistsBehaviour) throws RepositoryException {

    try {
      IBindingFactory factory = BindingDirectory.getFactory(NodeTypeValuesList.class);
      IUnmarshallingContext uctx = factory.createUnmarshallingContext();
      NodeTypeValuesList nodeTypeValuesList = (NodeTypeValuesList) uctx.unmarshalDocument(xml, null);
      ArrayList ntvList = nodeTypeValuesList.getNodeTypeValuesList();

      long start = System.currentTimeMillis();
      for (int i = 0; i < ntvList.size(); i++) {
        if (ntvList.get(i) != null) {
          NodeTypeValue nodeTypeValue = (NodeTypeValue) ntvList.get(i);
          registerNodeType(nodeTypeValue, alreadyExistsBehaviour);
        } else {
          // Hm! Smth is wrong in xml document
          LOG.error("Empty nodeTypeValue in xml document, index: " + i + ", skiping...");
        }
      }
      LOG.info("Nodetypes registered from xml definitions (count: " + ntvList.size() + "). "
          + (System.currentTimeMillis() - start) + " ms.");
    } catch (JiBXException e) {
      throw new RepositoryException("Error in config initialization " + e, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void registerNodeType(NodeTypeValue ntvalue, int alreadyExistsBehaviour) throws RepositoryException {

    if (accessControlPolicy.equals(AccessControlPolicy.DISABLE)) {
      List<String> nsupertypes = ntvalue.getDeclaredSupertypeNames();
      if (nsupertypes != null && nsupertypes.contains("exo:privilegeable")
          || ntvalue.getName().equals("exo:privilegeable")) {
        // skip this node, so it's not necessary at this runtime
        // + "' -- it's not necessary at this runtime";
        LOG.warn("Node type " + ntvalue.getName()
            + " is not register due to DISABLE control policy");
        return;
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

    nodeTypes.put(nodeType.getName(), nodeType);

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

  /**
   * 
   * Validate NodeTypeData and return new instance or throw an exception.
   * The new instance will be a guarany of valid NodeType.
   * 
   * Check according the JSR-170/JSR-283 spec.
   *
   * @param nodeType NodeTypeData to be checked
   * @return valid NodeTypeData
   */
  protected NodeTypeData validateNodeType(NodeTypeData nodeType) {

    return nodeType; // TODO
  }
  
  // impl

  public NodeDefinitionData findNodeDefinition(InternalQName nodeName,
                                               InternalQName primaryType,
                                               InternalQName[] mixinTypes) throws RepositoryException {
    
    return defsHolder.getDefaultChildNodeDefinition(primaryType, mixinTypes, nodeName);
  }

  public NodeTypeData findNodeType(InternalQName typeName) throws NoSuchNodeTypeException,
                                                       RepositoryException {
    return nodeTypes.get(typeName);
  }

  public PropertyDefinitionDatas findPropertyDefinitions(InternalQName propertyName,
                                                         InternalQName primaryType,
                                                         InternalQName[] mixinTypes) throws RepositoryException {
    
    //defsHolder.getPropertyDefinition(parentNodeType, childName, multiValued);
    return null;
  }

  public PropertyDefinitionDatas findPropertyDefinitions(InternalQName propertyName,
                                                         List<NodeTypeData> typesList) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<NodeTypeData> getAllNodeTypes() throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isNodeType(InternalQName testTypeName, InternalQName typeName) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isNodeType(InternalQName testTypeName,
                            InternalQName typeName,
                            InternalQName[] typeNames) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isNodeType(InternalQName testTypeName, InternalQName[] typeNames) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isOrderableChildNodesSupported(InternalQName primaryType,
                                                InternalQName[] mixinTypes) throws RepositoryException {
    // TODO Auto-generated method stub
    return false;
  }

}
