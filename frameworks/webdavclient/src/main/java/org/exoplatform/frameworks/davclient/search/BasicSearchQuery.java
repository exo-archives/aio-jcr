/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.search;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.search.basicsearch.BasicSearchCondition;
import org.exoplatform.services.log.ExoLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class BasicSearchQuery extends AbstractQuery {

  private static Log log = ExoLogger.getLogger("jcr.BasicSearchQuery");
  
  private String fromHref = "";
  private int fromDepth = Integer.MAX_VALUE;
  
  private BasicSearchCondition condition; 
  
  public BasicSearchQuery() {
    log.info("Construct..............");
  }
  
  public void setFrom(String fromHref, int fromDepth) {
    this.fromHref = fromHref;
    this.fromDepth = fromDepth;
  }
  
  public void setCondition(BasicSearchCondition condition) {
    this.condition = condition;
  }
  
  public Element toXml(Document xmlDocument) {    
    log.info("To XML...............");
    
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
