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

import java.util.Arrays;

import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SAS. <br/>Date: 25.11.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter
 *         Nedonosko</a>
 * @version $Id$
 */
public class NodeDefinitionData extends ItemDefinitionData {

  protected final InternalQName[] requiredPrimaryTypes;

  protected final InternalQName   defaultPrimaryType;

  protected final boolean         allowsSameNameSiblings;

  public NodeDefinitionData(InternalQName name,
                            InternalQName declaringNodeType,
                            boolean autoCreated,
                            boolean mandatory,
                            int onParentVersion,
                            boolean protectedItem,
                            InternalQName[] requiredPrimaryTypes,
                            InternalQName defaultPrimaryType,
                            boolean allowsSameNameSiblings) {
    super(name, declaringNodeType, autoCreated, mandatory, onParentVersion, protectedItem);
    this.requiredPrimaryTypes = requiredPrimaryTypes;
    this.defaultPrimaryType = defaultPrimaryType;
    this.allowsSameNameSiblings = allowsSameNameSiblings;
  }

  public InternalQName[] getRequiredPrimaryTypes() {
    return requiredPrimaryTypes;
  }

  public InternalQName getDefaultPrimaryType() {
    return defaultPrimaryType;
  }

  public boolean isAllowsSameNameSiblings() {
    return allowsSameNameSiblings;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if ((obj == null) || (obj.getClass() != this.getClass()))
      return false;
    // object must be Test at this point
    NodeDefinitionData test = (NodeDefinitionData) obj;
    return defaultPrimaryType == test.defaultPrimaryType
        && allowsSameNameSiblings == test.allowsSameNameSiblings && super.equals(test)
        && Arrays.equals(this.requiredPrimaryTypes, test.requiredPrimaryTypes);
  }
}
