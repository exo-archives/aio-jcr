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
 * <br/>Date: 25.11.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class PropertyDefinitionData extends ItemDefinitionData {

  protected final int      requiredType;

  protected final String[] valueConstraints;

  protected final String[] defaultValues;

  protected final boolean  multiple;

  public PropertyDefinitionData(InternalQName name,
                                InternalQName declaringNodeType,
                                boolean autoCreated,
                                boolean mandatory,
                                int onParentVersion,
                                boolean protectedItem,
                                int requiredType,
                                String[] valueConstraints,
                                String[] defaultValues,
                                boolean multiple) {
    super(name, declaringNodeType, autoCreated, mandatory, onParentVersion, protectedItem);
    this.requiredType = requiredType;
    this.valueConstraints = valueConstraints;
    this.defaultValues = defaultValues;
    this.multiple = multiple;
  }

  public int getRequiredType() {
    return requiredType;
  }

  public String[] getValueConstraints() {
    return valueConstraints;
  }

  public String[] getDefaultValues() {
    return defaultValues;
  }

  public boolean isMultiple() {
    return multiple;
  }

}
