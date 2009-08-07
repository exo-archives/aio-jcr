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
package org.exoplatform.services.jcr.core.nodetype;

import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public interface ExtendedNodeType extends NodeType {

  PropertyDefinitions getPropertyDefinitions(InternalQName name);

  NodeDefinition getChildNodeDefinition(InternalQName name);

  boolean isNodeType(InternalQName nodeTypeQName);

  boolean isChildNodePrimaryTypeAllowed(String typeName);

  NodeDefinition getChildNodeDefinition(String name);

  ArrayList<ItemDefinition> getManadatoryItemDefs();

  PropertyDefinitions getPropertyDefinitions(String name);

  InternalQName getQName();

  void setDeclaredNodeDefs(NodeDefinition[] declaredNodeDefs);

  void setDeclaredPropertyDefs(PropertyDefinition[] declaredPropertyDefs);

  void setDeclaredSupertypes(NodeType[] declaredSupertypes);

  void setMixin(boolean mixin);

  void setName(String name) throws RepositoryException;

  void setOrderableChild(boolean orderableChild);

  void setPrimaryItemName(String primaryItemName);
}
