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

import org.exoplatform.services.jcr.datamodel.QPath;

/**
 * Created by The eXo Platform SAS 
 * 
 * Date: 10.06.2008
 *
 * Cache record used to store item Id key.
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: CacheId.java 15631 2008-06-12 16:14:30Z pnedonosko $
 */
public class CacheId extends CacheKey {

  /**
   * Item identifier. This id (String) may be placed in childs caches CN, CP (WeakHashMap) as a key.
   * <br/>So, this instance will prevent GC remove them from CN, CP.
   */
  private final String id;
  
  CacheId(String id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object obj) {
    if (id.hashCode() == obj.hashCode() && obj instanceof CacheId)
      return id.equals(((CacheId) obj).id);
    return false;
    
    //return this.id.equals(obj);
  }

  @Override
  public int hashCode() {
    
    return this.id.hashCode();
  }

  @Override
  public String toString() {
    
    return this.id.toString();
  }

  @Override
  boolean isDescendantOf(QPath path) {
    return false;
  }
}
