/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.dataflow;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public abstract class ItemDataTraversingVisitor implements ItemDataVisitor {
  /**
   * Maximum level.
   */
  protected final int              maxLevel;
  
  public static final int          INFINITE_DEPTH = -1;

  /**
   * Current level.
   */
  protected int                    currentLevel = 0;

  protected final ItemDataConsumer dataManager;

  /**
   * @param dataManager
   *          - ItemDataConsumer.
   * @param maxLevel
   *          - maximum level.
   */
  public ItemDataTraversingVisitor(ItemDataConsumer dataManager, int maxLevel) {
    this.maxLevel = maxLevel;
    this.dataManager = dataManager;
  }

  /**
   * @param dataManager
   *          - ItemDataConsumer
   */
  public ItemDataTraversingVisitor(ItemDataConsumer dataManager) {
    this.maxLevel = INFINITE_DEPTH;
    this.dataManager = dataManager;
  }

  /**
   * {@inheritDoc}
   */
  public void visit(PropertyData property) throws RepositoryException {
    entering(property, currentLevel);
    leaving(property, currentLevel);
  }

  /**
   * {@inheritDoc}
   */
  public void visit(NodeData node) throws RepositoryException {
    try {
      entering(node, currentLevel);
      if (maxLevel == INFINITE_DEPTH || currentLevel < maxLevel) {
        currentLevel++;
        for (PropertyData data : dataManager.getChildPropertiesData(node))
          data.accept(this);
        for (NodeData data : dataManager.getChildNodesData(node))
          data.accept(this);
        currentLevel--;
      }
      leaving(node, currentLevel);
    } catch (RepositoryException re) {
      currentLevel = 0;
      throw re;
    }

  }

  /**
   * {@inheritDoc}
   */
  public ItemDataConsumer getDataManager() {
    return dataManager;
  }

  /**
   * handler for PropertyData entering
   * 
   * @param property
   * @param level
   * @throws RepositoryException
   */
  protected abstract void entering(PropertyData property, int level) throws RepositoryException;

  /**
   * handler for NodeData entering
   * 
   * @param node
   * @param level
   * @throws RepositoryException
   */
  protected abstract void entering(NodeData node, int level) throws RepositoryException;

  /**
   * handler for PropertyData leaving
   * 
   * @param property
   * @param level
   * @throws RepositoryException
   */
  protected abstract void leaving(PropertyData property, int level) throws RepositoryException;

  /**
   * handler for NodeData entering
   * 
   * @param node
   * @param level
   * @throws RepositoryException
   */
  protected abstract void leaving(NodeData node, int level) throws RepositoryException;

}
