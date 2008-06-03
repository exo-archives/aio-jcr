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
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SAS. 
 *
 * Describe item cache contract
 * 
 * Date: 29.04.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: ItemsCache.java 15127 2008-06-03 08:39:27Z pnedonosko $
 */
public interface ItemsCache {

  /**
   * Returns internal cache capacity.
   * 
   * @return
   */
  int getCapacity();
  
  /**
   * Returns maximum cache size.
   *  
   * @return
   */
  int getMaxSize();
  
  /**
   * Get item by parent identifier and name +index.
   * 
   * @param parentIdentifier
   * @param name
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
   * Adds (or updates if found) item.
   *   
   * @param data
   */
  void put(ItemData item);
  
  /**
   * Removes item.
   *  
   * @param item
   */
  void remove(ItemData item);
}
