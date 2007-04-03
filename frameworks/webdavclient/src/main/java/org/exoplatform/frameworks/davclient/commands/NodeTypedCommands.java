/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.commands;

import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.WebDavContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class NodeTypedCommands extends DavCommand {
  
  protected String nodeType;
  protected ArrayList<String> mixTypes = new ArrayList<String>();
  
  public NodeTypedCommands(WebDavContext context) throws Exception {
    super(context);    
  }
  
  public void finalExecute() {
  }
  
  public Element toXml(Document xmlDocument) {
    return null;
  }
  
  public void setNodeType(String nodeType) throws Exception {
    this.nodeType = nodeType; 
  }
  
  public void setMixType(String mixType) throws Exception {
    mixTypes.add(mixType);    
  }
  
  @Override
  public int execute() throws Exception {
    if (nodeType != null) {
      String nodeTypeHeader = new String(Base64.encodeBase64(nodeType.getBytes()));
      client.setRequestHeader(Const.HttpHeaders.NODETYPE, nodeTypeHeader);
    }
    
    String mixTypeHeader = "";
    for (int i = 0; i < mixTypes.size(); i++) {
      String mixType = mixTypes.get(i); 
      mixTypeHeader += mixType;
      if (i != mixTypes.size() - 1) {
        mixTypeHeader += ";";
      }
    }
    
    if (!"".equals(mixTypeHeader)) {
      mixTypeHeader = new String(Base64.encodeBase64(mixTypeHeader.getBytes()));
      client.setRequestHeader(Const.HttpHeaders.MIXTYPE, mixTypeHeader);
    }
    
    return super.execute();
  }
  
}
