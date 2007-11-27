/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.documents;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DocumentManager {
  
  protected static String [][]availableDocuments = {
    { Const.StreamDocs.PROPFIND, "org.exoplatform.frameworks.webdavclient.documents.PropFindDoc" },
    { Const.StreamDocs.MULTISTATUS, "org.exoplatform.frameworks.webdavclient.documents.Multistatus" },
    { Const.StreamDocs.PROP, "org.exoplatform.frameworks.webdavclient.documents.PropDoc" }    
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
      Log.info("Unhandled exception. ", exc);
      exc.printStackTrace();
    }
    
    return null;
  }
  
}
