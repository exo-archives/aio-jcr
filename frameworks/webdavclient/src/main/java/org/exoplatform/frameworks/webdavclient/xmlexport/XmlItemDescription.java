/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

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

