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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.ValueFactory;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
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

  public static final String          NODETYPES_ROOT = "/jcr:system/jcr:nodetypes";

  protected final ValueFactory        valueFactory;

  protected final LocationFactory     locationFactory;

  protected final NodeTypeDataManager typesManager;

  public NodeTypeManagerImpl(LocationFactory locationFactory,
                             ValueFactoryImpl valueFactory,
                             NodeTypeDataManager typesManager) {
    this.valueFactory = valueFactory;
    this.locationFactory = locationFactory;
    this.typesManager = typesManager;

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

  // JSR-170 stuff ================================
  // Extended stuff ================================

  public ExtendedNodeType findNodeType(InternalQName nodeTypeName) throws NoSuchNodeTypeException,
                                                                  RepositoryException {

    NodeTypeData ntdata = typesManager.findNodeType(nodeTypeName);
    if (ntdata != null)
      return new NodeTypeImpl(ntdata, typesManager, this, locationFactory, valueFactory);

    throw new NoSuchNodeTypeException("Nodetype not found " + nodeTypeName.getAsString());
  }

  /**
   * {@inheritDoc}
   */
  public void registerNodeType(ExtendedNodeType nodeType, int alreadyExistsBehaviour) throws RepositoryException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Deprecated
  // TODO remove me
  public void registerNodeType(Class<ExtendedNodeType> nodeTypeType, int alreadyExistsBehaviour) throws RepositoryException,
                                                                                                InstantiationException {

    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  public void registerNodeType(NodeTypeValue nodeTypeValue, int alreadyExistsBehaviour) throws RepositoryException {

    typesManager.registerNodeType(nodeTypeValue, alreadyExistsBehaviour);
  }

  /**
   * {@inheritDoc}
   */
  public void registerNodeTypes(InputStream xml, int alreadyExistsBehaviour) throws RepositoryException {

    typesManager.registerNodeTypes(xml, alreadyExistsBehaviour);
  }

  /**
   * {@inheritDoc}
   */
  public NodeTypeValue getNodeTypeValue(String ntName) throws NoSuchNodeTypeException,
                                                      RepositoryException {
    throw new RepositoryException("Unsupported operation");
  }

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
