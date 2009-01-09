/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.core.nodetype.registration;

import org.exoplatform.services.jcr.core.nodetype.ItemDefinitionData;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class DefinitionComparator {

  public DefinitionComparator() {
    super();
  }

  protected boolean isResidualMatch(InternalQName itemName, ItemDefinitionData[] recipientDefinition) {
    boolean containsResidual = false;
    for (int i = 0; i < recipientDefinition.length; i++) {
      if (itemName.equals(recipientDefinition[i].getName()))
        return false;
      else if (Constants.JCR_ANY_NAME.equals(recipientDefinition[i].getName()))
        containsResidual = true;
    }
    return containsResidual;
  }

}
