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

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SAS. <br/>Date: 25.11.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter
 *         Nedonosko</a>
 * @version $Id: NodeTypeDataManager.java 24422 2008-12-04 23:59:42Z pnedonosko
 *          $
 */
public interface NodeTypeDataManager {

  /**
   * Return all NodeTypes.
   * 
   * @return List of NodeTypeData
   * @throws RepositoryException in case of error
   */
  Collection<NodeTypeData> getAllNodeTypes();

  NodeTypeData findNodeType(InternalQName typeName);

  boolean isOrderableChildNodesSupported(InternalQName primaryNodeType, InternalQName[] mixinTypes);

  NodeDefinitionData getChildNodeDefinition(InternalQName nodeName,
                                            InternalQName nodeTypeName,
                                            InternalQName parentTypeName);

  NodeDefinitionData findChildNodeDefinition(InternalQName nodeName, InternalQName... nodeTypeNames);

  NodeDefinitionData findChildNodeDefinition(InternalQName nodeName,
                                             InternalQName primaryNodeType,
                                             InternalQName[] mixinTypes);

  // NodeDefinitionData findNodeDefinition(InternalQName nodeName,
  // List<NodeTypeData> typesList);

  PropertyDefinitionDatas getPropertyDefinitions(InternalQName propertyName,
                                                 InternalQName... nodeTypeNames);

  PropertyDefinitionDatas findPropertyDefinitions(InternalQName propertyName,
                                                  InternalQName primaryNodeType,
                                                  InternalQName[] mixinTypes);

  // PropertyDefinitionDatas findPropertyDefinitions(InternalQName propertyName,
  // List<NodeTypeData> typesList);

  boolean isNodeType(InternalQName testTypeName,
                     InternalQName primaryNodeType,
                     InternalQName[] mixinNames);

  boolean isNodeType(InternalQName testTypeName, InternalQName... typeNames);

  void registerNodeType(NodeTypeData nodeType, int alreadyExistsBehaviour) throws RepositoryException;

  void registerNodeTypes(Collection<NodeTypeData> nodeTypes, int alreadyExistsBehaviour) throws RepositoryException;

  NodeTypeData registerNodeType(NodeTypeValue ntvalue, int alreadyExistsBehaviour) throws RepositoryException;

  Collection<NodeTypeData> registerNodeTypes(Collection<NodeTypeValue> ntValues,
                                             int alreadyExistsBehaviour) throws RepositoryException;

  Collection<NodeTypeData> registerNodeTypes(InputStream xml, int alreadyExistsBehaviour) throws RepositoryException;

  // Proposed methods

  // boolean isChildNodePrimaryTypeAllowed(InternalQName parentNodeType,
  // InternalQName[] parentMixinNames,
  // InternalQName childNodeTypeName) throws NoSuchNodeTypeException,
  // RepositoryException;
  // or

  // boolean isChildNodePrimaryTypeAllowed(InternalQName childNodeTypeName,
  // InternalQName... typeNames) throws NoSuchNodeTypeException,
  // RepositoryException;
  //   

}
