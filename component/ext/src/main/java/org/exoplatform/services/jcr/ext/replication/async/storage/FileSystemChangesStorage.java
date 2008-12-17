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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.util.Collection;
import java.util.LinkedHashMap;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacket;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: FileSystemChangesStorage.java 25275 2008-12-17 14:40:05Z pnedonosko $
 */
public class FileSystemChangesStorage implements ChangesStorage {

  class ItemKey {
    private final String key;
    
    ItemKey(String itemId) {
      this.key = itemId;
    }
    
    ItemKey(QPath path) {
      this.key = path.getAsString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
      return key.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
      return key.hashCode();
    }
  }
  
  class StateLocator {
    private final String logPath;
    
    private final QPath path; 
    private final String itemId; 
    private final int state;
    
    StateLocator(String logPath, QPath path, String itemId, int state) {
      this.logPath = logPath;
      
      // path, id, state used in traversing
      this.path = path;
      this.itemId = itemId;
      this.state = state;
    }
    
    /**
     * Read file and deserialize the state.
     *
     * @return ItemState
     */
    ItemState getChange() {
      return null; // TODO
    }
    
  }
  
  private final LinkedHashMap<ItemKey, StateLocator> index = new LinkedHashMap<ItemKey, StateLocator>();

  /**
   * {@inheritDoc}
   */
  public ChangesSequence<ItemState> getChanges() {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ChangesSequence<ItemState> getDescendantChanges(QPath root) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<ItemState> getDescendantsChanges(QPath rootPath,
                                                     boolean onlyNodes,
                                                     boolean unique) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getItemState(NodeData parentData, QPathEntry name) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getItemState(QPath itemPath) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getItemState(String itemIdentifier) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getNextItemState(ItemState item) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public QPath getNextItemStateByUUIDOnUpdate(ItemState startState, String UUID) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getPreviousItemState(ItemState item) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public ItemState getPreviousItemStateByQPath(ItemState startState, QPath path) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    // TODO Auto-generated method stub
    return super.equals(obj);
  }

  /**
   * {@inheritDoc}
   */
  public void addPacket(AsyncPacket packet) {
    // TODO Auto-generated method stub
    
  }

}
