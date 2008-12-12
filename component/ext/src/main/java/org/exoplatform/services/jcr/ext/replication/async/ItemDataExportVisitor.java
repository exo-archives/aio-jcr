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
package org.exoplatform.services.jcr.ext.replication.async;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.version.VersionHistoryDataHelper;

/**
 * Created by The eXo Platform SAS Author : Karpenko Sergiy
 * karpenko.sergiy@gmail.com
 */
public class ItemDataExportVisitor extends ItemDataTraversingVisitor {

  public ItemDataExportVisitor(NodeData parent,
                               NodeTypeManagerImpl nodeTypeManager,
                               ItemDataConsumer dataManager) {
    super(dataManager);
    
    this.ntManager = nodeTypeManager;
    this.parents = new Stack<NodeData>();
    this.parents.add(parent);
  }

  /**
   * The list of added item states
   */
  protected List<ItemState>     itemAddStates = new ArrayList<ItemState>();

  /**
   * The stack. In the top it contains a parent node.
   */
  protected Stack<NodeData>     parents;

  /**
   * The NodeTypeManager
   */
  protected NodeTypeManagerImpl ntManager;

  // protected QPath ancestorToSave = null;

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {

    InternalQName qname = property.getQPath().getName();

    List<ValueData> values;

    if (ntManager.isNodeType(Constants.MIX_VERSIONABLE,
                             curParent().getPrimaryTypeName(),
                             curParent().getMixinTypeNames())) {

      // before manipulate with version stuff we have create a one new VH right
      // here!
      QPath vhpPath = QPath.makeChildPath(curParent().getQPath(), Constants.JCR_VERSIONHISTORY);
      ItemState vhpState = findLastItemState(vhpPath);
      if (vhpState == null) {
        // need create a new VH
        PlainChangesLogImpl changes = new PlainChangesLogImpl();
        VersionHistoryDataHelper vh = new VersionHistoryDataHelper(curParent(),
                                                                    changes,
                                                                   dataManager,
                                                                   ntManager);
        
        itemAddStates.addAll(changes.getAllStates());
      }

      values = new ArrayList<ValueData>(1);
      if (qname.equals(Constants.JCR_LOCKISDEEP)) {
        return;
      } else if (qname.equals(Constants.JCR_LOCKOWNER)) {
        return;
      } else if (qname.equals(Constants.JCR_VERSIONHISTORY)) {
        return; // added in VH create
      } else if (qname.equals(Constants.JCR_PREDECESSORS)) {
        return; // added in VH create
      } else if (qname.equals(Constants.JCR_BASEVERSION)) {
        return; // added in VH create
      } else if (qname.equals(Constants.JCR_ISCHECKEDOUT)) {
        values.add(new TransientValueData(true));
      } else if (qname.equals(Constants.JCR_MERGEFAILED)) {
        return; // skip it
      } else if (qname.equals(Constants.JCR_UUID)) {
        values.add(new TransientValueData(curParent().getIdentifier())); // uuid
        // of
        // the
        // parent
      } else {
        values = property.getValues(); // copy the property
      }
    } else if (ntManager.isNodeType(Constants.MIX_REFERENCEABLE,
                                    curParent().getPrimaryTypeName(),
                                    curParent().getMixinTypeNames())
        && qname.equals(Constants.JCR_UUID)) {

      values = new ArrayList<ValueData>(1);
      values.add(new TransientValueData(curParent().getIdentifier()));
    } else {
      // http://jira.exoplatform.org/browse/JCR-294
      if (qname.equals(Constants.JCR_LOCKISDEEP)) {
        return;
      } else if (qname.equals(Constants.JCR_LOCKOWNER)) {
        return;
      }
      values = property.getValues();
    }

    TransientPropertyData newProperty = new TransientPropertyData(QPath.makeChildPath(curParent().getQPath(),
                                                                                      qname),
                                                                  property.getIdentifier(),
                                                                  -1,
                                                                  property.getType(),
                                                                  curParent().getIdentifier(),
                                                                  property.isMultiValued());

    newProperty.setValues(values);

    itemAddStates.add(new ItemState(newProperty,
                                    ItemState.ADDED,
                                    true,
                                    curParent().getQPath(),
                                    level != 0));

  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {

    NodeData parent = curParent();
    QPath ancestorToSave = parent.getQPath();
    
    TransientNodeData newNode = new TransientNodeData(node.getQPath(),
                                                      node.getIdentifier(),
                                                      -1,
                                                      node.getPrimaryTypeName(),
                                                      node.getMixinTypeNames(),
                                                      node.getOrderNumber(),
                                                      node.getParentIdentifier(),
                                                      node.getACL());
    parents.push(newNode);

    // ancestorToSave is a parent node
    // if level == 0 set internal createt as false for validating on save
    itemAddStates.add(new ItemState(newNode, ItemState.ADDED, true, ancestorToSave, level != 0));

  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
    // Do nothing

  }

  @Override
  protected void leaving(NodeData node, int level) throws RepositoryException {
    parents.pop();
  }

  /**
   * Returns the current parent node
   */
  protected NodeData curParent() {
    return parents.peek();
  }

  protected ItemState findLastItemState(QPath itemPath) {
    for (int i = itemAddStates.size() - 1; i >= 0; i--) {
      ItemState istate = itemAddStates.get(i);
      if (istate.getData().getQPath().equals(itemPath))
        return istate;
    }
    return null;
  }

  /**
   * Returns the list of item add states
   */
  public List<ItemState> getItemAddStates() {
    return itemAddStates;
  }
  
  public PlainChangesLog getPlainChangesLog() {
    PlainChangesLog log = new PlainChangesLogImpl();
    log.addAll(itemAddStates);
    return log;
  }
  
}
