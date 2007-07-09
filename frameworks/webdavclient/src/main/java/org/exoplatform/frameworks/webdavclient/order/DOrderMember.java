/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.order;

import org.exoplatform.frameworks.webdavclient.Const;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DOrderMember {

  private String segment;
  private int position = OrderConst.FIRST;
  private String positionSegment;
  
  public DOrderMember() {
  }
  
  public void setSegment(String segment) {
    this.segment = segment;
  }
  
  public String getSegment() {
    return segment;
  }
  
  public void setPosition(int position) {
    this.position = position;
  }
  
  public void setPosition(int position, String positionSegment) {
    this.position = position;
    this.positionSegment = positionSegment;
  }
  
  public void serialize(Document document, Element parentElement) {    
    Element orderMebnerEl = document.createElement(Const.Dav.PREFIX + Const.DavProp.ORDERMEMBER);
    parentElement.appendChild(orderMebnerEl);
    
    Element segmentEl = document.createElement(Const.Dav.PREFIX + Const.DavProp.SEGMENT);
    orderMebnerEl.appendChild(segmentEl);
    segmentEl.setTextContent(segment);
    
    Element positionEl = document.createElement(Const.Dav.PREFIX + Const.DavProp.POSITION);
    orderMebnerEl.appendChild(positionEl);

    Element positionValueEl = null;
    
    while (true) {
      
      if (position == OrderConst.FIRST) {
        positionValueEl = document.createElement(Const.Dav.PREFIX + Const.DavProp.FIRST);
        break;
      }
      
      if (position == OrderConst.LAST) {
        positionValueEl = document.createElement(Const.Dav.PREFIX + Const.DavProp.LAST);
        break;
      }
      
      if (position == OrderConst.BEFORE) {
        positionValueEl = document.createElement(Const.Dav.PREFIX + Const.DavProp.BEFORE);
      } else {
        positionValueEl = document.createElement(Const.Dav.PREFIX + Const.DavProp.AFTER); 
      }

      Element positionSegmentEl = document.createElement(Const.Dav.PREFIX + Const.DavProp.SEGMENT);
      positionValueEl.appendChild(positionSegmentEl);
      positionSegmentEl.setTextContent(positionSegment);
      
      break;
    }

    positionEl.appendChild(positionValueEl);
  }
  
  
}
