/**
 * 
 */
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
package org.exoplatform.services.jcr.ext.replication.async.merge;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.exoplatform.services.jcr.core.nodetype.ItemDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.core.query.QueryHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TesterntManager.java 111 2008-11-11 11:11:11Z $
 */
public class TesterntManager implements NodeTypeDataManager {

  public TesterntManager() {
  }

  public PropertyDefinitionDatas findPropertyDefinitions(InternalQName propertyName,
                                                         InternalQName primaryNodeType,
                                                         InternalQName[] mixinTypes) {

    return new PropertyDefinitionDatas();
  }

  public void addQueryHandler(QueryHandler queryHandler) {
    // TODO Auto-generated method stub

  }

  public NodeDefinitionData findChildNodeDefinition(InternalQName nodeName,
                                                    InternalQName... nodeTypeNames) {
    // TODO Auto-generated method stub
    return null;
  }

  public NodeDefinitionData findChildNodeDefinition(InternalQName nodeName,
                                                    InternalQName primaryNodeType,
                                                    InternalQName[] mixinTypes) {
    // TODO Auto-generated method stub
    return null;
  }

  public NodeTypeData findNodeType(InternalQName typeName) {
    // TODO Auto-generated method stub
    return null;
  }

  public NodeDefinitionData[] getAllChildNodeDefinitions(InternalQName... nodeTypeNames) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<NodeTypeData> getAllNodeTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  public PropertyDefinitionData[] getAllPropertyDefinitions(InternalQName... nodeTypeNames) {
    // TODO Auto-generated method stub
    return null;
  }

  public NodeDefinitionData getChildNodeDefinition(InternalQName nodeName,
                                                   InternalQName nodeTypeName,
                                                   InternalQName parentTypeName) {
    // TODO Auto-generated method stub
    return null;
  }

  public List<ItemDefinitionData> getManadatoryItemDefs(InternalQName primaryNodeType,
                                                        InternalQName[] mixinTypes) {
    // TODO Auto-generated method stub
    return null;
  }

  public PropertyDefinitionDatas getPropertyDefinitions(InternalQName propertyName,
                                                        InternalQName... nodeTypeNames) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isChildNodePrimaryTypeAllowed(InternalQName childNodeTypeName,
                                               InternalQName parentNodeType,
                                               InternalQName[] parentMixinNames) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isNodeType(InternalQName testTypeName,
                            InternalQName primaryNodeType,
                            InternalQName[] mixinNames) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isNodeType(InternalQName testTypeName, InternalQName... typeNames) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isOrderableChildNodesSupported(InternalQName primaryNodeType,
                                                InternalQName[] mixinTypes) {
    // TODO Auto-generated method stub
    return false;
  }

  public void registerNodeType(NodeTypeData nodeType, int alreadyExistsBehaviour) throws RepositoryException {
    // TODO Auto-generated method stub

  }

  public NodeTypeData registerNodeType(NodeTypeValue ntvalue, int alreadyExistsBehaviour) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public void registerNodeTypes(Collection<NodeTypeData> nodeTypes, int alreadyExistsBehaviour) throws RepositoryException {
    // TODO Auto-generated method stub

  }

  public List<NodeTypeData> registerNodeTypes(Collection<NodeTypeValue> nodeTypes,
                                              int alreadyExistsBehaviour) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<NodeTypeData> registerNodeTypes(InputStream xml, int alreadyExistsBehaviour) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public void unregisterNodeType(InternalQName nodeTypeName) throws ConstraintViolationException {
    // TODO Auto-generated method stub

  }

}
