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

import java.util.LinkedHashMap;
import java.util.List;

import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SAS. 
 * 
 * ItemsCache impl based on HashMap.
 * 
 * Date: 29.04.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: HashMapItemsCache.java 15127 2008-06-03 08:39:27Z pnedonosko $
 */
public class HashMapItemsCache implements ItemsCache {

  protected final LinkedHashMap<Object, ItemData> cache;
  
  protected final int maxSize;
  
  protected final int capacity;
  
  protected final float loadFactor = 0.7f;
  
  /**
   * Cached item.
   * 
   * Can has one of or both properties and child nodes lists.
   * if hasn't it's null.
   */
  class CItem {
    
    final List<NodeData> childNodes;
    
    final List<PropertyData> properties;
    
    final ItemData item;
    
    CItem(ItemData item, List<PropertyData> properties, List<NodeData> childNodes) {
      this.item = item;
      this.properties = properties;
      this.childNodes = childNodes;
    }
    
  }
  
  HashMapItemsCache(int maxSize) {
    this.maxSize = maxSize;
    this.capacity = Math.round(this.maxSize / loadFactor) + 100;
    
    // LRU with no rehash feature
    this.cache = new LinkedHashMap<Object, ItemData>(capacity, loadFactor, true);
  }
  
  public int getCapacity() {
    return capacity;
  }

  public int getMaxSize() {
    return maxSize;
  }

  public ItemData get(final String parentIdentifier, final QPathEntry name) {
    return cache.get(new CacheQPath(parentIdentifier, name));
  }

  public ItemData get(final String identifier) {
    return cache.get(identifier);
  }

  public void put(final ItemData item) {
    cache.put(item.getIdentifier(), item);
    cache.put(new CacheQPath(item.getParentIdentifier(), item.getQPath()), item);
  }

  public void remove(final ItemData item) {
    cache.remove(item.getIdentifier());
    cache.remove(new CacheQPath(item.getParentIdentifier(), item.getQPath()));
  }

}
