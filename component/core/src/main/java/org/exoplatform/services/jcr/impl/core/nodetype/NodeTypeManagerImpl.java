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
package org.exoplatform.services.jcr.impl.core.nodetype;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.query.QueryHandler;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.util.EntityCollection;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: NodeTypeManagerImpl.java 13986 2008-05-08 10:48:43Z pnedonosko
 *          $
 */
public class NodeTypeManagerImpl implements ExtendedNodeTypeManager {

  protected static final Log          LOG            = ExoLogger.getLogger("jcr.NodeTypeManagerImpl");

  private static final String         NODETYPES_FILE = "nodetypes.xml";

  public static final String          NODETYPES_ROOT = "/jcr:system/jcr:nodetypes";

  protected final ValueFactory        valueFactory;

  protected final LocationFactory     locationFactory;

  protected final NodeTypeDataManager typesManager;

  /**
   * Listeners (soft references) TODO Map<NodeTypeManagerListener,
   * NodeTypeManagerListener> never free the self reference
   */
  // private final Map<NodeTypeManagerListener, NodeTypeManagerListener>
  // listeners = Collections.synchronizedMap(new
  // WeakHashMap<NodeTypeManagerListener, NodeTypeManagerListener>());
  private HashSet<QueryHandler>       queryHandlers;

  public NodeTypeManagerImpl(LocationFactory locationFactory,
                             ValueFactoryImpl valueFactory,
                             NodeTypeDataManager typesManager) {
    this.valueFactory = valueFactory;
    this.locationFactory = locationFactory;
    this.typesManager = typesManager;

  }

  @Deprecated
  public WorkspaceNTManagerImpl createWorkspaceNTManager(SessionImpl session) throws RepositoryException {
    WorkspaceNTManagerImpl wntm = new WorkspaceNTManagerImpl(null, session, typesManager);
    return wntm;
  }

  // JSR-170 stuff ================================

  /**
   * {@inheritDoc}
   */
  public NodeType getNodeType(final String nodeTypeName) throws NoSuchNodeTypeException,
                                                        RepositoryException {
    NodeTypeData ntdata = typesManager.findNodeType(locationFactory.parseJCRName(nodeTypeName)
                                                                   .getInternalName());
    if (ntdata != null)
      return new NodeTypeImpl(ntdata, typesManager, this, locationFactory, valueFactory);
    else
      throw new NoSuchNodeTypeException("Nodetype not found " + nodeTypeName);
  }

  /**
   * {@inheritDoc}
   */
  public NodeTypeIterator getAllNodeTypes() {
    EntityCollection ec = new EntityCollection();
    List<NodeTypeData> allNts = typesManager.getAllNodeTypes();

    for (NodeTypeData ntdata : allNts)
      ec.add(new NodeTypeImpl(ntdata, typesManager, this, locationFactory, valueFactory));

    return ec;
  }

  /**
   * Returns an iterator over all available primary node types.
   * 
   * @return An <code>NodeTypeIterator</code>.
   * @throws RepositoryException if an error occurs.
   */
  public NodeTypeIterator getPrimaryNodeTypes() throws RepositoryException {
    EntityCollection ec = new EntityCollection();
    NodeTypeIterator allTypes = getAllNodeTypes();
    while (allTypes.hasNext()) {
      NodeType type = allTypes.nextNodeType();
      if (!type.isMixin())
        ec.add(type);
    }
    return ec;
  }

  /**
   * Returns an iterator over all available mixin node types.
   * 
   * @return An <code>NodeTypeIterator</code>.
   * @throws RepositoryException if an error occurs.
   */
  public NodeTypeIterator getMixinNodeTypes() throws RepositoryException {
    List<NodeTypeData> allNodeTypes = typesManager.getAllNodeTypes();

    Collections.sort(allNodeTypes, new NodeTypeDataComparator());

    EntityCollection ec = new EntityCollection();
    for (NodeTypeData nodeTypeData : allNodeTypes) {
      if (nodeTypeData.isMixin())
        ec.add(new NodeTypeImpl(nodeTypeData, typesManager, this, locationFactory, valueFactory));

    }
    return ec;
  }

