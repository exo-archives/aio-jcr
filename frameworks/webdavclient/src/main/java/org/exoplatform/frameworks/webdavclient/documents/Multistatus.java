/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient.documents;

import java.util.ArrayList;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
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
