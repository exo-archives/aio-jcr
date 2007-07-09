/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.request.documents;

import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PropertyBehaviorDoc.java 12134 2007-01-20 15:50:13Z gavrikvetal $
 */

public class PropertyBehaviorDocument implements RequestDocument {

  public PropertyBehaviorDocument() {
  }
  
  public boolean init(Document requestDocument, PropertyFactory propertyFactory) {
    return false;
  }
 
//<?xml version="1.0"?>
//<A:propertybehavior xmlns:A="DAV:">
//    <A:keepalive>*</A:keepalive>
//</A:propertybehavior>
  
}
