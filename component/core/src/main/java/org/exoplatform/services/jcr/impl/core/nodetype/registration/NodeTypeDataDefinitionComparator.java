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

import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class NodeTypeDataDefinitionComparator implements DefinitionComparator<NodeTypeData> {

  private final PropertyDefinitionDataDefinitionComparator propertyDefinitionDataDefinitionComparator;

  private final NodeDefinitionDataDefinitionComparator     nodeDefinitionDataDefinitionComparator;

  /**
   * 
   */
  public NodeTypeDataDefinitionComparator() {
    propertyDefinitionDataDefinitionComparator = new PropertyDefinitionDataDefinitionComparator();
    nodeDefinitionDataDefinitionComparator = new NodeDefinitionDataDefinitionComparator();
  }

  public List<ComparationResult<NodeTypeData>> compare(NodeTypeData ancestorDefinition,
                                                       NodeTypeData recipientDefinition) throws RepositoryException {
    // if (!ancestorDefinition.getName().equals(recipientDefinition.getName()))
    // throw new
    // RepositoryException("Unsuported changes. Names can't be different");

    return null;

  }
}
