/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.search;

import org.exoplatform.frameworks.davclient.request.PropertyList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class AbstractQuery implements DavQuery {

  protected PropertyList properties = new PropertyList();  
  
  public void setRequiredProperty(String propertyName) {
    properties.setProperty(propertyName);
  }
  
  public abstract Element toXml(Document xmlDocument);
  
}
