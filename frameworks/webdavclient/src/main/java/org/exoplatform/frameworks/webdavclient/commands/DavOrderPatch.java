/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.commands;

import java.util.ArrayList;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.order.DOrderMember;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavOrderPatch extends MultistatusCommand {

  private ArrayList<DOrderMember> members = new ArrayList<DOrderMember>();
  
  public DavOrderPatch(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.ORDERPATCH;
  }
  
  public void addMember(DOrderMember member) {
    members.add(member);
  }
  
  public Element toXml(Document xmlDocument) {
    Element orderPatchEl = xmlDocument.createElementNS(Const.Dav.NAMESPACE, 
        Const.Dav.PREFIX + Const.StreamDocs.ORDERPATCH);
    xmlDocument.appendChild(orderPatchEl);
  
    for (int i = 0; i < members.size(); i++) {
      DOrderMember member = members.get(i);
      member.serialize(xmlDocument, orderPatchEl);
    }
    
    return orderPatchEl;
  }  

}
