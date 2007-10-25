/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.search.basicsearch;

import java.util.ArrayList;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.search.SearchConst;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class AndCondition implements BasicSearchCondition {
  
  private ArrayList<BasicSearchCondition> conditions = new ArrayList<BasicSearchCondition>();
  
  public AndCondition(BasicSearchCondition... conditions) {
    Log.info("Conditions: [" + conditions.length + "]");

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
