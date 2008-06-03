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

package org.exoplatform.frameworks.webdavclient.search;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.search.basicsearch.BasicSearchCondition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class BasicSearchQuery extends AbstractQuery {

  private String fromHref = "";
  private int fromDepth = Integer.MAX_VALUE;
  
  private BasicSearchCondition condition; 
  
  public BasicSearchQuery() {
    Log.info("Construct..............");
  }
  
  public void setFrom(String fromHref, int fromDepth) {
    this.fromHref = fromHref;
    this.fromDepth = fromDepth;
  }
  
  public void setCondition(BasicSearchCondition condition) {
    this.condition = condition;
  }
  
  public Element toXml(Document xmlDocument) {    
    Log.info("To XML...............");
    
    Element basicSearchEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.BASICSEARCH);
    
    Element selectEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.SELECT);
    basicSearchEl.appendChild(selectEl);
    
    selectEl.appendChild(properties.toXml(xmlDocument));
    
    {
      // FROM
      Element fromEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.FROM);
      basicSearchEl.appendChild(fromEl);
      
      Element scopeEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.SCOPE);
      fromEl.appendChild(scopeEl);
      
      Element hrefEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.HREF);
      scopeEl.appendChild(hrefEl);
      hrefEl.setTextContent(fromHref);
      
      Element depthEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.DEPTH);
      scopeEl.appendChild(depthEl);
      if (fromDepth == Integer.MAX_VALUE) {
        depthEl.setTextContent("infinity");
      } else {
        depthEl.setTextContent("" + fromDepth);
      }
    }

    Element whereEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.WHERE);
    basicSearchEl.appendChild(whereEl);
    whereEl.appendChild(condition.toXml(xmlDocument));
    
    return basicSearchEl;
  }
  
}
