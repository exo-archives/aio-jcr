/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.order;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class OrderMember {

  private static Log log = ExoLogger.getLogger("jcr.OrderMember");
  
  private String segment;
  private int position = OrderConst.FIRST;
  private String positionSegment;
  
  public OrderMember() {
  }
  
  public String getSegment() {
    return segment;
  }
  
  public int getposition() {
    return position;
  }
  
  public String getpositionSegment() {
    return positionSegment;
  }
  
  public boolean initFromDom(Node memberNode) {
    try {
      Node segmentNode = DavUtil.getChildNode(memberNode, DavProperty.SEGMENT);
      
      segment = segmentNode.getTextContent();
      
      Node positionNode = DavUtil.getChildNode(memberNode, DavProperty.POSITION);

      NodeList nodes = positionNode.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        Node positionValNode = nodes.item(i);
        
        if (DavProperty.FIRST.equals(positionValNode.getLocalName())) {
          position = OrderConst.FIRST;
          return true;          
        }
        
        if (DavProperty.LAST.equals(positionValNode.getLocalName())) {
          position = OrderConst.LAST;
          return true;          
        }
        
        if (DavProperty.BEFORE.equals(positionValNode.getLocalName())) {
          position = OrderConst.BEFORE;
          positionSegment = DavUtil.getChildNode(positionValNode, DavProperty.SEGMENT).getTextContent();
          return true;
        }
        
        if (DavProperty.AFTER.equals(positionValNode.getLocalName())) {
          position = OrderConst.AFTER;
          positionSegment = DavUtil.getChildNode(positionValNode, DavProperty.SEGMENT).getTextContent();
          return true;
        }
        
      }
        
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
    return false;
  }
  
}
