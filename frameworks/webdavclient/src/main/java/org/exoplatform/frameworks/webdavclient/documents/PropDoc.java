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

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.XmlUtil;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;
import org.exoplatform.frameworks.webdavclient.properties.PropManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropDoc implements DocumentApi {
  
  protected PropApi singleProperty = null;

  public boolean initFromDocument(Document document) {    
    Node documentNode = XmlUtil.getChildNode(document, Const.StreamDocs.PROP);

    NodeList nodes = documentNode.getChildNodes();
    Node propertyNode = null;
    for (int i = 0; i < nodes.getLength(); i++) {
      Node curNode = nodes.item(i);
      if (curNode.getLocalName() != null && 
          Const.Dav.NAMESPACE.equals(curNode.getNamespaceURI())) {
        propertyNode = curNode;
        break;
      }
    }
    
    if (propertyNode == null) {
      return false;
    }
    
    singleProperty = PropManager.getPropertyByNode(propertyNode, Const.HttpStatus.OK_DESCR);
    
    return false;
  }
  
  public PropApi getSingleProperty() {
    return singleProperty;
  }
  
}