  // eXo stuff ================================

  public NodeTypeDataManager getDataManager() {
    return typesManager;
  }

  public ExtendedNodeType findNodeType(InternalQName nodeTypeName) throws NoSuchNodeTypeException,
                                                                  RepositoryException {

    NodeTypeData ntdata = typesManager.findNodeType(nodeTypeName);
    if (ntdata != null)
      return new NodeTypeImpl(ntdata, typesManager, this, locationFactory, valueFactory);
    else
      throw new NoSuchNodeTypeException("Nodetype not found " + nodeTypeName.getAsString());
  }

  @Deprecated
  public List<ExtendedNodeType> getNodeTypes(InternalQName primaryType, InternalQName[] mixinTypes) throws NoSuchNodeTypeException,
                                                                                                   RepositoryException {
    ExtendedNodeType primaryNt = findNodeType(primaryType);
    if (primaryNt == null)
      throw new NoSuchNodeTypeException("Node (primary) type '" + primaryType.getAsString()
          + "' is not found.");

    List<ExtendedNodeType> nts = new ArrayList<ExtendedNodeType>();
    nts.add(primaryNt);

    if (mixinTypes != null)
      for (InternalQName mixin : mixinTypes) {
        ExtendedNodeType mixinNt = findNodeType(mixin);
        if (mixinNt == null)
          throw new NoSuchNodeTypeException("Node (mixin) type '" + mixin.getAsString()
              + "' is not found.");
        if (!mixinNt.isMixin())
          throw new IllegalArgumentException("Node type '" + mixin.getAsString()
              + "' is not mixin.");
        nts.add(mixinNt);
      }

    return nts;
  }

