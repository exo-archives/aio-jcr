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
