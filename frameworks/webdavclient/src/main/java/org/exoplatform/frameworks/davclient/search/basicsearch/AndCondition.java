/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.search.basicsearch;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.search.SearchConst;
import org.exoplatform.services.log.ExoLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class AndCondition implements BasicSearchCondition {
  
  private static Log log = ExoLogger.getLogger("jcr.AndCondition");

  private ArrayList<BasicSearchCondition> conditions = new ArrayList<BasicSearchCondition>();
  
  public AndCondition(BasicSearchCondition... conditions) {
    log.info("Conditions: [" + conditions.length + "]");

    for (int i = 0; i < conditions.length; i++) {
      this.conditions.add(conditions[i]);
    }
    
  }
  
  public Element toXml(Document xmlDocument) {
    Element andElement = xmlDocument.createElement(Const.Dav.PREFIX + SearchConst.AND_TAG);
    
    for (int i = 0; i < conditions.size(); i++) {
      BasicSearchCondition condition = conditions.get(i);
      andElement.appendChild(condition.toXml(xmlDocument));
    }
    
    return andElement;
  }
  
}
