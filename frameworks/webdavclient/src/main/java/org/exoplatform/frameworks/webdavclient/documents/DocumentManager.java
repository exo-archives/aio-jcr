/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient.documents;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DocumentManager {
  
  protected static String [][]availableDocuments = {
    { Const.StreamDocs.PROPFIND, PropFindDoc.class.getCanonicalName() },
    { Const.StreamDocs.MULTISTATUS, Multistatus.class.getCanonicalName() },
    { Const.StreamDocs.PROP, PropDoc.class.getCanonicalName() }
  };

  public static DocumentApi getResponseDocument(InputStream inStream) {
    if (inStream == null) {
      return null;
    }

    Document document = null;
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      document = builder.parse(inStream);
      
      NodeList nodeList = document.getChildNodes();
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node curDocumentNode = nodeList.item(i);
        
        String localName = curDocumentNode.getLocalName();
        String nameSpace = curDocumentNode.getNamespaceURI();
        
        if (localName != null && Const.Dav.NAMESPACE.equals(nameSpace)) {

          for (int docI = 0; docI < availableDocuments.length; docI++) {
            if (localName.equals(availableDocuments[docI][0])) {
              DocumentApi responseDoc = (DocumentApi)Class.forName(availableDocuments[docI][1]).newInstance();
              responseDoc.initFromDocument(document);
              return responseDoc;
            }
          }
          
        }
      }
      
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage());
      exc.printStackTrace();
    }
    
    return null;
  }
  
}
