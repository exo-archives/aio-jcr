/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionData;
import org.exoplatform.services.jcr.datamodel.InternalQName;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class NodeTypeDataDefinitionComparator implements DefinitionComparator<NodeTypeData> {

  private final PropertyDefinitionDataDefinitionComparator propertyDefinitionDataDefinitionComparator;

  private final NodeDefinitionDataDefinitionComparator     nodeDefinitionDataDefinitionComparator;

  private final SuperTypesNamesComparator                  superTypesNamesComparator;

  /**
   * 
   */
  public NodeTypeDataDefinitionComparator() {
    propertyDefinitionDataDefinitionComparator = new PropertyDefinitionDataDefinitionComparator();
    nodeDefinitionDataDefinitionComparator = new NodeDefinitionDataDefinitionComparator();
    superTypesNamesComparator = new SuperTypesNamesComparator();
  }

  public List<ComparationResult<NodeTypeData>> compare(NodeTypeData ancestorDefinition,
                                                       NodeTypeData recipientDefinition) throws RepositoryException {

    List<ComparationResult<InternalQName[]>> declaredSupertypeNamesChanges = superTypesNamesComparator.compare(ancestorDefinition.getDeclaredSupertypeNames(),
                                                                                                               recipientDefinition.getDeclaredSupertypeNames());
    List<ComparationResult<PropertyDefinitionData[]>> declaredPropertyDefinitionsChanges = propertyDefinitionDataDefinitionComparator.compare(ancestorDefinition.getDeclaredPropertyDefinitions(),
                                                                                                                                              recipientDefinition.getDeclaredPropertyDefinitions());
    List<ComparationResult<NodeDefinitionData[]>> declaredChildNodeDefinitionsChanges = nodeDefinitionDataDefinitionComparator.compare(ancestorDefinition.getDeclaredChildNodeDefinitions(),
                                                                                                                                       recipientDefinition.getDeclaredChildNodeDefinitions());
    List<ComparationResult<NodeTypeData>> result = new ArrayList<ComparationResult<NodeTypeData>>();

    result.add(new NodeTypeDataDefinitionComparationResult(ModificationType.CHANGED,
                                                           ancestorDefinition,
                                                           recipientDefinition,
                                                           declaredSupertypeNamesChanges,
                                                           declaredPropertyDefinitionsChanges,
                                                           declaredChildNodeDefinitionsChanges));

    return result;

  }

  private class DefinitionComparator<X> {
    void findDifferences(X[] ancestorDefinition,
                         X[] recipientDefinition,
                         List<X> newDefinitions,
                         List<X> sameDefinitions,
                         List<X> removedDfinitions) {
      // same and new
      for (int i = 0; i < recipientDefinition.length; i++) {
        boolean isSame = false;
        boolean isNew = true;
        for (int j = 0; j < ancestorDefinition.length && !isSame; j++) {
          if (ancestorDefinition[j].equals(recipientDefinition[i])) {
            sameDefinitions.add(recipientDefinition[i]);
            isNew = false;
            isSame = true;
          }
        }
        if (isNew) {
          newDefinitions.add(recipientDefinition[i]);
        }
      }
      // removed
      for (int i = 0; i < ancestorDefinition.length; i++) {
        boolean isRemoved = true;
        for (int j = 0; j < recipientDefinition.length && isRemoved; j++) {
          if (ancestorDefinition[j].equals(recipientDefinition[i])) {
            isRemoved = false;
          }
        }
        if (isRemoved)
          removedDfinitions.add(ancestorDefinition[i]);
      }
    };
  }
}
