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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.AbstractValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;

/**
 * 
 * This visitor takes node and extracts its ItemStates in next order:
 *  <p> - nodes ItemState;  
 *  <p> - nodes version history ItemStates(if exists);
 *  <p> - node properties ItemStares;
 *  <p> - node sub-nodes ItemStates. <p>
 *  
 * Created by The eXo Platform SAS Author : Karpenko Sergiy
 * karpenko.sergiy@gmail.com
 */
public class ItemDataExportVisitor extends ItemDataTraversingVisitor {

  /**
   * Output where extracted ItemStates will be written.
   */
  protected ObjectOutputStream  out;

  /**
   * The stack. In the top it contains a parent node.
   */
  protected Stack<NodeData>     parents;

  /**
   * The NodeTypeManager.
   */
  protected NodeTypeDataManager ntManager;

  public ItemDataExportVisitor(ObjectOutputStream out,
                               NodeData parent,
                               NodeTypeDataManager nodeTypeManager,
                               ItemDataConsumer dataManager) {
    super(dataManager);

    this.out = out;
    this.ntManager = nodeTypeManager;
    this.parents = new Stack<NodeData>();
    this.parents.add(parent);
  }

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {
    TransientPropertyData newProperty = new TransientPropertyData(property.getQPath(),
                                                                  property.getIdentifier(),
                                                                  -1,
                                                                  property.getType(),
                                                                  property.getParentIdentifier(),
                                                                  property.isMultiValued());

    List<ValueData> list = property.getValues();
    
    Iterator<ValueData> it = list.iterator();
    
    while(it.hasNext()){
      TransientValueData value = ((AbstractValueData)it.next()).createTransientCopy();
      newProperty.setValue(value);
    }
    
    //newProperty.setValues(property.getValues());

    try {
      out.writeObject(new ItemState(newProperty,
                                    ItemState.ADDED,
                                    false,
                                    curParent().getQPath(),
                                    level != 0));
    } catch (IOException e) {
      throw new RepositoryException(e);
    }
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
    // if level == 0 set internal created as false for validating on save
    
    try {
      out.writeObject(new ItemState(newNode, ItemState.ADDED, true, ancestorToSave, level != 0));
    } catch (IOException e) {
      throw new RepositoryException(e);
    }

    // check is versionable
    if (ntManager.isNodeType(Constants.MIX_VERSIONABLE,
                             node.getPrimaryTypeName(),
                             node.getMixinTypeNames())) {

      // get reference to version history
      PropertyData property = (PropertyData) dataManager.getItemData(node,
                                                                     new QPathEntry(Constants.JCR_VERSIONHISTORY,
                                                                                    1));
      String ref;
      try {
        ref = ((TransientValueData) property.getValues().get(0)).getString();
      } catch (IOException e) {
        throw new RepositoryException(e);
      }

      NodeData verStorage = (NodeData) dataManager.getItemData(Constants.VERSIONSTORAGE_UUID);

      QPathEntry nam;
      try {
        nam = QPathEntry.parse("[]" + ref + ":1");
      } catch (IllegalNameException e) {
        throw new RepositoryException(e);
      }

      NodeData verHistory = (NodeData) dataManager.getItemData(verStorage, nam);

      // extract full version history
      ItemDataExportVisitor vis = new ItemDataExportVisitor(out, verStorage, ntManager, dataManager);

      verHistory.accept(vis);
    }
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

}
