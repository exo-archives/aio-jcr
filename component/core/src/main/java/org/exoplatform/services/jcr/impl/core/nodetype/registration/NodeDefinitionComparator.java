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

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionData;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeData;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeDataManagerImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class NodeDefinitionComparator {
  /**
   * Class logger.
   */
  private static final Log              LOG = ExoLogger.getLogger(NodeDefinitionComparator.class);

  private final LocationFactory         locationFactory;

  protected final DataManager           persister;

  private final NodeTypeDataManagerImpl nodeTypeDataManager;

  /**
   * @param locationFactory
   * @param nodeTypeDataManager
   * @param persister
   */
  public NodeDefinitionComparator(NodeTypeDataManagerImpl nodeTypeDataManager,
                                  LocationFactory locationFactory,
                                  DataManager persister) {
    super();
    this.locationFactory = locationFactory;
    this.nodeTypeDataManager = nodeTypeDataManager;
    this.persister = persister;
  }

  public PlainChangesLog processNodeDefinitionChanges(NodeTypeData registeredNodeType,
                                                      NodeDefinitionData[] ancestorDefinition,
                                                      NodeDefinitionData[] recipientDefinition) throws RepositoryException {

    List<NodeDefinitionData> sameDefinitionData = new ArrayList<NodeDefinitionData>();
    List<List<NodeDefinitionData>> changedDefinitionData = new ArrayList<List<NodeDefinitionData>>();
    List<NodeDefinitionData> newDefinitionData = new ArrayList<NodeDefinitionData>();
    List<NodeDefinitionData> removedDefinitionData = new ArrayList<NodeDefinitionData>();
    init(ancestorDefinition,
         recipientDefinition,
         sameDefinitionData,
         changedDefinitionData,
         newDefinitionData,
         removedDefinitionData);
    // create changes log
    PlainChangesLog changesLog = new PlainChangesLogImpl();
    // check removed
    validateRemoved(registeredNodeType, removedDefinitionData);

    return changesLog;

  }

  private void validateRemoved(NodeTypeData registeredNodeType,
                               List<NodeDefinitionData> removedDefinitionData) throws RepositoryException {

    for (NodeDefinitionData removeNodeDefinitionData : removedDefinitionData) {
      Set<String> nodes;
      if (removeNodeDefinitionData.getName().equals(Constants.JCR_ANY_NAME)) {
        nodes = nodeTypeDataManager.getNodes(registeredNodeType.getName());
        for (String uuid : nodes) {
          NodeData nodeData = (NodeData) persister.getItemData(uuid);
          List<NodeData> childs = persister.getChildNodesData(nodeData);
          // more then mixin and primary type
          // TODO it could be possible, check add definitions
          if (childs.size() > 0) {
            String msg = "Can't remove node definition "
                + removeNodeDefinitionData.getName().getAsString() + "  for "
                + registeredNodeType.getName().getAsString() + " node type because node "
                + nodeData.getQPath().getAsString() + " " + " countains child nodes with name :";
            for (NodeData childsData : childs) {
              msg += childsData.getQPath().getName().getAsString() + " ";
            }
            throw new RepositoryException(msg);
          }
        }
      } else {
        nodes = nodeTypeDataManager.getNodes(registeredNodeType.getName());
        for (String uuid : nodes) {
          NodeData nodeData = (NodeData) persister.getItemData(uuid);
          ItemData child = persister.getItemData(nodeData,
                                                 new QPathEntry(removeNodeDefinitionData.getName(),
                                                                0));
          if (child != null && child.isNode()) {
            throw new RepositoryException("Can't remove node definition "
                + removeNodeDefinitionData.getName().getAsString() + "  for "
                + registeredNodeType.getName().getAsString() + " node type because node "
                + nodeData.getQPath().getAsString() + " " + " countains child node with name "
                + child.getQPath().getName().getAsString());

          }
        }

      }
    }
  }

  private void init(NodeDefinitionData[] ancestorDefinition,
                    NodeDefinitionData[] recipientDefinition,
                    List<NodeDefinitionData> sameDefinitionData,
                    List<List<NodeDefinitionData>> changedDefinitionData,
                    List<NodeDefinitionData> newDefinitionData,
                    List<NodeDefinitionData> removedDefinitionData) {
    for (int i = 0; i < recipientDefinition.length; i++) {
      boolean isNew = true;
      for (int j = 0; j < ancestorDefinition.length; j++) {
        if (recipientDefinition[i].getName().equals(ancestorDefinition[j].getName())) {
          isNew = false;
          if (recipientDefinition[i].equals(ancestorDefinition[j]))
            sameDefinitionData.add(recipientDefinition[i]);
          else {
            // TODO make better structure
            List<NodeDefinitionData> list = new ArrayList<NodeDefinitionData>();
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
