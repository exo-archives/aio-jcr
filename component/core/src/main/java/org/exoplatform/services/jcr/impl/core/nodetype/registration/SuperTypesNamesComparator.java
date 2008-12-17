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

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class SuperTypesNamesComparator extends
                                      AbstractDefinitionComparator<InternalQName, InternalQName[]> {
  /**
   * Class logger.
   */
  private static final Log LOG = ExoLogger.getLogger(SuperTypesNamesComparator.class);

  public List<ComparationResult<InternalQName[]>> compare(InternalQName[] ancestorDefinition,
                                                          InternalQName[] recipientDefinition) throws RepositoryException {
    List<ComparationResult<InternalQName[]>> result = new ArrayList<ComparationResult<InternalQName[]>>();

    List<InternalQName> sameNames = new ArrayList<InternalQName>();
    List<InternalQName> newNames = new ArrayList<InternalQName>();
    List<InternalQName> removedNames = new ArrayList<InternalQName>();

    findDifferences(ancestorDefinition, recipientDefinition, sameNames, newNames, removedNames);

    result.add(new SuperTypesNamesComparationResult(ModificationType.UNCHANGED,
                                                    sameNames,
                                                    ancestorDefinition,
                                                    recipientDefinition));
    result.add(new SuperTypesNamesComparationResult(ModificationType.ADDED,
                                                    newNames,
                                                    ancestorDefinition,
                                                    recipientDefinition));

    result.add(new SuperTypesNamesComparationResult(ModificationType.REMOVED,
                                                    removedNames,
                                                    ancestorDefinition,
                                                    recipientDefinition));
    return result;
  }
}
