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
 * @version $Id: RequestDocument.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public interface RequestDocument {

  //void initFactory(PropertyFactory propertyFactory);
  
  boolean init(Document requestDocument, PropertyFactory propertyFactory);
  
}
