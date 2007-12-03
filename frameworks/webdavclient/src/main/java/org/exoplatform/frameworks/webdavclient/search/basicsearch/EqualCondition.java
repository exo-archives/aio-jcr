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

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.request.NameSpaceRegistry;
import org.exoplatform.frameworks.webdavclient.search.SearchConst;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class EqualCondition implements BasicSearchCondition {
  
  private NameSpaceRegistry nsRegistry = new NameSpaceRegistry();
  
  private String propertyName;
  private String propertyValue;
  
  public EqualCondition(String propertyName, String propertyNameSpace, String propertyValue) {
    
    if (nsRegistry.registerNameSpace(propertyName, propertyNameSpace)) {
      this.propertyName = propertyName;
    } else {
      this.propertyName = Const.Dav.PREFIX + propertyName;
    }
    
    this.propertyValue = propertyValue;
  }
  
  public Element toXml(Document xmlDocument) {
    Element eqElement = xmlDocument.createElement(Const.Dav.PREFIX + SearchConst.EQ_TAG);
    
    Element propElement = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.PROP);
    eqElement.appendChild(propElement);
    
    Element propertyElement = xmlDocument.createElement(propertyName);
    nsRegistry.fillNameSpaces(propertyElement);
    propElement.appendChild(propertyElement);
    
    Element literalElement = xmlDocument.createElement(Const.Dav.PREFIX + SearchConst.LITERAL_TAG);
    eqElement.appendChild(literalElement);
    
    literalElement.setTextContent(propertyValue);
    
    return eqElement;
  }

//  +       "<D:eq>"
//  +          "<D:prop>"
//  +           "<jcr:primaryType />"
//  +          "</D:prop>"
//  +         "<D:literal>"
//  +           "nt:file"
//  +         "</D:literal>"
//  +       "</D:eq>"
  

}
