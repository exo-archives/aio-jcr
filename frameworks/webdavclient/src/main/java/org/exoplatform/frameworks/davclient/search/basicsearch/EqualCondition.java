/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.search.basicsearch;

import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.Const.Dav;
import org.exoplatform.frameworks.davclient.Const.DavProp;
import org.exoplatform.frameworks.davclient.request.NameSpaceRegistry;
import org.exoplatform.frameworks.davclient.search.SearchConst;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class EqualCondition implements BasicSearchCondition {
  
  private NameSpaceRegistry nsRegistry = new NameSpaceRegistry();
  
  private String propertyName;
  private String propertyValue;
  
  public EqualCondition(String propertyName, String propertyValue) {
    
    if (nsRegistry.registerNameSpace(propertyName)) {
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
