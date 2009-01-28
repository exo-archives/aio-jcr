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
package org.exoplatform.services.jcr.core.nodetype;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.core.query.QueryHandler;

/**
 * Created by The eXo Platform SAS. <br/>Date: 25.11.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter
 *         Nedonosko</a>
 * @version $Id: NodeTypeDataManager.java 24494 2008-12-05 12:26:49Z pnedonosko
 *          $
 */
public interface NodeTypeDataManager {

  void addQueryHandler(QueryHandler queryHandler);

  /**
   * @param nodeName
   * @param nodeTypeNames
   * @return
   */
  NodeDefinitionData findChildNodeDefinition(InternalQName nodeName, InternalQName... nodeTypeNames);

  /**
   * @param nodeName
   * @param primaryNodeType
   * @param mixinTypes
   * @return
   */
  NodeDefinitionData findChildNodeDefinition(InternalQName nodeName,
                                             InternalQName primaryNodeType,
                                             InternalQName[] mixinTypes);

  /**
   * @param typeName
   * @return
   */
  NodeTypeData findNodeType(InternalQName typeName);

  /**
   * @param propertyName
   * @param primaryNodeType
   * @param mixinTypes
   * @return
   */
  PropertyDefinitionDatas findPropertyDefinitions(InternalQName propertyName,
                                                  InternalQName primaryNodeType,
                                                  InternalQName[] mixinTypes);

  /**
   * @param nodeTypeNames
   * @return
   */
  NodeDefinitionData[] getAllChildNodeDefinitions(InternalQName... nodeTypeNames);

  /**
   * Return all NodeTypes.
   * 
   * @return List of NodeTypeData
   * @throws RepositoryException in case of error
   */
  List<NodeTypeData> getAllNodeTypes();

  /**
   * @param nodeTypeNames
   * @return
   */
  PropertyDefinitionData[] getAllPropertyDefinitions(InternalQName... nodeTypeNames);

  /**
   * @param nodeName
   * @param nodeTypeName
   * @param parentTypeName
   * @return
   */
  NodeDefinitionData getChildNodeDefinition(InternalQName nodeName,
                                            InternalQName nodeTypeName,
                                            InternalQName parentTypeName);

  /**
   * @param nodeTypeName
   * @return
   */
  Set<InternalQName> getDescendantNodeTypes(final InternalQName nodeTypeName);

  /**
   * @param primaryNodeType
   * @param mixinTypes
   * @return
   */
  List<ItemDefinitionData> getManadatoryItemDefs(InternalQName primaryNodeType,
                                                 InternalQName[] mixinTypes);

  /**
   * @param propertyName
   * @param nodeTypeNames
   * @return
   */
  PropertyDefinitionDatas getPropertyDefinitions(InternalQName propertyName,
                                                 InternalQName... nodeTypeNames);

  /**
   * @param childNodeTypeName
   * @param parentNodeType
   * @param parentMixinNames
   * @return
   */
  boolean isChildNodePrimaryTypeAllowed(InternalQName childNodeTypeName,
                                        InternalQName parentNodeType,
                                        InternalQName[] parentMixinNames);

  /**
   * @param testTypeName
   * @param typeNames
   * @return
   */
  boolean isNodeType(InternalQName testTypeName, InternalQName... typeNames);

  /**
   * @param testTypeName
   * @param primaryNodeType
   * @param mixinNames
   * @return
   */
  boolean isNodeType(InternalQName testTypeName,
                     InternalQName primaryNodeType,
                     InternalQName[] mixinNames);

  /**
   * @param primaryNodeType
   * @param mixinTypes
   * @return
   */
  boolean isOrderableChildNodesSupported(InternalQName primaryNodeType, InternalQName[] mixinTypes);

  /**
   * Create PlainChangesLog of autocreated items to this node. No checks will be
   * passed for autocreated items.
   * 
   * @throws RepositoryException
   */
  PlainChangesLog makeAutoCreatedItems(NodeData parent,
                                       InternalQName nodeTypeName,
                                       ItemDataConsumer dataManager,
                                       String owner) throws RepositoryException;

  /**
   * @param xml
   * @param alreadyExistsBehaviour
   * @return
   * @throws RepositoryException
   */
  List<NodeTypeData> registerNodeTypes(InputStream xml, int alreadyExistsBehaviour) throws RepositoryException;

  /**
   * @param ntValues
   * @param alreadyExistsBehaviour
   * @return
   * @throws RepositoryException
   */
  List<NodeTypeData> registerNodeTypes(List<NodeTypeValue> ntValues, int alreadyExistsBehaviour) throws RepositoryException;

  /**
   * @param nodeTypeName
   * @throws RepositoryException
   */
  void unregisterNodeType(InternalQName nodeTypeName) throws RepositoryException;
}
