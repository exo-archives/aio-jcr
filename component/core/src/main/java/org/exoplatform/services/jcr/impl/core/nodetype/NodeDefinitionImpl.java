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

import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: NodeDefinitionImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class NodeDefinitionImpl extends ItemDefinitionImpl implements NodeDefinition {

  private NodeType   defaultNodeType;

  private NodeType[] requiredNodeTypes;

  private boolean    multiple;

  public NodeDefinitionImpl(String name,
      NodeType declaringNodeType,
      NodeType[] requiredNodeTypes,
      NodeType defaultNodeType,
      boolean autoCreate,
      boolean mandatory,
      int onVersion,
      boolean readOnly,
      boolean multiple,
      InternalQName qName) {

    super(name, declaringNodeType, autoCreate, onVersion, readOnly, mandatory, qName);

    this.declaringNodeType = declaringNodeType;
    this.requiredNodeTypes = requiredNodeTypes;
    this.defaultNodeType = defaultNodeType;
    this.multiple = multiple;

  }

  public NodeDefinitionImpl(String name, InternalQName qName) {
    super(name, qName);
  }

  /**
   * @see javax.jcr.nodetype.NodeDefinition#getRequiredPrimaryTypes
   */
  public NodeType[] getRequiredPrimaryTypes() {

    return requiredNodeTypes;
  }

  /**
   * @see javax.jcr.nodetype.NodeDefinition#getDefaultPrimaryType
   */
  public NodeType getDefaultPrimaryType() {
    return defaultNodeType;
  }

  /**
   * @see javax.jcr.nodetype.NodeDefinition#allowSameNameSibs
   */
  public boolean allowsSameNameSiblings() {
    return multiple;
  }

  /**
   * @param defaultNodeType The defaultNodeType to set.
   */
  public void setDefaultNodeType(NodeType defaultNodeType) {
    this.defaultNodeType = defaultNodeType;
  }

  /**
   * @param multiple The multiple to set.
   */
  public void setMultiple(boolean multiple) {
    this.multiple = multiple;
  }

  /**
   * @param requiredNodeTypes The requiredNodeTypes to set.
   */
  public void setRequiredNodeTypes(NodeType[] requiredNodeTypes) {
    this.requiredNodeTypes = requiredNodeTypes;
  }

  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (!(obj instanceof NodeDefinitionImpl))
      return false;
    if (this.getName() == null)
      return ((NodeDefinitionImpl) obj).getName() == null;
    return this.getName().equals(((NodeDefinitionImpl) obj).getName());
  }
}
