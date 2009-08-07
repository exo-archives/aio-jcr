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

import java.util.List;

/**
 * Created by The eXo Platform SAS.<br/>
 * NodeDefinition value object
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id$
 */

public class NodeDefinitionValue extends ItemDefinitionValue {

  private String       defaultNodeTypeName;

  private List<String> requiredNodeTypeNames;

  private boolean      sameNameSiblings;

  public NodeDefinitionValue() {
  }

  /**
   * @return Returns the defaultNodeTypeName.
   */
  public String getDefaultNodeTypeName() {
    return defaultNodeTypeName;
  }

  /**
   * @param defaultNodeTypeName
   *          The defaultNodeTypeName to set.
   */
  public void setDefaultNodeTypeName(String defaultNodeTypeName) {
    this.defaultNodeTypeName = defaultNodeTypeName;
  }

  /**
   * @return Returns the sameNameSiblings.
   */
  public boolean isSameNameSiblings() {
    return sameNameSiblings;
  }

  /**
   * @param sameNameSiblings
   *          The sameNameSiblings to set.
   */
  public void setSameNameSiblings(boolean multiple) {
    this.sameNameSiblings = multiple;
  }

  /**
   * @return Returns the requiredNodeTypeNames.
   */
  public List<String> getRequiredNodeTypeNames() {
    return requiredNodeTypeNames;
  }

  /**
   * @param requiredNodeTypeNames
   *          The requiredNodeTypeNames to set.
   */
  public void setRequiredNodeTypeNames(List<String> requiredNodeTypeNames) {
    this.requiredNodeTypeNames = requiredNodeTypeNames;
  }
}
