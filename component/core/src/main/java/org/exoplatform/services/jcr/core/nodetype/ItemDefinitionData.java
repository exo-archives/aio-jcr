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
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS. <br/>Date: 25.11.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter
 *         Nedonosko</a>
 * @version $Id$
 */
public class ItemDefinitionData {

  protected final InternalQName name;

  protected final InternalQName declaringNodeType;

  protected final boolean       autoCreated;

  protected final boolean       mandatory;

  protected final int           onParentVersion;

  protected final boolean       protectedItem;

  public ItemDefinitionData(InternalQName name,
                            InternalQName declaringNodeType,
                            boolean autoCreated,
                            boolean mandatory,
                            int onParentVersion,
                            boolean protectedItem) {
    this.name = name;
    this.declaringNodeType = declaringNodeType;
    this.autoCreated = autoCreated;
    this.mandatory = mandatory;
    this.onParentVersion = onParentVersion;
    this.protectedItem = protectedItem;
  }

  public boolean isResidualSet() {
    return this.getName().equals(Constants.JCR_ANY_NAME);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if ((obj == null) || (obj.getClass() != this.getClass()))
      return false;
    // object must be Test at this point
    ItemDefinitionData test = (ItemDefinitionData) obj;
    return name == test.name && declaringNodeType == test.declaringNodeType
        && autoCreated == test.autoCreated && mandatory == test.mandatory
        && onParentVersion == test.onParentVersion && protectedItem == test.protectedItem;
  }

  public InternalQName getName() {
    return name;
  }

  public InternalQName getDeclaringNodeType() {
    return declaringNodeType;
  }

  public boolean isAutoCreated() {
    return autoCreated;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public int getOnParentVersion() {
    return onParentVersion;
  }

  public boolean isProtected() {
    return protectedItem;
  }

}
