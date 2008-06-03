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

package org.exoplatform.frameworks.webdavclient.search.basicsearch;

import java.util.ArrayList;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.http.Log;
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
