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

import org.exoplatform.commons.utils.QName;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 25.11.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class ItemDefinitionData {

  public static final String RESIDUAL_SET = "*";

  public boolean isResidualSet() {
    // TODO
    return this.getName().getName().equals(RESIDUAL_SET);
  }

  public QName getName() {
    return null;
  }

  public QName[] getDeclaringNodeType() {
    return null;
  }

  public boolean isAutoCreated() {
    return false;
  }

  public boolean isMandatory() {
    return false;
  }

  public int getOnParentVersion() {
    return 0;
  }

  public boolean isProtected() {
    return false;
  }

}
