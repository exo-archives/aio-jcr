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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeDataManagerImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class SuperTypeNamesComparator {
  /**
   * Class logger.
   */
  private static final Log              LOG = ExoLogger.getLogger(SuperTypeNamesComparator.class);

  protected final DataManager           persister;

  private final NodeTypeDataManagerImpl nodeTypeDataManager;

  /**
   * @param locationFactory
   * @param nodeTypeDataManager
   * @param persister
   */
  public SuperTypeNamesComparator(NodeTypeDataManagerImpl nodeTypeDataManager, DataManager persister) {
    super();
    this.nodeTypeDataManager = nodeTypeDataManager;
    this.persister = persister;
  }

  public PlainChangesLog compare(NodeTypeData registeredNodeType,
                                 InternalQName[] ancestorSuperTypeNames,
                                 InternalQName[] recipientSuperTypeNames) throws ConstraintViolationException,
                                                                         RepositoryException {
    List<InternalQName> sameDefinitionData = new ArrayList<InternalQName>();
    List<InternalQName> newDefinitionData = new ArrayList<InternalQName>();
    List<InternalQName> removedDefinitionData = new ArrayList<InternalQName>();

    init(ancestorSuperTypeNames,
         recipientSuperTypeNames,
         sameDefinitionData,
         newDefinitionData,
         removedDefinitionData);
    PlainChangesLogImpl changesLog = new PlainChangesLogImpl();
    Set<String> nodes = nodeTypeDataManager.getNodes(registeredNodeType.getName());
    validateAdded(registeredNodeType, newDefinitionData, nodes);
    return changesLog;
  }

  private void validateAdded(NodeTypeData registeredNodeType,
                             List<InternalQName> newDefinitionData,
                             Set<String> nodes) {

  }

  private void init(InternalQName[] ancestorSuperTypeNames,
                    InternalQName[] recipientSuperTypeNames,
                    List<InternalQName> sameDefinitionData,
                    List<InternalQName> newDefinitionData,
                    List<InternalQName> removedDefinitionData) {
    for (int i = 0; i < recipientSuperTypeNames.length; i++) {
      boolean isNew = true;
      for (int j = 0; j < recipientSuperTypeNames.length; j++) {
        if (recipientSuperTypeNames[i].equals(ancestorSuperTypeNames[j])) {
          isNew = false;
          sameDefinitionData.add(recipientSuperTypeNames[i]);
        }
      }
      if (isNew)
        newDefinitionData.add(recipientSuperTypeNames[i]);
    }
    for (int i = 0; i < ancestorSuperTypeNames.length; i++) {
      boolean isRemoved = true;
      for (int j = 0; j < recipientSuperTypeNames.length && isRemoved; j++) {
        if (recipientSuperTypeNames[i].equals(ancestorSuperTypeNames[j])) {
          isRemoved = false;
          break;
        }
      }
      if (isRemoved)
        removedDefinitionData.add(ancestorSuperTypeNames[i]);
    }
  }
}
