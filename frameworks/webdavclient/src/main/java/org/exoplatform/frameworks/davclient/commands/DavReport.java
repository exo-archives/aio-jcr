/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient.commands;

import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.ServerLocation;
import org.exoplatform.frameworks.davclient.Const.StreamDocs;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavReport extends MultistatusCommand {

//  protected static final String []defReqProps = {
//    Const.DavProp.VERSIONNAME,
//    Const.DavProp.CREATORDISPLAYNAME,
//    Const.DavProp.GETLASTMODIFIED,
//    Const.DavProp.GETCONTENTLENGTH,
//    Const.DavProp.SUCCESSORSET,
//    Const.DavProp.CHECKEDIN,
//    Const.DavProp.CHECKEDOUT
//  };
  
  public DavReport(ServerLocation location) throws Exception {
    super(location);
    commandName = Const.DavCommand.REPORT;
    xmlName = Const.StreamDocs.VERSION_TREE;
  }
  
//  public Document toXml(Document xmlDocument) {
//    Element versionTreeEl = xmlDocument.createElementNS(Const.Dav.NAMESPACE,
//          Const.Dav.PREFIX + Const.StreamDocs.VERSION_TREE);
//    xmlDocument.appendChild(versionTreeEl);
//    
//    Element propEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.PROP);
//    versionTreeEl.appendChild(propEl);
//    
//    if (isRequireAllProps) {
//      Element allPropEl = xmlDocument.createElement(Const.Dav.PREFIX + Const.DavProp.ALLPROP);
//      propEl.appendChild(allPropEl);
//    } else {
//      for (int i = 0; i < requiredProperties.size(); i++) {
//        String curPropName = requiredProperties.get(i);
//        Element propValEl = xmlDocument.createElement(Const.Dav.PREFIX + curPropName);
//        propEl.appendChild(propValEl);
//      }
//    }
//    
//    return xmlDocument;    
//  }
  
}
