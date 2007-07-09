/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.command;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: HeadCommand.java 12787 2007-02-13 12:13:17Z gavrikvetal $
 */

public class HeadCommand extends WebDavCommand {
  
  protected boolean process() throws Exception {
    WebDavResource resource = getResourceFactory().getSrcResource(false);
    
    ResourceData resourceData = resource.getResourceData();
    
    davResponse().setResponseHeader(DavConst.Headers.LASTMODIFIED, resourceData.getLastModified());
    davResponse().setResponseHeader(DavConst.Headers.CONTENTTYPE, resourceData.getContentType());

    if (!resourceData.isCollection()) {
      davResponse().setResponseHeader(DavConst.Headers.CONTENTLENGTH, "" + resourceData.getContentLength());
    }
    
    davResponse().answerOk();
    return true;    
  }  

}
