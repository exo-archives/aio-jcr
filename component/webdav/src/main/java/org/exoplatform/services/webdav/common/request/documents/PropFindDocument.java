/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.request.documents;

import org.exoplatform.services.webdav.DavConst;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PropFindDoc.java 12134 2007-01-20 15:50:13Z gavrikvetal $
 */

public class PropFindDocument extends PropertyRequiredDocument {

  public PropFindDocument() {
    super();
    xmlName = DavConst.DavDocument.PROPFIND;
  }  
  
}
