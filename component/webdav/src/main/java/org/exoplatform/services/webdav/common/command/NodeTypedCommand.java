/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.webdav.WebDavService;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class NodeTypedCommand extends WebDavCommand {
  
  public NodeTypedCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }  

  protected String getNodeType(String nodeTypeHeader) {
    if (nodeTypeHeader == null) {
      return null;
    }
    String nodeType = new String(Base64.decodeBase64(nodeTypeHeader.getBytes()));
    return nodeType;
  }
  
  protected ArrayList<String> getMixinTypes(String mixinTypeHeader) {
    ArrayList<String> mixins = new ArrayList<String>();
    if (mixinTypeHeader == null) {
      return mixins;
    }
    
    String mixTypes =  new String(Base64.decodeBase64(mixinTypeHeader.getBytes()));
    
    String []mixType = mixTypes.split(";");
     
    for (int i = 0; i < mixType.length; i++) {
      String curMixType = mixType[i];
      if ("".equals(curMixType)) {
        continue;
      }
      mixins.add(curMixType);
    }
    
    return mixins;
  }  
  
}
