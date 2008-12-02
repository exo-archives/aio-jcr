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

import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitions;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 02.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: NodeTypeImpl.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class NodeTypeImpl implements ExtendedNodeType {

  protected static final Log          LOG = ExoLogger.getLogger("jcr.NodeTypeImpl");

  protected final NodeTypeData        data;

  protected final NodeTypeDataManager manager;

  protected final LocationFactory     locationFactory;

  public NodeTypeImpl(NodeTypeData data,
                      NodeTypeDataManager manager,
                      LocationFactory locationFactory) {
    this.data = data;
    this.manager = manager;
    this.locationFactory = locationFactory;
  }

  // JSR-170 stuff ==========================

  public boolean canAddChildNode(String childNodeName) {

    try {
      InternalQName cname = locationFactory.parseJCRName(childNodeName).getInternalName();
      
      NodeDefinitionData cdef = manager.findChildNodeDefinition(cname, data.getName(), null);
      return cdef != null;
    } catch (RepositoryException e) {
      LOG.error("Child node name is wrong " + e);
      return false;
    }
  }

  public boolean canAddChildNode(String childNodeName, String nodeTypeName) {
    try {
      InternalQName cname = locationFactory.parseJCRName(childNodeName).getInternalName();
      
      NodeDefinitionData cdef = manager.findChildNodeDefinition(cname, data.getName(), null);
      return cdef != null;
    } catch (RepositoryException e) {
      LOG.error("Child node name is wrong " + e);
      return false;
    }
  }

  public boolean canRemoveItem(String itemName) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean canSetProperty(String propertyName, Value value) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean canSetProperty(String propertyName, Value[] values) {
    // TODO Auto-generated method stub
    return false;
  }

  public NodeDefinition[] getChildNodeDefinitions() {

    // NodeDefinitionData data = manager.;

    // if (data != null) {

    // String name,
    // NodeType declaringNodeType,
    // NodeType[] requiredNodeTypes,
    // NodeType defaultNodeType,
    // boolean autoCreate,
    // boolean mandatory,
    // int onVersion,
    // boolean readOnly,
    // boolean multiple
    // NodeDefinitionImpl impl = new NodeDefinitionImpl();

    return null;
  }

  public NodeDefinition[] getDeclaredChildNodeDefinitions() {
    // TODO Auto-generated method stub
    return null;
  }

  public PropertyDefinition[] getDeclaredPropertyDefinitions() {
    // TODO Auto-generated method stub
    return null;
  }

  public NodeType[] getDeclaredSupertypes() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getName() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getPrimaryItemName() {
    // TODO Auto-generated method stub
    return null;
  }

  public PropertyDefinition[] getPropertyDefinitions() {
    // TODO Auto-generated method stub
    return null;
  }

  public NodeType[] getSupertypes() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasOrderableChildNodes() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isMixin() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isNodeType(String nodeTypeName) {
    // TODO Auto-generated method stub
    return false;
  }

  // eXo stuff =====

  public NodeDefinition getChildNodeDefinition(InternalQName name) {
    return null;
  }

  public NodeDefinition getChildNodeDefinition(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public ArrayList<ItemDefinition> getManadatoryItemDefs() {
    // TODO Auto-generated method stub
    return null;
  }

  public PropertyDefinitions getPropertyDefinitions(InternalQName name) {
    // TODO Auto-generated method stub
    return null;
  }

  public PropertyDefinitions getPropertyDefinitions(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  public InternalQName getQName() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isChildNodePrimaryTypeAllowed(String typeName) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isNodeType(InternalQName nodeTypeQName) {
    // TODO Auto-generated method stub
    return false;
  }

}
