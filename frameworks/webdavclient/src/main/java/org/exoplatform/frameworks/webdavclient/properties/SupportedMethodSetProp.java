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

package org.exoplatform.frameworks.webdavclient.properties;

import java.util.ArrayList;

import org.exoplatform.frameworks.webdavclient.Const;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SupportedMethodSetProp extends CommonProp {
  
  private ArrayList<String> methods = new ArrayList<String>();

  public SupportedMethodSetProp() {
    this.propertyName = Const.DavProp.SUPPORTEDMETHODSET;
  }
  
  @Override
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }

    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node childNode = nodes.item(i);
      
      String childNodeName = childNode.getLocalName();
      if (childNodeName == null) {
        continue;
      }
    
      if (Const.DavProp.SUPPORTEDMETHOD.equals(childNodeName) &&
          Const.Dav.NAMESPACE.equals(childNode.getNamespaceURI())) {
        String curMethod = childNode.getAttributes().getNamedItem(Const.DavProp.NAME).getTextContent();
        methods.add(curMethod);
      }

    }
    
    return true;
  }
  
  public ArrayList<String> getMethods() {
    return methods;
  }
  
}
