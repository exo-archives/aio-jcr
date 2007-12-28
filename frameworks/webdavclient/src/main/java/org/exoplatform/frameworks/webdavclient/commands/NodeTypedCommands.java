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

package org.exoplatform.frameworks.webdavclient.commands;

import java.util.ArrayList;

import org.apache.commons.codec.binary.Base64;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.http.HttpHeader;

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
