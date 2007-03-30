/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow.session;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.impl.core.ItemImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;

/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: SessionItemPool.java 12841 2007-02-16 08:58:38Z peterit $
 */
 final class SessionItemPool {
  
  private WeakHashMap <String, ItemImpl> items;
  
  SessionItemPool() {
    items = new WeakHashMap <String, ItemImpl> ();
  }
  
  void remove(String uuid) {
    items.remove(uuid);
    System.gc();
  } 
  
  void put(ItemImpl item) {
    if(!items.containsKey(item.getInternalUUID()))
      items.put(item.getInternalUUID(), item);
  }

  ItemImpl get(String uuid) {
    return items.get(uuid);
  }

  ItemImpl get(InternalQPath path) {
    for(Iterator <ItemImpl> i = items.values().iterator(); i.hasNext();) {
      ItemImpl item = i.next();
      if(item.getInternalPath().equals(path))
        return item;
    }
    return null;
  }

  List <NodeImpl> getChildNodes(String parentUUID) {
    List <NodeImpl> children = new ArrayList <NodeImpl> ();
    for(Iterator <ItemImpl> i = items.values().iterator(); i.hasNext();) {
      ItemImpl item = i.next();
      if(item.getParentUUID().equals(parentUUID)
          && item.isNode())
        children.add((NodeImpl)item);
    }
    return children;
  }

  List <PropertyImpl> getChildProperties(String parentUUID) {
    List <PropertyImpl> children = new ArrayList <PropertyImpl> ();
    for(Iterator <ItemImpl> i = items.values().iterator(); i.hasNext();) {
      ItemImpl item = i.next();
      if(item.getParentUUID().equals(parentUUID)
          && item.isNode())
        children.add((PropertyImpl)item);
    }
    return children;
  }

}
