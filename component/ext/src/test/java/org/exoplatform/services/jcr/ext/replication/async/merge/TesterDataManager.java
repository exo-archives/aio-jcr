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
package org.exoplatform.services.jcr.ext.replication.async.merge;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TesterDataManager.java 111 2008-11-11 11:11:11Z $
 */
public class TesterDataManager implements DataManager {

  private List<ItemData> items;

  public TesterDataManager() {
    items = new ArrayList<ItemData>();
  }

  public TesterDataManager(List<ItemData> items) {
    this.items = items;
  }

  public List<NodeData> getChildNodesData(NodeData parent) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public ItemData getItemData(NodeData parent, QPathEntry name) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public ItemData getItemData(String identifier) throws RepositoryException {
    for (ItemData item : items) {
      if (item.getIdentifier().equals(identifier)) {
        return item;
      }
    }
    return null;
  }

  public List<PropertyData> getReferencesData(String identifier, boolean skipVersionStorage) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public List<PropertyData> listChildPropertiesData(NodeData nodeData) throws RepositoryException {
    // TODO Auto-generated method stub
    return null;
  }

  public void save(ItemStateChangesLog changes) throws InvalidItemStateException,
                                               UnsupportedOperationException,
                                               RepositoryException {
    // TODO Auto-generated method stub

  }

}
