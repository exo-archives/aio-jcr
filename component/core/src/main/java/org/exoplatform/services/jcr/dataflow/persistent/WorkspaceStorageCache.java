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
package org.exoplatform.services.jcr.dataflow.persistent;

import java.util.List;

import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SAS.
 * 
 * Defines storage cache contract
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: WorkspaceStorageCache.java 13869 2008-05-05 08:40:10Z pnedonosko $
 */
public interface WorkspaceStorageCache extends ItemsPersistenceListener {

  public static final String MAX_SIZE_PARAMETER_NAME  = "max-size";

  public static final String LIVE_TIME_PARAMETER_NAME = "live-time";

  /**
   * Get item by parent identifier and name +index.
   * 
   * @param parentIdentifier
   * @param path
   * @return itemData by parent Identifier and item name with index or null if not found
   */
  ItemData get(String parentIdentifier, QPathEntry name);

  /**
   * Get item by identifier.
   * 
   * @param identifier
   * @return ItemData by Identifier or null if not found
   */
  ItemData get(String identifier);

  /**
   * @param parent
   * @return child nodes for parent if found; empty list if no items found; null if no items
   *         initialized
   */
  List<NodeData> getChildNodes(NodeData parent);

  /**
   * Get node child properties.<br/>
   * 
   * @param parent
   * @return child properties for parent if found; empty list if no items found; null if no items
   *         initialized
   */
  List<PropertyData> getChildProperties(NodeData parent);

  /**
   * List node child properties.<br/> A difference from {@link getChildProperties()} it's that the
   * method may return list of node properties (PropertyData) which contains no data
   * (ValueData).<br/> Used for Node.hasProperties(), NodeIndexer.createDoc().
   * 
   * @param parent
   * @return child properties for parent if found; null if no items initialized
   */
  List<PropertyData> listChildProperties(final NodeData parentData);

  /**
   * Adds (or updates if found) ItemData.
   * 
   * @param item
   */
  void put(ItemData item);

  /**
   * Adds (update should not be the case!) list of child nodes. The list can be empty. If list is
   * null the operation is ignored.
   * 
   * @param parent
   * @param childNodes
   */
  void addChildNodes(NodeData parent, List<NodeData> childNodes);

  /**
   * Adds (update should not be the case!) list of child properties. The list can be empty. If list
   * is null the operation is ignored.
   * 
   * @param parent
   * @param childNodes
   */
  void addChildProperties(NodeData parent, List<PropertyData> childProperties);

  /**
   * Adds (update should not be the case!) list of child properties with empty values. The list can
   * be empty. If list is null the operation is ignored.
   * 
   * @param parent
   * @param childNodes
   */
  void addChildPropertiesList(NodeData parent, List<PropertyData> childProperties);

  /**
   * Removes data and its children from cache.
   * 
   * @param item
   */
  void remove(ItemData item);

  /**
   * 
   * @return enabled status flag, if true then cache is enabled
   */
  boolean isEnabled();

  /**
   * Cache size.
   * 
   * @return long value
   */
  long getSize();

}
