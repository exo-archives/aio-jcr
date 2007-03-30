/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.request.documents.PropFindDocument;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.response.MultiStatus;
import org.exoplatform.services.webdav.common.response.Response;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PropfindCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class PropfindCommand extends WebDavCommand {
  
  protected boolean process() throws RepositoryException {
    RequestDocument reqDoc = davRequest().getDocumentFromRequest();
    if (!(reqDoc instanceof PropFindDocument)) {      
      davResponse().answerNotFound();
      return false;
    }
    
    DavResource davResource = getResourceFactory().getSrcResource(false);
    
    int depth = davRequest().getDepth();
    
    ArrayList<Response> responses = davResource.getChildsResponses((PropFindDocument)reqDoc, depth);
    
    MultiStatus multistatus = new MultiStatus(responses);
    
    davResponse().setMultistatus(multistatus);      
    
    return true;
  }  
  
}
