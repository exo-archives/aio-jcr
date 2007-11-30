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

package org.exoplatform.services.jcr.impl.dataflow.session;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.core.ItemImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id$
 */
 final class SessionItemPool {
  
  private WeakHashMap <String, ItemImpl> items;
  
  SessionItemPool() {
    items = new WeakHashMap <String, ItemImpl> ();
  }
  
  void remove(String identifier) {
    items.remove(identifier);
    System.gc();
  } 
  
  void put(ItemImpl item) {
    if(!items.containsKey(item.getInternalIdentifier()))
      items.put(item.getInternalIdentifier(), item);
  }

  ItemImpl get(String identifier) {
    return items.get(identifier);
  }

  ItemImpl get(QPath path) {
    for(Iterator <ItemImpl> i = items.values().iterator(); i.hasNext();) {
      ItemImpl item = i.next();
      if(item.getInternalPath().equals(path))
        return item;
    }
    return null;
  }

  List <NodeImpl> getChildNodes(String parentIdentifier) {
    List <NodeImpl> children = new ArrayList <NodeImpl> ();
    for(Iterator <ItemImpl> i = items.values().iterator(); i.hasNext();) {
      ItemImpl item = i.next();
      if(item.getParentIdentifier().equals(parentIdentifier)
          && item.isNode())
        children.add((NodeImpl)item);
    }
    return children;
  }

  List <PropertyImpl> getChildProperties(String parentIdentifier) {
    List <PropertyImpl> children = new ArrayList <PropertyImpl> ();
    for(Iterator <ItemImpl> i = items.values().iterator(); i.hasNext();) {
      ItemImpl item = i.next();
      if(item.getParentIdentifier().equals(parentIdentifier)
          && item.isNode())
        children.add((PropertyImpl)item);
    }
    return children;
  }

}
