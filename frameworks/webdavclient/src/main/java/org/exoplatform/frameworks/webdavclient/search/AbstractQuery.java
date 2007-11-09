/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.search;

import org.exoplatform.frameworks.webdavclient.request.PropertyList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class AbstractQuery implements DavQuery {

  protected PropertyList properties = new PropertyList();  
  
  public void setRequiredProperty(String name, String nameSpace) {
    properties.setProperty(name, nameSpace);
  }
  
  public abstract Element toXml(Document xmlDocument);
  
}
