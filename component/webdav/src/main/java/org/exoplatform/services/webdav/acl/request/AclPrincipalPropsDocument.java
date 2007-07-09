/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.acl.request;

import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: AclPrincipalPropsDoc.java 12134 2007-01-20 15:50:13Z gavrikvetal $
 */

public class AclPrincipalPropsDocument implements RequestDocument {

  public AclPrincipalPropsDocument() {
  }
  
  public boolean init(Document requestDocument, PropertyFactory propertyFactory) {
    return false;
  }  
  
}
