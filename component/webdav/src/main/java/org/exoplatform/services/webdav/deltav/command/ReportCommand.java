/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.deltav.command;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.MultiStatus;
import org.exoplatform.services.webdav.common.response.Response;
import org.exoplatform.services.webdav.deltav.request.VersionTreeDocument;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;
import org.exoplatform.services.webdav.deltav.response.DeltaVResponseBuilder;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: ReportCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class ReportCommand extends WebDavCommand {

  protected boolean process() throws RepositoryException {    
    RequestDocument requestDoc = davRequest().getDocumentFromRequest();
    
    if (!(requestDoc instanceof VersionTreeDocument)) {
      davResponse().answerPreconditionFailed();
      return false;
    }
    
    WebDavResource resource = getResourceFactory().getSrcResource(false);
    
    if (!(resource instanceof DeltaVResource)) {
      davResponse().answerForbidden();
      return false;
    }
    
    int depth = davRequest().getDepth();
    
    DeltaVResponseBuilder builder = new DeltaVResponseBuilder(resource, (VersionTreeDocument)requestDoc);
    ArrayList<Response> responses = builder.getVersionResponses();
    
    MultiStatus multistatus = new MultiStatus(responses);    
    davResponse().setMultistatus(multistatus);      
    return true;
  }
  
}
