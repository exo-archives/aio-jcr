/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: ItemDataTraversingVisitor.java 12843 2007-02-16 09:11:18Z peterit $
 */

public abstract class ItemDataTraversingVisitor implements ItemDataVisitor {

  protected final int maxLevel;

  protected int currentLevel = 0;
  
  protected final ItemDataConsumer dataManager;

  public ItemDataTraversingVisitor(ItemDataConsumer dataManager, int  maxLevel) {
    this.maxLevel = maxLevel;
    this.dataManager = dataManager;
  }
  
  public ItemDataTraversingVisitor(ItemDataConsumer dataManager) {
    this.maxLevel = -1;
    this.dataManager = dataManager;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataVisitor#visit(org.exoplatform.services.jcr.datamodel.PropertyData)
   */
  public void visit(PropertyData property) throws RepositoryException {
    entering(property, currentLevel);
    leaving(property, currentLevel);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataVisitor#visit(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public void visit(NodeData node) throws RepositoryException {
    try {
      entering(node, currentLevel);
      if (maxLevel == -1 || currentLevel < maxLevel) {
        currentLevel++;
        for(PropertyData data : dataManager.getChildPropertiesData(node))
          data.accept(this); 
        for(NodeData data : dataManager.getChildNodesData(node))
          data.accept(this);
        currentLevel--;
      }
      leaving(node, currentLevel);
    } catch (RepositoryException re) {
      currentLevel = 0;
      throw re; 
    }

  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataVisitor#getDataManager()
   */
  public ItemDataConsumer getDataManager() {
    return dataManager;
  }

  /**
   * handler for PropertyData entering
   * @param property
   * @param level
   * @throws RepositoryException
   */
  protected abstract void entering(PropertyData property, int level) throws RepositoryException;

  /**
   * handler for NodeData entering
   * @param node
   * @param level
   * @throws RepositoryException
   */
  protected abstract void entering(NodeData node, int level) throws RepositoryException;

  /**
   * handler for PropertyData leaving
   * @param property
   * @param level
   * @throws RepositoryException
   */
  protected abstract void leaving(PropertyData property, int level) throws RepositoryException;

  /**
   * handler for NodeData entering
   * @param node
   * @param level
   * @throws RepositoryException
   */
  protected abstract void leaving(NodeData node, int level) throws RepositoryException;



}
