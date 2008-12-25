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
import java.util.Collection;
import java.util.List;

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

  /**
   * Return all NodeTypes.
   * 
   * @return List of NodeTypeData
   * @throws RepositoryException in case of error
   */
  List<NodeTypeData> getAllNodeTypes();

  NodeTypeData findNodeType(InternalQName typeName);

  boolean isOrderableChildNodesSupported(InternalQName primaryNodeType, InternalQName[] mixinTypes);

  List<ItemDefinitionData> getManadatoryItemDefs(InternalQName primaryNodeType,
                                                 InternalQName[] mixinTypes);

  NodeDefinitionData getChildNodeDefinition(InternalQName nodeName,
                                            InternalQName nodeTypeName,
                                            InternalQName parentTypeName);

  NodeDefinitionData[] getAllChildNodeDefinitions(InternalQName... nodeTypeNames);

  NodeDefinitionData findChildNodeDefinition(InternalQName nodeName, InternalQName... nodeTypeNames);

  NodeDefinitionData findChildNodeDefinition(InternalQName nodeName,
                                             InternalQName primaryNodeType,
                                             InternalQName[] mixinTypes);

  PropertyDefinitionDatas getPropertyDefinitions(InternalQName propertyName,
                                                 InternalQName... nodeTypeNames);

  PropertyDefinitionData[] getAllPropertyDefinitions(InternalQName... nodeTypeNames);

  PropertyDefinitionDatas findPropertyDefinitions(InternalQName propertyName,
                                                  InternalQName primaryNodeType,
                                                  InternalQName[] mixinTypes);

  boolean isNodeType(InternalQName testTypeName,
                     InternalQName primaryNodeType,
                     InternalQName[] mixinNames);

  boolean isNodeType(InternalQName testTypeName, InternalQName... typeNames);

  void registerNodeType(NodeTypeData nodeType, int alreadyExistsBehaviour) throws RepositoryException;

  void registerNodeTypes(Collection<NodeTypeData> nodeTypes, int alreadyExistsBehaviour) throws RepositoryException;

  NodeTypeData registerNodeType(NodeTypeValue ntvalue, int alreadyExistsBehaviour) throws RepositoryException;

  public void unregisterNodeType(InternalQName nodeTypeName) throws RepositoryException;

  Collection<NodeTypeData> registerNodeTypes(Collection<NodeTypeValue> ntValues,
                                             int alreadyExistsBehaviour) throws RepositoryException;

  Collection<NodeTypeData> registerNodeTypes(InputStream xml, int alreadyExistsBehaviour) throws RepositoryException;

  // Proposed methods

  boolean isChildNodePrimaryTypeAllowed(InternalQName childNodeTypeName,
                                        InternalQName parentNodeType,
                                        InternalQName[] parentMixinNames);

  // query

  public void addQueryHandler(QueryHandler queryHandler);

  /**
   * Create PlainChangesLog of autocreated items to this node. No checks will be
   * passed for autocreated items.
   * 
   * @throws RepositoryException
   */
  public PlainChangesLog makeAutoCreatedItems(NodeData parent,
                                              InternalQName nodeTypeName,
                                              ItemDataConsumer dataManager,
                                              String owner) throws RepositoryException;
}
