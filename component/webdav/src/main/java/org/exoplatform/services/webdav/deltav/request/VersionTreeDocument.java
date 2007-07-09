/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.deltav.request;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.request.documents.PropertyRequiredDocument;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: VersionTreeDoc.java 12134 2007-01-20 15:50:13Z gavrikvetal $
 */

public class VersionTreeDocument extends PropertyRequiredDocument {
  
  public VersionTreeDocument() {    
    xmlName = DavConst.DavDocument.VERSIONTREE;    
  }
  
}
