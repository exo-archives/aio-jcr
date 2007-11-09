/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.commands;

import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.exoplatform.frameworks.httpclient.HttpHeader;
import org.exoplatform.frameworks.webdavclient.WebDavContext;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class NodeTypedCommands extends DavCommand {
  
  protected String nodeType;
  protected ArrayList<String> mixTypes = new ArrayList<String>();
  
  public NodeTypedCommands(WebDavContext context) throws Exception {
    super(context);    
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
      client.setRequestHeader(HttpHeader.NODETYPE, nodeTypeHeader);
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
      client.setRequestHeader(HttpHeader.MIXTYPE, mixTypeHeader);
    }
    
    return super.execute();
  }
  
}
