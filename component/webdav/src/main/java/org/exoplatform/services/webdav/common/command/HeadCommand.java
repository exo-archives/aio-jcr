/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.command;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.resource.DavResourceInfo;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: HeadCommand.java 12787 2007-02-13 12:13:17Z gavrikvetal $
 */

public class HeadCommand extends WebDavCommand {
  
  protected boolean process() throws RepositoryException {
    DavResource resource = getResourceFactory().getSrcResource(false);
    
    DavResourceInfo info = resource.getInfo();
    
    davResponse().setResponseHeader(DavConst.Headers.LASTMODIFIED, info.getLastModified());
    davResponse().setResponseHeader(DavConst.Headers.CONTENTTYPE, info.getContentType());
    
    if (!info.getType()) {
      davResponse().setResponseHeader(DavConst.Headers.CONTENTLENGTH, "" + info.getContentLength());
    }
    
    davResponse().answerOk();
    return true;    
  }  

}
