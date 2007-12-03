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

package org.exoplatform.services.webdav.order;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavProperty;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class OrderMember {
  
  public static final int POSITION_FIRST     = 0;

  public static final int POSITION_LAST      = 1;
  
  public static final int POSITION_BEFORE    = 2;
  
  public static final int POSITION_AFTER     = 3;  

  private static Log log = ExoLogger.getLogger("jcr.OrderMember");
  
  public static final String XML_ORDERMEMBER = "order-member"; 
  
  private String segment;
  
  private int position = POSITION_FIRST;
  
  private String positionSegment;
  
  private int status = WebDavStatus.OK;
  
  public int getStatus() {
    return status;
  }
  
  public void setStatus(int status) {
    this.status = status;
  }
  
  public OrderMember() {
  }
  
  public String getSegment() {
    return segment;
  }
  
  public int getPosition() {
    return position;
  }
  
  public String getPositionSegment() {
    return positionSegment;
  }
  
  public boolean initFromDom(Node memberNode) {
    try {
      Node segmentNode = DavUtil.getChildNode(memberNode, WebDavProperty.SEGMENT);
      
      segment = segmentNode.getTextContent();
      
      Node positionNode = DavUtil.getChildNode(memberNode, WebDavProperty.POSITION);

      NodeList nodes = positionNode.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        Node positionValNode = nodes.item(i);
        
        if (WebDavProperty.FIRST.equals(positionValNode.getLocalName())) {
          position = POSITION_FIRST;
          return true;          
        }
        
        if (WebDavProperty.LAST.equals(positionValNode.getLocalName())) {
          position = POSITION_LAST;
          return true;          
        }
        
        if (WebDavProperty.BEFORE.equals(positionValNode.getLocalName())) {
          position = POSITION_BEFORE;
          positionSegment = DavUtil.getChildNode(positionValNode, WebDavProperty.SEGMENT).getTextContent();
          return true;
        }
        
        if (WebDavProperty.AFTER.equals(positionValNode.getLocalName())) {
          position = POSITION_AFTER;
          positionSegment = DavUtil.getChildNode(positionValNode, WebDavProperty.SEGMENT).getTextContent();
          return true;
        }
        
      }
        
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
    return false;
  }
  
}
