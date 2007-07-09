/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.metadata;

import javax.jcr.Value;

import org.apache.commons.chain.Context;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;

/**
 * for node operations like addNode, checkin 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class SetDCMetadataAction implements Action{

  public boolean execute(Context ctx) throws Exception {
    NodeImpl node = (NodeImpl)ctx.get("currentItem");
    if (node.canAddMixin("dc:elementSet"))
      node.addMixin("dc:elementSet");
    node.setProperty("dc:creator", new String[] { node.getSession().getUserID() });
    ValueFactoryImpl vf = node.getSession().getValueFactory();
      
    node.setProperty("dc:date", new Value[] { vf.createValue(       
        node.getSession().getTransientNodesManager()
        .getTransactManager().getStorageDataManager().getCurrentTime())});
    
    return false;

//    //ItemImpl item = (ItemImpl) ctx.get("currentItem");
//    NodeImpl node = null;
//    if (item instanceof PropertyImpl ){
//      PropertyImpl prop =(PropertyImpl) item;
//      if(!prop.getName().equals("dc:creator") &&  !prop.getName().equals("dc:date"))
//      node = (NodeImpl) prop.getParent();
//    }else if (item instanceof NodeImpl){
//      node = (NodeImpl) item;
//    }
//    
//    if(node != null ){
//      if (node.canAddMixin("dc:elementSet"))
//      node.addMixin("dc:elementSet");
//      node.setProperty("dc:creator", new String[] { node.getSession().getUserID() });
//      ValueFactoryImpl vf = node.getSession().getValueFactory();
//      
//      node.setProperty("dc:date", new Value[] { vf.createValue(node.getSession().getTransientNodesManager()
//          .getTransactManager().getStorageDataManager().getCurrentTime())});
//
//    }
//    return false;
  }

}
