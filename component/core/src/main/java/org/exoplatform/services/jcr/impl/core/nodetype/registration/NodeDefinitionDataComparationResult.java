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

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class NodeDefinitionDataComparationResult extends ComparationResult<NodeDefinitionData> {
  public NodeDefinitionDataComparationResult(ModificationType modificationType,
                                             NodeDefinitionData ancestorDefinition,
                                             NodeDefinitionData recipientDefinition) {
    super(modificationType, ancestorDefinition, recipientDefinition);
  }

  /**
   * Class logger.
   */
  private static final Log LOG = ExoLogger.getLogger(NodeDefinitionDataComparationResult.class);

}
