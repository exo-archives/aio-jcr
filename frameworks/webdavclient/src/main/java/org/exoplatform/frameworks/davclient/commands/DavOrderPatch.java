/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.commands;

import java.util.ArrayList;

import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.ServerLocation;
import org.exoplatform.frameworks.davclient.order.DOrderMember;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavOrderPatch extends MultistatusCommand {

  private ArrayList<DOrderMember> members = new ArrayList<DOrderMember>();
  
  public DavOrderPatch(ServerLocation location) throws Exception {
    super(location);
    commandName = Const.DavCommand.ORDERPATCH;
  }
  
  public void addMember(DOrderMember member) {
    members.add(member);
  }
  
  public Document toXml(Document xmlDocument) {    
    Element orderPatchEl = xmlDocument.createElementNS(Const.Dav.NAMESPACE, 
        Const.Dav.PREFIX + Const.StreamDocs.ORDERPATCH);
    xmlDocument.appendChild(orderPatchEl);
  
    for (int i = 0; i < members.size(); i++) {
      DOrderMember member = members.get(i);
      member.serialize(xmlDocument, orderPatchEl);
    }
    
    return xmlDocument;
  }  

}
