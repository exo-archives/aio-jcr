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

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: NodeDefinitionImpl.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class NodeDefinitionImpl extends ItemDefinitionImpl implements NodeDefinition {

  private final NodeType   defaultNodeType;

  private final NodeType[] requiredNodeTypes;

  private final boolean    multiple;

  public NodeDefinitionImpl(String name,
                            NodeType declaringNodeType,
                            NodeType[] requiredNodeTypes,
                            NodeType defaultNodeType,
                            boolean autoCreate,
                            boolean mandatory,
                            int onVersion,
                            boolean readOnly,
                            boolean multiple) {

    super(name, declaringNodeType, autoCreate, onVersion, readOnly, mandatory);

    this.requiredNodeTypes = requiredNodeTypes;
    this.defaultNodeType = defaultNodeType;
    this.multiple = multiple;

    int hk = 31 * this.hashCode + requiredNodeTypes.hashCode();
    if (defaultNodeType != null)
      hk = 31 * hk + defaultNodeType.hashCode();
    this.hashCode = 31 * hk + (multiple ? 0 : 1);
  }

  /**
   * {@inheritDoc}
   */
  public NodeType[] getRequiredPrimaryTypes() {

    return requiredNodeTypes;
  }

  /**
   * {@inheritDoc}
   */
  public NodeType getDefaultPrimaryType() {
    return defaultNodeType;
  }

  /**
   * {@inheritDoc}
   */
  public boolean allowsSameNameSiblings() {
    return multiple;
  }

  /**
   * {@inheritDoc}
   */
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