  /**
   * Don't use. To use findPropertyDefinitions() and getAnyDefinition() then.
   */
  @Deprecated
  public PropertyDefinition findPropertyDefinition(InternalQName propertyName,
                                                   List<ExtendedNodeType> typesList) throws RepositoryException {
    PropertyDefinition pdResidual = null;
    for (ExtendedNodeType nt : typesList) {
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

    if (pdResidual == null)
      throw new RepositoryException("Property definition '" + propertyName.getAsString()
          + "' is not found.");

    return pdResidual;
  }

  /**
   * Don't use. To use findPropertyDefinitions() and getAnyDefinition() then.
   */
  @Deprecated
  public PropertyDefinition findPropertyDefinition(InternalQName propertyName,
                                                   InternalQName primaryType,
                                                   InternalQName[] mixinTypes) throws RepositoryException {

    List<ExtendedNodeType> allTypes = getNodeTypes(primaryType, mixinTypes);
    return findPropertyDefinition(propertyName, allTypes);
  }

  public PropertyDefinitions findPropertyDefinitions(InternalQName propertyName,
                                                     List<ExtendedNodeType> typesList) throws RepositoryException {
    PropertyDefinitions pdResidual = null;
    for (ExtendedNodeType nt : typesList) {
      PropertyDefinitions pds = nt.getPropertyDefinitions(propertyName);

      // need to check any definition first!!!
      PropertyDefinitionImpl pd = (PropertyDefinitionImpl) pds.getAnyDefinition();
      if (pd != null) {
        if (pd.isResidualSet())
          pdResidual = pds;
        else
          return pds;
      }
    }

    if (pdResidual == null)
      throw new RepositoryException("Property definition '" + propertyName.getAsString()
          + "' is not found.");

    return pdResidual;
  }

  public PropertyDefinitions findPropertyDefinitions(InternalQName propertyName,
                                                     InternalQName primaryType,
                                                     InternalQName[] mixinTypes) throws RepositoryException {

    List<ExtendedNodeType> allTypes = getNodeTypes(primaryType, mixinTypes);
    return findPropertyDefinitions(propertyName, allTypes);
  }

  public NodeDefinitionImpl findNodeDefinition(InternalQName nodeName,
                                               List<ExtendedNodeType> typesList) throws RepositoryException {
    NodeDefinitionImpl ndResidual = null;
    for (ExtendedNodeType nt : typesList) {
      NodeDefinitionImpl nd = (NodeDefinitionImpl) nt.getChildNodeDefinition(nodeName);
      if (nd != null) {
        if (nd.isResidualSet())
          ndResidual = nd;
        else
          return nd;
      }
    }

    if (ndResidual == null)
      throw new RepositoryException("Child node definition '" + nodeName.getAsString()
          + "' is not found.");

    return ndResidual;
  }

  public NodeDefinitionImpl findNodeDefinition(InternalQName nodeName,
                                               InternalQName primaryType,
                                               InternalQName[] mixinTypes) throws RepositoryException {

    List<ExtendedNodeType> allTypes = getNodeTypes(primaryType, mixinTypes);
    return findNodeDefinition(nodeName, allTypes);
  }

  public boolean isOrderableChildNodesSupported(InternalQName primaryType,
                                                InternalQName[] mixinTypes) throws RepositoryException {

    for (ExtendedNodeType nt : getNodeTypes(primaryType, mixinTypes)) {
      if (nt.hasOrderableChildNodes()) {
        return true;
      }
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  public void registerNodeType(ExtendedNodeType nodeType, int alreadyExistsBehaviour) throws RepositoryException {
    throw new UnsupportedOperationException();

    // if (nodeType == null) {
    // throw new RepositoryException("NodeType object " + nodeType +
    // " is null");
    // }
    //
    // long start = System.currentTimeMillis();
    //
    // if (accessControlPolicy.equals(AccessControlPolicy.DISABLE)
    // && nodeType.getName().equals("exo:privilegeable")) {
    // throw new RepositoryException("NodeType exo:privilegeable is DISABLED");
    // }
    //
    // InternalQName qname = nodeType.getQName();
    // if (qname == null) {
    // throw new RepositoryException("NodeType implementation class "
    // + nodeType.getClass().getName() + " is not supported in this method");
    // }
    //
    // if (findNodeType(qname) != null) {
    // if (alreadyExistsBehaviour == FAIL_IF_EXISTS) {
    // throw new RepositoryException("NodeType " + nodeType.getName() +
    // " is already registered");
    // } else
    // LOG.warn("NodeType " + nodeType.getName() + " is already registered");
    // return;
    // }
    //
    // // TODO nodeTypes.put(nodeType.getQName(), nodeType);
    // typesManager.registerNodeType(nodeType, alreadyExistsBehaviour);

    // TODO
    // if (persister.isInitialized()) {
    // try {
    // if (!persister.hasNodeTypeData(nodeType.getName())) {
    // persister.addNodeType(nodeType);
    // persister.saveChanges();
    // }
    // } catch (InvalidItemStateException e) {
    // LOG.warn("Error of storing node type " + nodeType.getName()
    // + ". May be node type already registered .", e);
    // }
    // if (LOG.isDebugEnabled())
    // LOG.debug("NodeType " + nodeType.getName() + " initialized. "
    // + (System.currentTimeMillis() - start) + " ms");
    // } else {
    // if (LOG.isDebugEnabled())
    // LOG.debug("NodeType " + nodeType.getName()
    // + " registered but not initialized (storage is not initialized). "
    // + (System.currentTimeMillis() - start) + " ms");
    // }
  }

  /**
   * {@inheritDoc}
   */
  public void registerNodeType(Class<ExtendedNodeType> nodeTypeType, int alreadyExistsBehaviour) throws RepositoryException,
                                                                                                InstantiationException {

    throw new UnsupportedOperationException();
    // registerNodeType((ExtendedNodeType) makeNtFromClass(nodeTypeType),
    // alreadyExistsBehaviour);
  }

  /**
   * {@inheritDoc}
   */
  public void registerNodeType(NodeTypeValue nodeTypeValue, int alreadyExistsBehaviour) throws RepositoryException {

    // if (accessControlPolicy.equals(AccessControlPolicy.DISABLE)) {
    // List<String> nsupertypes = nodeTypeValue.getDeclaredSupertypeNames();
    // if (nsupertypes != null && nsupertypes.contains("exo:privilegeable")
    // || nodeTypeValue.getName().equals("exo:privilegeable")) {
    // // skip this node, so it's not necessary at this runtime
    // // + "' -- it's not necessary at this runtime";
    // LOG.warn("Node type " + nodeTypeValue.getName()
    // + " is not register due to DISABLE control policy");
    // return;
    // }
    // }
    //
    // // We have to validate node value before registering it
    // nodeTypeValue.validateNodeType();
    // // NodeTypeImpl nodeType = new NodeTypeImpl(this, nodeTypeValue);
    // // registerNodeType(nodeType, alreadyExistsBehaviour);

    typesManager.registerNodeType(nodeTypeValue, alreadyExistsBehaviour);
  }

  /**
   * {@inheritDoc}
   */
  public void registerNodeTypes(InputStream xml, int alreadyExistsBehaviour) throws RepositoryException {

    typesManager.registerNodeTypes(xml, alreadyExistsBehaviour);
    // try {
    // IBindingFactory factory =
    // BindingDirectory.getFactory(NodeTypeValuesList.class);
    // IUnmarshallingContext uctx = factory.createUnmarshallingContext();
    // NodeTypeValuesList nodeTypeValuesList = (NodeTypeValuesList)
    // uctx.unmarshalDocument(xml, null);
    // ArrayList ntvList = nodeTypeValuesList.getNodeTypeValuesList();
    //
    // long start = System.currentTimeMillis();
    // for (int i = 0; i < ntvList.size(); i++) {
    // if (ntvList.get(i) != null) {
    // NodeTypeValue nodeTypeValue = (NodeTypeValue) ntvList.get(i);
    // registerNodeType(nodeTypeValue, alreadyExistsBehaviour);
    // } else {
    // // Hm! Smth is wrong in xml document
    // LOG.error("Empty nodeTypeValue in xml document, index: " + i +
    // ", skiping...");
    // }
    // }
    // LOG.info("Nodetypes registered from xml definitions (count: " +
    // ntvList.size() + "). "
    // + (System.currentTimeMillis() - start) + " ms.");
    // } catch (JiBXException e) {
    // throw new RepositoryException("Error in config initialization " + e, e);
    // }
  }

  private NodeType makeNtFromClass(Class<ExtendedNodeType> nodeTypeType) throws InstantiationException {

    try {
      Constructor<ExtendedNodeType> c = nodeTypeType.getConstructor(new Class[] { NodeTypeManager.class });
      return c.newInstance(new Object[] { this });
    } catch (Exception e1) {
      throw new InstantiationException("Error in making istance of "
          + nodeTypeType.getName()
          + ". Class should have been NodeType subclass and have constructor with one argument NodeTypeManager type. Reason: "
          + e1.getCause());
    }
  }

  // private void initDefault() throws RepositoryException {
  // long start = System.currentTimeMillis();
  // try {
  // InputStream xml =
  // NodeTypeManagerImpl.class.getResourceAsStream(NODETYPES_FILE);
  // if (xml != null) {
  // registerNodeTypes(xml, ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
  // } else {
  // String msg = "Resource file '" + NODETYPES_FILE
  // +
  // "' with NodeTypes configuration does not found. Can not create node type manager"
  // ;
  // LOG.error(msg);
  // throw new RepositoryException(msg);
  // }
  // } catch (Exception e) {
  // String msg =
  // "Error of initialization default types. Resource file with NodeTypes configuration '"
  // + NODETYPES_FILE + "'. " + e;
  // LOG.error(msg);
  // throw new RepositoryException(msg, e);
  // } finally {
  // LOG.info("Initialization of default nodetypes done. " +
  // (System.currentTimeMillis() - start)
  // + " ms.");
  // }
  // }

  // public void loadFromStorage() throws RepositoryException {
  // long start = System.currentTimeMillis();
  //
  // try {
  // List<NodeType> rNodeTypes = new ArrayList<NodeType>();
  // rNodeTypes.addAll(nodeTypes.values());
  // List<NodeType> loadedNt = persister.loadNodetypes(rNodeTypes, this);
  // for (NodeType nodeType : loadedNt) {
  // nodeTypes.put(((ExtendedNodeType) nodeType).getQName(), (ExtendedNodeType)
  // nodeType);
  // }
  // if (loadedNt.size() > 0)
  // LOG.info("NodeTypes (count: " + loadedNt.size() + ") loaded. "
  // + (System.currentTimeMillis() - start) + " ms");
  // } catch (PathNotFoundException e) {
  // LOG.warn(
  // "NodeTypes storage (/jcr:system/jcr:nodetypes) is not initialized. Only default nodetypes is accessible"
  // );
  // return;
  // }
  // }

  public boolean isNodeType(InternalQName testTypeName, InternalQName[] typeNames) {

    for (int i = 0; i < typeNames.length; i++) {
      if (testTypeName.equals(typeNames[i])) {
        return true;
      }

      ExtendedNodeType subType;
      ExtendedNodeType testType;
      try {
        subType = findNodeType(typeNames[i]);
        testType = findNodeType(testTypeName);
      } catch (RepositoryException e) {
        LOG.error("Error obtaining node type " + e);
        continue;
      }

      NodeType[] superTypes = subType.getSupertypes();
      for (int j = 0; j < superTypes.length; j++) {
        ExtendedNodeType testSuperType = (ExtendedNodeType) superTypes[j];
        if (testSuperType.getQName().equals(testType.getQName()))
          return true;
      }
    }
    return false;
  }

  /**
   * For use with primary and mixin types in one call
   */
  public boolean isNodeType(InternalQName testTypeName,
                            InternalQName typeName,
                            InternalQName[] typeNames) {

    if (isNodeType(testTypeName, typeName))
      return true;

    for (InternalQName tname : typeNames) {
      if (isNodeType(testTypeName, tname))
        return true;
    }
    return false;
  }

  public boolean isNodeType(InternalQName testTypeName, InternalQName typeName) {

    if (testTypeName.equals(typeName)) {
      return true;
    }

    ExtendedNodeType subType;
    ExtendedNodeType testType;
    try {
      subType = findNodeType(typeName);
      testType = findNodeType(testTypeName);
    } catch (RepositoryException e) {
      LOG.error("Error obtaining node type " + e);
      return false;
    }

    NodeType[] superTypes = subType.getSupertypes();
    for (int j = 0; j < superTypes.length; j++) {
      ExtendedNodeType testSuperType = (ExtendedNodeType) superTypes[j];
      if (testSuperType.getQName().equals(testType.getQName()))
        return true;
    }
    return false;
  }

  public LocationFactory getLocationFactory() {
    return locationFactory;
  }

  public ValueFactory getValueFactory() {
    return valueFactory;
  }

  /**
   * {@inheritDoc}
   */
  public NodeTypeValue getNodeTypeValue(String ntName) throws NoSuchNodeTypeException,
                                                      RepositoryException {
    throw new RepositoryException("Unsupported operation");
  }

  //
  // /**
  // * Add a <code>NodeTypeRegistryListener</code>
  // *
  // * @param listener the new listener to be informed on (un)registration of
  // node
  // * types
  // */
  // public void addListener(NodeTypeManagerListener listener) {
  // if (!listeners.containsKey(listener)) {
  // listeners.put(listener, listener);
  // }
  // }
  //
  // /**
  // * Remove a <code>NodeTypeRegistryListener</code>.
  // *
  // * @param listener an existing listener
  // */
  // public void removeListener(NodeTypeManagerListener listener) {
  // listeners.remove(listener);
  // }
  //
  // /**
  // * Notify the listeners that a node type <code>ntName</code> has been
  // * registered.
  // *
  // * @param ntName NT name.
  // */
  // private void notifyRegistered(InternalQName ntName) {
  // // copy listeners to array to avoid ConcurrentModificationException
  // NodeTypeManagerListener[] la = listeners.values()
  // .toArray(new NodeTypeManagerListener[listeners.size()]);
  // for (int i = 0; i < la.length; i++) {
  // if (la[i] != null) {
  // la[i].nodeTypeRegistered(ntName);
  // }
  // }
  // }
  //
  // /**
  // * Notify the listeners that a node type <code>ntName</code> has been
  // * re-registered.
  // *
  // * @param ntName NT name.
  // */
  // private void notifyReRegistered(InternalQName ntName) {
  // // copy listeners to array to avoid ConcurrentModificationException
  // NodeTypeManagerListener[] la = listeners.values()
  // .toArray(new NodeTypeManagerListener[listeners.size()]);
  // for (int i = 0; i < la.length; i++) {
  // if (la[i] != null) {
  // la[i].nodeTypeReRegistered(ntName);
  // }
  // }
  // }
  //
  // /**
  // * Notify the listeners that a node type <code>ntName</code> has been
  // * unregistered.
  // *
  // * @param ntName NT name.
  // */
  // private void notifyUnregistered(InternalQName ntName) {
  // // copy listeners to array to avoid ConcurrentModificationException
  // NodeTypeManagerListener[] la = listeners.values()
  // .toArray(new NodeTypeManagerListener[listeners.size()]);
  // for (int i = 0; i < la.length; i++) {
  // if (la[i] != null) {
  // la[i].nodeTypeUnregistered(ntName);
  // }
  // }
  // }

  public NodeTypeIterator registerNodeTypes(Collection<NodeTypeValue> values,
                                            int alreadyExistsBehaviour) throws UnsupportedRepositoryOperationException,
                                                                       RepositoryException {

    Collection<NodeTypeData> nts = typesManager.registerNodeTypes(values, alreadyExistsBehaviour);
    EntityCollection types = new EntityCollection();
    for (NodeTypeData ntdata : nts)
      types.add(new NodeTypeImpl(ntdata, typesManager, this, locationFactory, valueFactory));

    return types;
  }

  public void unregisterNodeType(String name) throws UnsupportedRepositoryOperationException,
                                             NoSuchNodeTypeException,
                                             RepositoryException {
    // TODO Auto-generated method stub

  }

  public void unregisterNodeTypes(String[] names) throws UnsupportedRepositoryOperationException,
                                                 NoSuchNodeTypeException,
                                                 RepositoryException {
    // TODO Auto-generated method stub

  }

  private class NodeTypeDataComparator implements Comparator<NodeTypeData> {

    private static final int NT    = 4;

    private static final int MIX   = 3;

    private static final int JCR   = 2;

    private static final int EXO   = 1;

    private static final int OTHER = 0;

    /**
     * @param o1
     * @param o2
     * @return
     */
    public int compare(NodeTypeData o1, NodeTypeData o2) {

      return getIndex(o2.getName().getNamespace()) - getIndex(o1.getName().getNamespace());
    }

    private int getIndex(String nameSpace) {
      if (Constants.NS_NT_URI.equals(nameSpace))
        return NT;
      else if (Constants.NS_MIX_URI.equals(nameSpace))
        return MIX;
      else if (Constants.NS_JCR_URI.equals(nameSpace))
        return JCR;
      else if (Constants.NS_EXO_URI.equals(nameSpace))
        return EXO;
      return OTHER;
    }
  }

}
