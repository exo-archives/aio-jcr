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

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionData;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class NodeTypeDataDefinitionComparationResult extends ComparationResult<NodeTypeData> {
  /**
   * Class logger.
   */
  private static final Log                                        LOG = ExoLogger.getLogger(NodeTypeDataDefinitionComparationResult.class);

  private final List<ComparationResult<PropertyDefinitionData[]>> declaredChildNodeDefinitionsChanges;

  private final List<ComparationResult<NodeDefinitionData[]>>     declaredPropertyDefinitionsChanges;

  private final List<ComparationResult<InternalQName[]>>          declaredSupertypeNamesChanges;

  public NodeTypeDataDefinitionComparationResult(ModificationType modificationType,
                                                 NodeTypeData ancestorDefinition,
                                                 NodeTypeData recipientDefinition,
                                                 List<ComparationResult<InternalQName[]>> declaredSupertypeNamesChanges,
                                                 List<ComparationResult<PropertyDefinitionData[]>> declaredChildNodeDefinitionsChanges,
                                                 List<ComparationResult<NodeDefinitionData[]>> declaredPropertyDefinitionsChanges) {
    super(modificationType, ancestorDefinition, recipientDefinition);
    this.declaredSupertypeNamesChanges = declaredSupertypeNamesChanges;
    this.declaredChildNodeDefinitionsChanges = declaredChildNodeDefinitionsChanges;
    this.declaredPropertyDefinitionsChanges = declaredPropertyDefinitionsChanges;

  }

  public boolean isNameChanged() {
    return !getAncestorDefinition().getName().equals(getRecipientDefinition().getName());
  };

  public boolean isPrimaryItemNameChanged() {
    return !getAncestorDefinition().getPrimaryItemName()
                                   .equals(getRecipientDefinition().getPrimaryItemName());
  }

  public boolean isDeclaredChildNodeDefinitionsChanged() {
    return declaredChildNodeDefinitionsChanges != null
        && declaredChildNodeDefinitionsChanges.size() > 0;
  }

  public boolean isDeclaredPropertyDefinitionsDefinitionsChanged() {
    return declaredPropertyDefinitionsChanges != null
        && declaredPropertyDefinitionsChanges.size() > 0;
  }

  public boolean isDeclaredSupertypeNamesChanged() {
    return declaredSupertypeNamesChanges != null && declaredSupertypeNamesChanges.size() > 0;
  }

  public boolean isOrderableChildNodesChanged() {
    return getAncestorDefinition().hasOrderableChildNodes() != getRecipientDefinition().hasOrderableChildNodes();
  }

  public boolean isMixinChanged() {
    return getAncestorDefinition().isMixin() != getRecipientDefinition().isMixin();
  };

}
