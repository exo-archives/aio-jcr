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

package org.exoplatform.frameworks.webdavclient.xmlexport;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class XmlItemDescription {
  
  public static final String PREFIX = "sv:";
  public static final String XML_NODE = "node";
  public static final String XML_PROPERTY = "property";
  
  public static final String XML_NAME = "sv:name";
  public static final String XML_HREF = "xlink:href";

  private String name;
  private String href;  
  
  public XmlItemDescription(Node node) {
    NamedNodeMap attributes = node.getAttributes();
    name = attributes.getNamedItem(XML_NAME).getTextContent();
    href = attributes.getNamedItem(XML_HREF).getTextContent();
  }

  public String getName() {
    return name;
  }
  
  public String getHref() {
    return href;
  }  
  
}

