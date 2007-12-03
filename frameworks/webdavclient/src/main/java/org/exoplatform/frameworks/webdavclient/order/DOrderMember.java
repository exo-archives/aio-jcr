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

package org.exoplatform.frameworks.webdavclient.order;

import org.exoplatform.frameworks.webdavclient.Const;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
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
