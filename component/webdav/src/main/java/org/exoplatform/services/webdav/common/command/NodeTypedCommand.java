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

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.webdav.WebDavService;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
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
