/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemDataTraversingVisitor;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class ItemDataRenameVisitor extends ItemDataTraversingVisitor {
  private static Log        log               = ExoLogger.getLogger("jcr.ItemDataRenameVisitor");

  protected List<ItemState> itemRenamedStates = new ArrayList<ItemState>();

  private Stack<NodeData>   parents           = new Stack<NodeData>();

  private final boolean     isPersisted;

  private final boolean     isEventFire;

  public ItemDataRenameVisitor(NodeData parenData,
                               boolean isPersisted,
                               boolean isEventFile,
                               ItemDataConsumer dataManager,
                               int maxLevel) {
    super(dataManager, maxLevel);
    this.isPersisted = isPersisted;
    this.isEventFire = isEventFile;
    parents.push(parenData);
  }

  @Override
  protected void entering(PropertyData property, int level) throws RepositoryException {
    QPath newPath = QPath.makeChildPath(getParent().getQPath(), property.getQPath().getName());
    TransientPropertyData tDataCopy = new TransientPropertyData(newPath,
                                                                property.getIdentifier(),
                                                                property.getPersistedVersion(),
                                                                property.getType(),
                                                                property.getParentIdentifier(),
                                                                property.isMultiValued());
    tDataCopy.setValues(property.getValues());
    ItemState deletedState = new ItemState(property,
                                           ItemState.RENAMED,
                                           isEventFire,
                                           null,
                                           false,
                                           isPersisted);
    ItemState reanameState = new ItemState(tDataCopy,
                                           ItemState.RENAMED,
                                           isEventFire,
                                           null,
                                           false,
                                           isPersisted);
    itemRenamedStates.add(deletedState);
    itemRenamedStates.add(reanameState);
  }

  @Override
  protected void entering(NodeData node, int level) throws RepositoryException {
    QPath newPath = QPath.makeChildPath(getParent().getQPath(), node.getQPath().getName());
    TransientNodeData newData = new TransientNodeData(newPath,
                                                      node.getIdentifier(),
                                                      node.getPersistedVersion(),
                                                      node.getPrimaryTypeName(),
                                                      node.getMixinTypeNames(),
                                                      node.getOrderNumber(),
                                                      node.getParentIdentifier(),
                                                      node.getACL());
    ItemState deletedState = new ItemState(node,
                                           ItemState.DELETED,
                                           isEventFire,
                                           null,
                                           false,
                                           isPersisted);

    ItemState reanameState = new ItemState(newData,
                                           ItemState.RENAMED,
                                           isEventFire,
                                           null,
                                           false,
                                           isPersisted);
    
    itemRenamedStates.add(deletedState);
    itemRenamedStates.add(reanameState);

    parents.push(newData);
  }

  @Override
  protected void leaving(PropertyData property, int level) throws RepositoryException {
  }

  @Override
  protected void leaving(NodeData node, int level) throws RepositoryException {
    parents.pop();


  }

  private NodeData getParent() {
    return parents.peek();
  }

  public List<ItemState> getItemRenamedStates() {
    return itemRenamedStates;
  }
}
