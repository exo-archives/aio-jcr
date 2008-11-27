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

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SAS.
 * 
 * Define base abstraction for NodeType data used in core.
 * 
 * <br/>Date: 25.11.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class NodeTypeData {

  protected InternalQName   name;

  protected InternalQName   primaryItemName;
  
  protected InternalQName[] declaredSupertypeNames;

  protected PropertyDefinitionData[] declaredPropertyDefinitions;
  
  protected NodeDefinitionData[] declaredChildNodeDefinitions;

  protected boolean hasOrderableChildNodes;

  protected boolean mixin;

  public NodeTypeData(InternalQName name,
                      InternalQName primaryItemName,
                      boolean mixin,
                      boolean hasOrderableChildNodes,
                      InternalQName[] declaredSupertypeNames,
                      PropertyDefinitionData[] declaredPropertyDefinitions,
                      NodeDefinitionData[] declaredChildNodeDefinitions) {

    this.name = name;
    this.primaryItemName = primaryItemName;
    this.mixin = mixin;
    this.hasOrderableChildNodes = hasOrderableChildNodes;
    this.declaredSupertypeNames = declaredSupertypeNames;
    this.declaredPropertyDefinitions = declaredPropertyDefinitions;
    this.declaredChildNodeDefinitions = declaredChildNodeDefinitions;
  }

  public NodeDefinitionData[] getDeclaredChildNodeDefinitions() {
    return declaredChildNodeDefinitions;
  }

  public PropertyDefinitionData[] getDeclaredPropertyDefinitions() {
    return declaredPropertyDefinitions;
  }

  public InternalQName[] getDeclaredSupertypeNames() {
    return declaredSupertypeNames;
  }

  public InternalQName getPrimaryItemName() {
    return primaryItemName;
  }

  public InternalQName getName() {
    return name;
  }

  public boolean hasOrderableChildNodes() {
    return hasOrderableChildNodes;
  }

  public boolean isMixin() {
    return mixin;
  }

}
