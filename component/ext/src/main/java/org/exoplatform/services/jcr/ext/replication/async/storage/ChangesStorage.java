/**
 * 
 */
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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.util.Collection;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Actually ChangesLog impl. But without getAllStates().
 * 
 * <br/>Date: 16.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public interface ChangesStorage {

  /**
   * Get last ItemState by Item id.
   * 
   * @param itemIdentifier
   *          String, Item id
   * @return ItemState
   */
  ItemState getItemState(String itemIdentifier);

  /**
   * Get last ItemState by parent and Item name.
   * 
   * @param parentData
   *          NodeData of the parent
   * @param name
   *          QPathEntry, Item name
   * @return ItemState
   */
  ItemState getItemState(NodeData parentData, QPathEntry name);

  /**
   * Get last ItemState by Item path.
   * 
   * @param itemPath
   *          QPath, path
   * @return ItemState
   */
  ItemState getItemState(QPath itemPath);

  /**
   * Get sequence of all changes.
   * 
   * @return ChangesSequence
   */
  ItemStatesSequence<ItemState> getChanges();

  /**
   * Get sequence of the path descendant changes.
   * 
   * @param root
   *          QPath, root path
   * @return ChangesSequence
   */
  ItemStatesSequence<ItemState> getDescendantChanges(QPath root);

  // =========== custom ==============

  /**
   * TODO
   * 
   * Return descendants changes for a given path.
   * 
   * @param rootPath
   *          - QPath
   * @param onlyNodes
   *          - boolean, true for only NodeData changes
   * @param unique
   *          - ???
   * @return Collection of ItemState
   */
  Collection<ItemState> getDescendantsChanges(QPath rootPath, boolean onlyNodes, boolean unique);

  
  /**
   * TODO can we rely on sequence on log?
   * 
   * getPreviousItemStateByQPath.
   * 
   * @param startState
   * @param path
   * @return
   */
  ItemState getPreviousItemStateByQPath(ItemState startState, QPath path);

  /**
   * TODO can we rely on sequence on log?
   * 
   * getNextItemStateByUUIDOnUpdate.
   * 
   * @param startState
   * @param UUID
   * @return
   */
  QPath getNextItemStateByUUIDOnUpdate(ItemState startState, String UUID);

  /**
   * TODO can we rely on sequence on log?
   * 
   * getPreviousItemState.
   * 
   * @param item
   * @return
   */
  ItemState getPreviousItemState(ItemState item);

  /**
   * TODO can we rely on sequence on log?
   * 
   * getNextItemState.
   * 
   * @param item
   * @return
   */
  ItemState getNextItemState(ItemState item);

}
