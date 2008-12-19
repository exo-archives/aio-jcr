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
import java.util.Set;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.ExtendedPropertyType;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionData;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeDataManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class PropertyDefinitionComparator {
  /**
   * Class logger.
   */
  private static final Log LOG = ExoLogger.getLogger(PropertyDefinitionComparator.class);

  /**
   * @param nodeTypeDataManager
   * @param persister
   */
  public PropertyDefinitionComparator(NodeTypeDataManagerImpl nodeTypeDataManager,
                                      DataManager persister) {
    super();
    this.nodeTypeDataManager = nodeTypeDataManager;
    this.persister = persister;
  }

  protected final DataManager           persister;

  private final NodeTypeDataManagerImpl nodeTypeDataManager;

  public PlainChangesLog processPropertyDefinitionChanges(NodeTypeData registeredNodeType,
                                                          PropertyDefinitionData[] ancestorDefinition,
                                                          PropertyDefinitionData[] recipientDefinition) throws RepositoryException {

    List<PropertyDefinitionData> sameDefinitionData = new ArrayList<PropertyDefinitionData>();
    List<List<PropertyDefinitionData>> changedDefinitionData = new ArrayList<List<PropertyDefinitionData>>();
    List<PropertyDefinitionData> newDefinitionData = new ArrayList<PropertyDefinitionData>();
    List<PropertyDefinitionData> removedDefinitionData = new ArrayList<PropertyDefinitionData>();
    init(ancestorDefinition,
         recipientDefinition,
         sameDefinitionData,
         changedDefinitionData,
         newDefinitionData,
         removedDefinitionData);

    // create changes log
    PlainChangesLog changesLog = new PlainChangesLogImpl();

    // DataManager dm = persister.getDataManager();
    // removing properties
    validateRemoved(registeredNodeType, removedDefinitionData);

    // new property definition
    List<PropertyDefinitionData> toAddList = new ArrayList<PropertyDefinitionData>();

    validateAdded(newDefinitionData, removedDefinitionData, toAddList);

    Set<String> nodes = nodeTypeDataManager.getNodes(registeredNodeType.getName());
    //
    doAdd(toAddList, changesLog, nodes);
    // changed
    doChanged(registeredNodeType, changedDefinitionData, nodes);
    return changesLog;

  }

  /**
   * @param registeredNodeType
   * @param changedDefinitionData
   * @param nodes
   * @throws RepositoryException
   */
  private void doChanged(NodeTypeData registeredNodeType,
                         List<List<PropertyDefinitionData>> changedDefinitionData,
                         Set<String> nodes) throws RepositoryException {
    for (List<PropertyDefinitionData> list : changedDefinitionData) {
      PropertyDefinitionData ancestorDefinitionData = list.get(0);
      PropertyDefinitionData recipientDefinitionData = list.get(1);
      // change from mandatory=false to mandatory = true
      // TODO residual
      if (!ancestorDefinitionData.isMandatory() && recipientDefinitionData.isMandatory()) {
        Set<String> nodes2 = nodeTypeDataManager.getNodes(registeredNodeType.getName(),
                                                          new InternalQName[] {},
                                                          new InternalQName[] { recipientDefinitionData.getName() });
        if (nodes2.size() > 0) {
          String message = "Can not change " + recipientDefinitionData.getName().getAsString()
              + " property definition from mandatory=false to mandatory = true , because "
              + " the following nodes ";
          for (String uuids : nodes) {
            message += uuids + " ";
          }
          message += "  doesn't have these properties ";

          throw new RepositoryException(message);
        }

      }
      // change from Protected=false to Protected = true
      if (!ancestorDefinitionData.isProtected() && recipientDefinitionData.isProtected()) {
        // TODO residual
        Set<String> nodes2 = nodeTypeDataManager.getNodes(registeredNodeType.getName(),
                                                          new InternalQName[] {},
                                                          new InternalQName[] { recipientDefinitionData.getName() });
        if (nodes2.size() > 0) {
          String message = "Can not change " + recipientDefinitionData.getName().getAsString()
              + " property definition from Protected=false to Protected = true , because "
              + " the following nodes ";
          for (String uuids : nodes) {
            message += uuids + " ";
          }
          message += "  doesn't have these properties ";

          throw new RepositoryException(message);
        }
      }
      // Required type change
      if (ancestorDefinitionData.getRequiredType() != recipientDefinitionData.getRequiredType()
          && recipientDefinitionData.getRequiredType() != PropertyType.UNDEFINED) {
        Set<String> nodes2;
        if (Constants.JCR_ANY_NAME.equals(recipientDefinitionData.getName())) {
          nodes2 = nodeTypeDataManager.getNodes(registeredNodeType.getName());
        } else {
          nodes2 = nodeTypeDataManager.getNodes(registeredNodeType.getName(),
                                                new InternalQName[] { recipientDefinitionData.getName() },
                                                new InternalQName[] {});
        }
        for (String uuid : nodes2) {
          NodeData nodeData = (NodeData) persister.getItemData(uuid);
          if (Constants.JCR_ANY_NAME.equals(recipientDefinitionData.getName())) {
            List<PropertyData> propertyDatas = persister.getChildPropertiesData(nodeData);
            for (PropertyData propertyData : propertyDatas) {
              // skip mixin and primary type
              if (!propertyData.getQPath().getName().equals(Constants.JCR_PRIMARYTYPE)
                  && !propertyData.getQPath().getName().equals(Constants.JCR_MIXINTYPES)) {
                if (propertyData.getType() != recipientDefinitionData.getRequiredType()) {
                  throw new RepositoryException("Can not change  requiredType to "
                      + ExtendedPropertyType.nameFromValue(recipientDefinitionData.getRequiredType())
                      + " in " + recipientDefinitionData.getName().getAsString() + "  because "
                      + propertyData.getQPath().getAsString() + " have "
                      + ExtendedPropertyType.nameFromValue(propertyData.getType()));

                }
              }
            }

          } else {
            PropertyData propertyData = (PropertyData) persister.getItemData(nodeData,
                                                                             new QPathEntry(recipientDefinitionData.getName(),
                                                                                            0));
            if (propertyData.getType() != recipientDefinitionData.getRequiredType()) {
              throw new RepositoryException("Can not change  requiredType to "
                  + ExtendedPropertyType.nameFromValue(recipientDefinitionData.getRequiredType())
                  + " in " + recipientDefinitionData.getName().getAsString() + "  because "
                  + propertyData.getQPath().getAsString() + " have "
                  + ExtendedPropertyType.nameFromValue(propertyData.getType()));
            }
          }

        }
      }
      // ValueConstraints
      // multiple change
    }
  }

  /**
   * @param toAddList
   * @param changesLog
   * @param nodes
   * @throws RepositoryException
   */
  private void doAdd(List<PropertyDefinitionData> toAddList,
                     PlainChangesLog changesLog,
                     Set<String> nodes) throws RepositoryException {
    for (String uuid : nodes) {
      NodeData nodeData = (NodeData) persister.getItemData(uuid);

      // added properties
      for (PropertyDefinitionData newPropertyDefinitionData : toAddList) {
        List<ValueData> valueConstraintsValues = new ArrayList<ValueData>();
        for (String vc : newPropertyDefinitionData.getValueConstraints())
          valueConstraintsValues.add(new TransientValueData(vc));

        // added state
        int type = newPropertyDefinitionData.getRequiredType();
        changesLog.add(new ItemState(TransientPropertyData.createPropertyData(nodeData,
                                                                              Constants.JCR_PRIMARYTYPE,
                                                                              type,
                                                                              newPropertyDefinitionData.isMultiple(),
                                                                              valueConstraintsValues),
                                     ItemState.ADDED,
                                     false,
                                     nodeData.getQPath()));
      }
    }
  }

  /**
   * @param newDefinitionData
   * @param removedDefinitionData
   * @param toAddList
   * @throws RepositoryException
   */
  private void validateAdded(List<PropertyDefinitionData> newDefinitionData,
                             List<PropertyDefinitionData> removedDefinitionData,
                             List<PropertyDefinitionData> toAddList) throws RepositoryException {
    if (newDefinitionData.size() > 0) {

      for (PropertyDefinitionData propertyDefinitionData : removedDefinitionData) {
        // skipping residual
        if (propertyDefinitionData.getName().equals(Constants.JCR_ANY_NAME))
          continue;
        // try to add mandatory or auto-created properties for
        // for already addded nodes.
        if (propertyDefinitionData.isMandatory() || propertyDefinitionData.isAutoCreated()) {
          if (propertyDefinitionData.getValueConstraints().length == 0)
            throw new RepositoryException("No default values defined for "
                + propertyDefinitionData.getName());
          toAddList.add(propertyDefinitionData);

        }
      }
    }
  }

  /**
   * @param registeredNodeType
   * @param toRemoveList
   * @throws RepositoryException
   */
  private void validateRemoved(NodeTypeData registeredNodeType,
                               List<PropertyDefinitionData> removedDefinitionData) throws RepositoryException {
    for (PropertyDefinitionData removePropertyDefinitionData : removedDefinitionData) {
      Set<String> nodes;
      if (removePropertyDefinitionData.getName().equals(Constants.JCR_ANY_NAME)) {
        nodes = nodeTypeDataManager.getNodes(registeredNodeType.getName());
        for (String uuid : nodes) {
          NodeData nodeData = (NodeData) persister.getItemData(uuid);
          List<PropertyData> childs = persister.getChildPropertiesData(nodeData);
          // more then mixin and primary type
          // TODO it could be possible, check add definitions
          for (PropertyData propertyData : childs) {
            if (!propertyData.getQPath().getName().equals(Constants.JCR_PRIMARYTYPE)
                && !propertyData.getQPath().getName().equals(Constants.JCR_MIXINTYPES)) {
              throw new RepositoryException("Can't remove residual property definition for "
                  + registeredNodeType.getName().getAsString() + " node type, because node "
                  + nodeData.getQPath().getAsString() + " contains property "
                  + propertyData.getQPath().getName().getAsString());
            }
          }
        }
      } else
        nodes = nodeTypeDataManager.getNodes(registeredNodeType.getName(),
                                             new InternalQName[] { removePropertyDefinitionData.getName() },
                                             new InternalQName[] {});
      if (nodes.size() > 0) {
        String message = "Can not remove " + removePropertyDefinitionData.getName().getAsString()
            + " PropertyDefinitionData, because the following nodes have these properties: ";
        for (String uuids : nodes) {
          message += uuids + " ";
        }
        throw new RepositoryException(message);

      }
    }
  }

  /**
   * @param ancestorDefinition
   * @param recipientDefinition
   * @param sameDefinitionData
   * @param changedDefinitionData
   * @param newDefinitionData
   * @param removedDefinitionData
   */
  private void init(PropertyDefinitionData[] ancestorDefinition,
                    PropertyDefinitionData[] recipientDefinition,
                    List<PropertyDefinitionData> sameDefinitionData,
                    List<List<PropertyDefinitionData>> changedDefinitionData,
                    List<PropertyDefinitionData> newDefinitionData,
                    List<PropertyDefinitionData> removedDefinitionData) {
    for (int i = 0; i < recipientDefinition.length; i++) {
      boolean isNew = true;
      for (int j = 0; j < ancestorDefinition.length; j++) {
        if (recipientDefinition[i].getName().equals(ancestorDefinition[j].getName())) {
          isNew = false;
          if (recipientDefinition[i].equals(ancestorDefinition[j]))
            sameDefinitionData.add(recipientDefinition[i]);
          else {
            List<PropertyDefinitionData> list = new ArrayList<PropertyDefinitionData>();

            list.add(ancestorDefinition[j]);
            list.add(recipientDefinition[i]);
            changedDefinitionData.add(list);
          }
        }
      }
      if (isNew)
        newDefinitionData.add(recipientDefinition[i]);
    }
    for (int i = 0; i < ancestorDefinition.length; i++) {
      boolean isRemoved = true;
      for (int j = 0; j < recipientDefinition.length && isRemoved; j++) {
        if (recipientDefinition[i].getName().equals(ancestorDefinition[j].getName())) {
          isRemoved = false;
          break;
        }
      }
      if (isRemoved)
        removedDefinitionData.add(ancestorDefinition[i]);
    }
  }
}
