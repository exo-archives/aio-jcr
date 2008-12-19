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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.jcr.InvalidItemStateException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;

import org.exoplatform.services.jcr.access.AccessControlPolicy;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.ItemDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionValue;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValuesList;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.nodetype.registration.PropertyDefinitionComparator;
import org.exoplatform.services.jcr.impl.core.query.QueryHandler;
import org.exoplatform.services.jcr.impl.core.query.lucene.FieldNames;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryHits;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.log.ExoLogger;

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

  private final Set<InternalQName>                                    buildInNodeTypesNames;

  /**
   * Listeners (soft references)
   */
  private final Map<NodeTypeManagerListener, NodeTypeManagerListener> listeners;

  private HashSet<QueryHandler>                                       queryHandlers;

  private final ValueFactoryImpl                                      valueFactory;

  public NodeTypeDataManagerImpl(RepositoryEntry config,
                                 LocationFactory locationFactory,
                                 NamespaceRegistry namespaceRegistry,
                                 NodeTypeDataPersister persister) throws RepositoryException {

    this.namespaceRegistry = namespaceRegistry;

    this.persister = persister;

    this.locationFactory = locationFactory;
    this.valueFactory = new ValueFactoryImpl(locationFactory);
    this.accessControlPolicy = config.getAccessControl();

    this.hierarchy = new NodeTypeDataHierarchyHolder();

    this.defsHolder = new ItemDefinitionDataHolder(this.hierarchy);
    this.listeners = Collections.synchronizedMap(new WeakHashMap<NodeTypeManagerListener, NodeTypeManagerListener>());
    this.buildInNodeTypesNames = new HashSet<InternalQName>();
    initDefault();
    this.queryHandlers = new HashSet<QueryHandler>();
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

  public void addQueryHandler(QueryHandler queryHandler) {
    queryHandlers.add(queryHandler);
  }

  /**
   * {@inheritDoc}
   */
  public NodeDefinitionData findChildNodeDefinition(InternalQName nodeName,
                                                    InternalQName... nodeTypeNames) {

    NodeDefinitionData ndResidual = defsHolder.getDefaultChildNodeDefinition(nodeName,
                                                                             nodeTypeNames);

    if (ndResidual == null && !Constants.JCR_ANY_NAME.equals(nodeName))
      ndResidual = findChildNodeDefinition(Constants.JCR_ANY_NAME, nodeTypeNames);

    return ndResidual;

  }

  /**
   * {@inheritDoc}
   */
  public NodeDefinitionData findChildNodeDefinition(InternalQName nodeName,
                                                    InternalQName primaryNodeType,
                                                    InternalQName[] mixinTypes) {

    if (mixinTypes != null) {
      InternalQName[] nts = new InternalQName[mixinTypes.length + 1];
      nts[0] = primaryNodeType;
      for (int i = 0; i < mixinTypes.length; i++) {
        nts[i + 1] = mixinTypes[i];
      }
      return findChildNodeDefinition(nodeName, nts);
    }

    return findChildNodeDefinition(nodeName, primaryNodeType);
  }

  /**
   * {@inheritDoc}
   */
  public NodeTypeData findNodeType(InternalQName typeName) {
    return hierarchy.getNodeType(typeName);
  }

  /**
   * {@inheritDoc}
   */
  public PropertyDefinitionDatas findPropertyDefinitions(InternalQName propertyName,
                                                         InternalQName primaryNodeType,
                                                         InternalQName[] mixinTypes) {

    if (mixinTypes != null) {
      InternalQName[] nts = new InternalQName[mixinTypes.length + 1];
      nts[0] = primaryNodeType;
      for (int i = 0; i < mixinTypes.length; i++) {
        nts[i + 1] = mixinTypes[i];
      }
      return getPropertyDefinitions(propertyName, nts);
    }

    return getPropertyDefinitions(propertyName, primaryNodeType);
  }

  /**
   * @return the accessControlPolicy
   */
  public String getAccessControlPolicy() {
    return accessControlPolicy;
  }

  /**
   * {@inheritDoc}
   */
  public NodeDefinitionData[] getAllChildNodeDefinitions(InternalQName... nodeTypeNames) {
    Collection<NodeDefinitionData> defs = new HashSet<NodeDefinitionData>();

    for (InternalQName ntname : nodeTypeNames) {
      for (NodeDefinitionData cnd : hierarchy.getNodeType(ntname).getDeclaredChildNodeDefinitions())
        defs.add(cnd);

      for (InternalQName suname : hierarchy.getSupertypes(ntname)) {
        for (NodeDefinitionData cnd : hierarchy.getNodeType(suname)
                                               .getDeclaredChildNodeDefinitions())
          defs.add(cnd);
      }
    }

    return defs.toArray(new NodeDefinitionData[defs.size()]);
  }

  /**
   * {@inheritDoc}
   */
  public List<NodeTypeData> getAllNodeTypes() {
    return hierarchy.getAllNodeTypes();
  }

  /**
   * {@inheritDoc}
   */
  public PropertyDefinitionData[] getAllPropertyDefinitions(InternalQName... nodeTypeNames) {
    Collection<PropertyDefinitionData> defs = new HashSet<PropertyDefinitionData>();

    for (InternalQName ntname : nodeTypeNames) {
      for (PropertyDefinitionData pd : hierarchy.getNodeType(ntname)
                                                .getDeclaredPropertyDefinitions())
        defs.add(pd);

      for (InternalQName suname : hierarchy.getSupertypes(ntname)) {
        for (PropertyDefinitionData pd : hierarchy.getNodeType(suname)
                                                  .getDeclaredPropertyDefinitions())
          defs.add(pd);
      }
    }

    return defs.toArray(new PropertyDefinitionData[defs.size()]);
  }

  // impl

  /**
   * {@inheritDoc}
   */
  public NodeDefinitionData getChildNodeDefinition(InternalQName nodeName,
                                                   InternalQName nodeTypeName,
                                                   InternalQName parentTypeName) {
    NodeDefinitionData def = defsHolder.getChildNodeDefinition(parentTypeName,
                                                               nodeName,
                                                               nodeTypeName);
    // residual
    if (def == null)
      def = defsHolder.getChildNodeDefinition(parentTypeName, Constants.JCR_ANY_NAME, nodeTypeName);
    return def;
  }

  public List<ItemDefinitionData> getManadatoryItemDefs(InternalQName primaryNodeType,
                                                        InternalQName[] mixinTypes) {
    Collection<ItemDefinitionData> mandatoryDefs = new HashSet<ItemDefinitionData>();
    // primary type properties
    ItemDefinitionData[] itemDefs = getAllPropertyDefinitions(new InternalQName[] { primaryNodeType });
    for (int i = 0; i < itemDefs.length; i++) {
      if (itemDefs[i].isMandatory())
        mandatoryDefs.add(itemDefs[i]);
    }
    // primary type nodes
    itemDefs = getAllChildNodeDefinitions(new InternalQName[] { primaryNodeType });
    for (int i = 0; i < itemDefs.length; i++) {
      if (itemDefs[i].isMandatory())
        mandatoryDefs.add(itemDefs[i]);
    }
    // mixin properties
    itemDefs = getAllPropertyDefinitions(mixinTypes);
    for (int i = 0; i < itemDefs.length; i++) {
      if (itemDefs[i].isMandatory())
        mandatoryDefs.add(itemDefs[i]);
    }
    // mixin nodes
    itemDefs = getAllChildNodeDefinitions(mixinTypes);
    for (int i = 0; i < itemDefs.length; i++) {
      if (itemDefs[i].isMandatory())
        mandatoryDefs.add(itemDefs[i]);
    }
    return new ArrayList<ItemDefinitionData>(mandatoryDefs);
  }

  /**
   * Return
   * 
   * @param nodeType
   * @return
   * @throws RepositoryException
   * @throws IOException
   */
  public Set<String> getNodes(InternalQName nodeType) throws RepositoryException {
    return getNodes(nodeType, new InternalQName[0], new InternalQName[0]);
  }

  /**
   * Return
   * 
   * @param nodeType
   * @return
   * @throws RepositoryException
   * @throws IOException
   */
  public Set<String> getNodes(InternalQName nodeType,
                              InternalQName[] includeProperties,
                              InternalQName[] excludeProperties) throws RepositoryException {
    Query query = getQuery(nodeType);
    if (includeProperties.length > 0) {
      BooleanQuery tmp = new BooleanQuery();
      for (int i = 0; i < includeProperties.length; i++) {

        String field = locationFactory.createJCRName(includeProperties[i]).getAsString();
        tmp.add(new TermQuery(new Term(FieldNames.PROPERTIES_SET, field)), Occur.MUST);
      }
      tmp.add(query, Occur.MUST);
      query = tmp;
    }

    if (excludeProperties.length > 0) {
      BooleanQuery tmp = new BooleanQuery();
      for (int i = 0; i < includeProperties.length; i++) {

        String field = locationFactory.createJCRName(includeProperties[i]).getAsString();
        tmp.add(new TermQuery(new Term(FieldNames.PROPERTIES_SET, field)), Occur.MUST_NOT);
      }
      tmp.add(query, Occur.MUST_NOT);
      query = tmp;
    }

    Iterator<QueryHandler> it = queryHandlers.iterator();
    Set<String> result = new HashSet<String>();

    try {
      while (it.hasNext()) {
        QueryHandler queryHandler = it.next();
        QueryHits hits = queryHandler.executeQuery(query,
                                                   true,
                                                   new InternalQName[0],
                                                   new boolean[0]);
        for (int i = 0; i < hits.length(); i++) {
          result.add(hits.getFieldContent(i, FieldNames.UUID));
        }
      }
    } catch (IOException e) {
      throw new RepositoryException(e.getLocalizedMessage(), e);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public PropertyDefinitionDatas getPropertyDefinitions(InternalQName propertyName,
                                                        InternalQName... nodeTypeNames) {

    PropertyDefinitionDatas propertyDefinitions = defsHolder.getPropertyDefinitions(propertyName,
                                                                                    nodeTypeNames);
    // Try super
    if (propertyDefinitions == null) {
      for (int i = 0; i < nodeTypeNames.length && propertyDefinitions == null; i++) {
        InternalQName[] supers = hierarchy.getNodeType(nodeTypeNames[i])
                                          .getDeclaredSupertypeNames();
        propertyDefinitions = getPropertyDefinitions(propertyName, supers);

      }
    }

    // try residual def
    if (propertyDefinitions == null && !propertyName.equals(Constants.JCR_ANY_NAME)) {
      propertyDefinitions = getPropertyDefinitions(Constants.JCR_ANY_NAME, nodeTypeNames);
    }

    return propertyDefinitions;
  }

  // TODO make me private
  public Set<QueryHandler> getQueryHandlers() {
    return queryHandlers;
  }

  public boolean isChildNodePrimaryTypeAllowed(InternalQName childNodeTypeName,
                                               InternalQName parentNodeType,
                                               InternalQName[] parentMixinNames) {
    // NodeTypeData childDef = findNodeType(childNodeTypeName);
    Set<InternalQName> testSuperTypesNames = hierarchy.getSupertypes(childNodeTypeName);
    NodeDefinitionData[] allChildNodeDefinitions = getAllChildNodeDefinitions(parentNodeType);
    for (NodeDefinitionData cnd : allChildNodeDefinitions) {
      for (InternalQName req : cnd.getRequiredPrimaryTypes()) {
        if (childNodeTypeName.equals(req))
          return true;
        for (InternalQName superName : testSuperTypesNames) {
          if (superName.equals(req))
            return true;
        }
      }
    }
    allChildNodeDefinitions = getAllChildNodeDefinitions(parentMixinNames);
    for (NodeDefinitionData cnd : allChildNodeDefinitions) {
      for (InternalQName req : cnd.getRequiredPrimaryTypes()) {
        if (childNodeTypeName.equals(req))
          return true;
        for (InternalQName superName : testSuperTypesNames) {
          if (superName.equals(req))
            return true;
        }
      }
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isNodeType(final InternalQName testTypeName, final InternalQName... typesNames) {
    return hierarchy.isNodeType(testTypeName, typesNames);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isNodeType(final InternalQName testTypeName,
                            final InternalQName primaryType,
                            final InternalQName[] mixinTypes) {

    if (hierarchy.isNodeType(testTypeName, primaryType))
      return true;

    if (hierarchy.isNodeType(testTypeName, mixinTypes))
      return true;

    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isOrderableChildNodesSupported(final InternalQName primaryType,
                                                final InternalQName[] mixinTypes) {

    final int nlen = mixinTypes != null ? mixinTypes.length : 0;
    for (int i = -1; i < nlen; i++) {
      InternalQName name;
      if (i < 0)
        name = primaryType;
      else
        name = mixinTypes[i];

      NodeTypeData nt = hierarchy.getNodeType(name);

      if (nt != null) {
        if (nt.hasOrderableChildNodes())
          return true;

        Set<InternalQName> supers = hierarchy.getSupertypes(nt.getName());
        for (InternalQName suName : supers) {
          NodeTypeData su = hierarchy.getNodeType(suName);
          if (su != null && su.hasOrderableChildNodes())
            return true;
        }
      }
    }

    return false;
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

    NodeTypeData registeredNodeType = findNodeType(qname);
    if (registeredNodeType != null) {
      switch (alreadyExistsBehaviour) {
      case ExtendedNodeTypeManager.FAIL_IF_EXISTS:
        throw new RepositoryException("NodeType " + nodeType.getName() + " is already registered");
      case ExtendedNodeTypeManager.IGNORE_IF_EXISTS:
        LOG.warn("Skipped " + nodeType.getName() + " as already registered");
        break;
      case ExtendedNodeTypeManager.REPLACE_IF_EXISTS:
        reregisterNodeType(registeredNodeType, nodeType);
        break;
      }
    } else
      persister.saveChanges(internalRegister(nodeType, true));

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

      PropertyDefinitionData pd;
      try {
        pd = new PropertyDefinitionData(locationFactory.parseJCRName(v.getName()).getInternalName(),
                                        ntName,
                                        v.isAutoCreate(),
                                        v.isMandatory(),
                                        v.getOnVersion(),
                                        v.isReadOnly(),
                                        v.getRequiredType(),
                                        v.getValueConstraints() != null ? v.getValueConstraints()
                                                                           .toArray(new String[v.getValueConstraints()
                                                                                                .size()])
                                                                       : new String[0],
                                        v.getDefaultValueStrings() == null ? new String[0]
                                                                          : v.getDefaultValueStrings()
                                                                             .toArray(new String[v.getDefaultValueStrings()
                                                                                                  .size()]),
                                        v.isMultiple());

        props[i] = pd;
      } catch (Exception e) {
        e.printStackTrace();
      }

    }

    List<NodeDefinitionValue> ndlist = ntvalue.getDeclaredChildNodeDefinitionValues();
    NodeDefinitionData[] nodes = new NodeDefinitionData[ndlist.size()];
    for (int i = 0; i < ndlist.size(); i++) {
      NodeDefinitionValue v = ndlist.get(i);

      List<String> rnts = v.getRequiredNodeTypeNames();
      InternalQName[] requiredNTs = new InternalQName[rnts.size()];
      for (int ri = 0; ri < rnts.size(); ri++) {
        requiredNTs[ri] = locationFactory.parseJCRName(rnts.get(ri)).getInternalName();
      }
      InternalQName defaultNodeName = null;
      if (v.getDefaultNodeTypeName() != null) {
        defaultNodeName = locationFactory.parseJCRName(v.getDefaultNodeTypeName())
                                         .getInternalName();
      }
      NodeDefinitionData nd = new NodeDefinitionData(locationFactory.parseJCRName(v.getName())
                                                                    .getInternalName(),
                                                     ntName,
                                                     v.isAutoCreate(),
                                                     v.isMandatory(),
                                                     v.getOnVersion(),
                                                     v.isReadOnly(),
                                                     requiredNTs,
                                                     defaultNodeName,
                                                     v.isSameNameSiblings());
      nodes[i] = nd;
    }

    InternalQName primaryItemName = null;
    if (ntvalue.getPrimaryItemName() != null)
      primaryItemName = locationFactory.parseJCRName(ntvalue.getPrimaryItemName())
                                       .getInternalName();

    NodeTypeData ntdata = new NodeTypeData(ntName,
                                           primaryItemName,
                                           ntvalue.isMixin(),
                                           ntvalue.isOrderableChild(),
                                           supertypes,
                                           props,
                                           nodes);

    registerNodeType(ntdata, alreadyExistsBehaviour);

    return ntdata;
  }

  /**
   * {@inheritDoc}
   */
  public void registerNodeTypes(Collection<NodeTypeData> nodeTypes, int alreadyExistsBehaviour) throws RepositoryException {
    // 1. validate collection and self/new referencing, TODO

    // 2. traverse and reg
    for (NodeTypeData nt : nodeTypes) {
      registerNodeType(nt, alreadyExistsBehaviour);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<NodeTypeData> registerNodeTypes(Collection<NodeTypeValue> ntvalues,
                                              int alreadyExistsBehaviour) throws RepositoryException {
    // 1. validate collection and self/new referencing, TODO

    // 2. traverse and reg
    List<NodeTypeData> nts = new ArrayList<NodeTypeData>();
    for (NodeTypeValue v : ntvalues) {
      nts.add(registerNodeType(v, alreadyExistsBehaviour));
    }

    return nts;
  }

  /**
   * {@inheritDoc}
   */
  public List<NodeTypeData> registerNodeTypes(InputStream xml, int alreadyExistsBehaviour) throws RepositoryException {

    try {
      IBindingFactory factory = BindingDirectory.getFactory(NodeTypeValuesList.class);
      IUnmarshallingContext uctx = factory.createUnmarshallingContext();
      NodeTypeValuesList nodeTypeValuesList = (NodeTypeValuesList) uctx.unmarshalDocument(xml, null);
      List ntvList = nodeTypeValuesList.getNodeTypeValuesList();

      long start = System.currentTimeMillis();
      List<NodeTypeData> nts = new ArrayList<NodeTypeData>();
      for (int i = 0; i < ntvList.size(); i++) {
        if (ntvList.get(i) != null) {
          NodeTypeValue nodeTypeValue = (NodeTypeValue) ntvList.get(i);
          nts.add(registerNodeType(nodeTypeValue, alreadyExistsBehaviour));
        } else {
          // Hm! Smth is wrong in xml document
          LOG.error("Empty nodeTypeValue in xml document, index: " + i + ", skiping...");
        }
      }
      registerNodeTypes(nts, alreadyExistsBehaviour);

      LOG.info("Nodetypes registered from xml definitions (count: " + ntvList.size() + "). "
          + (System.currentTimeMillis() - start) + " ms.");
      return nts;
    } catch (JiBXException e) {
      throw new RepositoryException("Error in config initialization " + e, e);
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
   * Unregisters the specified node type. In order for a node type to be
   * successfully unregistered it must meet the following conditions:
   * <ol>
   * <li>the node type must obviously be registered.</li>
   * <li>a built-in node type can not be unregistered.</li>
   * <li>the node type must not have dependents, i.e. other node types that are
   * referencing it.</li>
   * <li>the node type must not be currently used by any workspace.</li>
   * </ol>
   * 
   * @param ntName name of the node type to be unregistered
   * @throws NoSuchNodeTypeException if <code>ntName</code> does not denote a
   *           registered node type.
   * @throws RepositoryException
   * @throws RepositoryException if another error occurs.
   * @see #unregisterNodeTypes(Collection)
   */
  public void unregisterNodeType(InternalQName nodeTypeName) throws RepositoryException {

    NodeTypeData nodeType = hierarchy.getNodeType(nodeTypeName);
    if (nodeType == null)
      throw new NoSuchNodeTypeException(nodeTypeName.getAsString());
    // check build in
    if (buildInNodeTypesNames.contains(nodeTypeName))
      throw new RepositoryException(nodeTypeName.toString()
          + ": can't unregister built-in node type.");
    // check dependencies
    Set<InternalQName> descendantNt = hierarchy.getDescendantNodeTypes(nodeTypeName);
    if (descendantNt.size() > 0) {
      String message = "Can not remove " + nodeTypeName.getAsString()
          + "nodetype, because the following node types depend on it: ";
      for (InternalQName internalQName : descendantNt) {
        message += internalQName.getAsString() + " ";
      }
      throw new RepositoryException(message);
    }
    Set<String> nodes = getNodes(nodeTypeName);
    if (nodes.size() > 0) {
      String message = "Can not remove " + nodeTypeName.getAsString()
          + " nodetype, because the following node types is used in nodes with uuid: ";
      for (String uuids : nodes) {
        message += uuids + " ";
      }
      throw new RepositoryException(message);

    }
    internalUnregister(nodeTypeName, nodeType);
  }

  void reregisterNodeType(NodeTypeData ancestorDefinition, NodeTypeData recipientDefinition) throws RepositoryException {
    if (!ancestorDefinition.getName().equals(recipientDefinition.getName())) {
      throw new RepositoryException("Unsupported Operation");
    }
    if (buildInNodeTypesNames.contains(recipientDefinition.getName())) {
      throw new RepositoryException(recipientDefinition.getName()
          + ": can't reregister built-in node type.");
    }
    PlainChangesLog changesLog = new PlainChangesLogImpl();
    PropertyDefinitionComparator propertyDefinitionComparator = new PropertyDefinitionComparator(this,
                                                                                                 persister.getDataManager());
    changesLog.addAll(propertyDefinitionComparator.processPropertyDefinitionChanges(recipientDefinition,
                                                                                    ancestorDefinition.getDeclaredPropertyDefinitions(),
                                                                                    recipientDefinition.getDeclaredPropertyDefinitions())
                                                  .getAllStates());
    // TODO super names
    // TODO primaryItemName
    // TODO child nodes
    // TODO properties defs

    // TODO hasOrderableChildNodes
    // TODO mixinom

    changesLog.addAll(internalUnregister(recipientDefinition.getName(), recipientDefinition));
    changesLog.addAll(internalRegister(recipientDefinition, false).getAllStates());
    persister.saveChanges(changesLog);
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
  /**
   * validateNodeType.
   * 
   * @param nodeType
   * @return
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

  private Query getQuery(InternalQName nodeType) throws RepositoryException {
    List<Term> terms = new ArrayList<Term>();
    // try {
    String mixinTypesField = locationFactory.createJCRName(Constants.JCR_MIXINTYPES).getAsString();
    String primaryTypeField = locationFactory.createJCRName(Constants.JCR_PRIMARYTYPE)
                                             .getAsString();

    // ExtendedNodeTypeManager ntMgr =
    // session.getWorkspace().getNodeTypeManager();
    NodeTypeData base = findNodeType(nodeType);

    if (base.isMixin()) {
      // search for nodes where jcr:mixinTypes is set to this mixin
      Term t = new Term(FieldNames.PROPERTIES,
                        FieldNames.createNamedValue(mixinTypesField,
                                                    locationFactory.createJCRName(nodeType)
                                                                   .getAsString()));
      terms.add(t);
    } else {
      // search for nodes where jcr:primaryType is set to this type
      Term t = new Term(FieldNames.PROPERTIES,
                        FieldNames.createNamedValue(primaryTypeField,
                                                    locationFactory.createJCRName(nodeType)
                                                                   .getAsString()));
      terms.add(t);
    }

    Iterator<InternalQName> allTypes = hierarchy.getDescendantNodeTypes(nodeType).iterator();
    while (allTypes.hasNext()) {

      NodeTypeData nodeTypeData = findNodeType(allTypes.next());

      String ntName = locationFactory.createJCRName(nodeTypeData.getName()).getAsString();
      Term t;
      if (nodeTypeData.isMixin()) {
        // search on jcr:mixinTypes
        t = new Term(FieldNames.PROPERTIES, FieldNames.createNamedValue(mixinTypesField, ntName));
      } else {
        // search on jcr:primaryType
        t = new Term(FieldNames.PROPERTIES, FieldNames.createNamedValue(primaryTypeField, ntName));
      }
      terms.add(t);
    }
    // now search for all node types that are derived from base

    if (terms.size() == 0) {
      // exception occured
      return new BooleanQuery();
    } else if (terms.size() == 1) {
      return new TermQuery(terms.get(0));
    } else {
      BooleanQuery b = new BooleanQuery();
      for (Term term : terms) {
        b.add(new TermQuery(term), Occur.SHOULD);
      }
      return b;
    }
  }

  private void initDefault() throws RepositoryException {
    long start = System.currentTimeMillis();
    try {
      InputStream xml = NodeTypeManagerImpl.class.getResourceAsStream(NODETYPES_FILE);
      if (xml != null) {
        List<NodeTypeData> defaultNts = registerNodeTypes(xml,
                                                          ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
        for (NodeTypeData nodeTypeData : defaultNts) {
          buildInNodeTypesNames.add(nodeTypeData.getName());
        }
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
   * @param nodeType
   * @throws RepositoryException
   * @throws ValueFormatException
   * @throws PathNotFoundException
   */
  private PlainChangesLog internalRegister(NodeTypeData nodeType, boolean checkExistence) throws PathNotFoundException,
                                                                                         ValueFormatException,
                                                                                         RepositoryException {
    PlainChangesLog changesLog = new PlainChangesLogImpl();
    long start = System.currentTimeMillis();
    hierarchy.addNodeType(nodeType);

    defsHolder.putDefinitions(nodeType.getName(), nodeType);
    // put supers
    Set<InternalQName> supers = hierarchy.getSupertypes(nodeType.getName());

    for (InternalQName superName : supers) {
      defsHolder.putDefinitions(nodeType.getName(), hierarchy.getNodeType(superName));
    }

    if (persister.isInitialized()) {
      try {
        if (!checkExistence || !persister.hasNodeTypeData(nodeType.getName())) {
          changesLog.addAll(persister.addNodeType(nodeType).getAllStates());
          // persister.addNodeType(nodeType);
          // persister.saveChanges();
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
    return changesLog;
  }

  /**
   * @param nodeTypeName
   * @param nodeType
   * @throws RepositoryException
   */
  private List<ItemState> internalUnregister(InternalQName nodeTypeName, NodeTypeData nodeType) throws RepositoryException {
    // remove from internal lists
    hierarchy.removeNodeType(nodeTypeName);
    // put supers
    Set<InternalQName> supers = hierarchy.getSupertypes(nodeTypeName);

    // remove supers
    if (supers != null)
      for (InternalQName superName : supers) {
        defsHolder.removeDefinitions(nodeTypeName, hierarchy.getNodeType(superName));
      }
    // remove it self
    defsHolder.removeDefinitions(nodeTypeName, nodeType);
    return persister.removeNodeType(nodeType);
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
