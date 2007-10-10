/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.order.request;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.exoplatform.services.webdav.order.OrderMember;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: OrderPatchDoc.java 12134 2007-01-20 15:50:13Z gavrikvetal $
 */

public class OrderPatchDocument {

  private static Log log = ExoLogger.getLogger("jcr.OrderPatchDoc");
  
  private ArrayList<OrderMember> members = new ArrayList<OrderMember>();
  
  public ArrayList<OrderMember> getMembers() {
    return members;
  }
  
  public String getDocumentName() {
    return DavConst.DavDocument.ORDERPATCH; 
  }
  
  public boolean init(Document requestDocument, PropertyFactory propertyFactory) {
    try {
      Node orderPatch = DavUtil.getChildNode(requestDocument, getDocumentName());
      
      NodeList childs = orderPatch.getChildNodes();
      for (int i = 0; i < childs.getLength(); i++) {
        Node child = childs.item(i);
        
        if (DavProperty.ORDERMEMBER.equals(child.getLocalName())) {
          OrderMember member = new OrderMember();
          if (member.initFromDom(child)) {
            members.add(member);
          }          
        }
                
      }
      
      return true;
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
    return false;
  }
  
}
