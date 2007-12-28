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

package org.exoplatform.frameworks.webdavclient.documents;

import java.util.ArrayList;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.XmlUtil;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class Multistatus implements DocumentApi {

  protected ArrayList<ResponseDoc> responses = new ArrayList<ResponseDoc>(); 
  
  public boolean initFromDocument(Document document) {
    try {      
      Node documentNode = XmlUtil.getChildNode(document, Const.StreamDocs.MULTISTATUS);
      
      NodeList respNodes = documentNode.getChildNodes();
      for (int i = 0; i < respNodes.getLength(); i++) {
        Node curResponse = respNodes.item(i);
        
        String localName = curResponse.getLocalName();
        String nameSpace = curResponse.getNamespaceURI();
        
        if (localName != null && Const.Dav.NAMESPACE.equals(nameSpace)) {
          ResponseDoc response = new ResponseDoc(curResponse);
          responses.add(response);
        }
        
      }
      
      return true;
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage());
      exc.printStackTrace();
    }
    
    return false;
  }
  
  public ArrayList<ResponseDoc> getResponses() {
    return responses;
  }
  
}
