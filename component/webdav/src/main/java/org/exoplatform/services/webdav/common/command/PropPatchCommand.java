/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.property.WebDavProperty;
import org.exoplatform.services.webdav.common.request.documents.PropertyUpdateDocument;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.exoplatform.services.webdav.common.response.Href;
import org.exoplatform.services.webdav.common.response.MultiStatus;
import org.exoplatform.services.webdav.common.response.Response;
import org.exoplatform.services.webdav.common.response.ResponseImpl;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PropPatchCommand.java 12635 2007-02-07 12:57:47Z gavrikvetal $
 */

public class PropPatchCommand extends WebDavCommand {
  
  protected boolean process() throws RepositoryException {
    RequestDocument requestDoc = davRequest().getDocumentFromRequest();
    
    if (!(requestDoc instanceof PropertyUpdateDocument)) {
      davResponse().setStatus(DavStatus.PRECONDITION_FAILED);
      return false;
    }
    
    DavResource resource = getResourceFactory().getSrcResource(false);
    
    if (!(resource instanceof AbstractNodeResource)) {
      davResponse().setStatus(DavStatus.FORBIDDEN);
      return false;
    }

    HashMap<String, WebDavProperty> sets = ((PropertyUpdateDocument)requestDoc).getSetList();    
    ArrayList<WebDavProperty> removes = ((PropertyUpdateDocument)requestDoc).getRemoveList();
    
    Response response = new ResponseImpl();
    
    Href href = new Href(davContext(), "/" + davRequest().getSrcWorkspace() + davRequest().getSrcPath());    
    response.setHref(href);
    
    Iterator<String> keyIter = sets.keySet().iterator();
    while (keyIter.hasNext()) {
      String key = keyIter.next();
      WebDavProperty property = sets.get(key);
      property.set(resource);
      response.addProperty(property, false);
    }
    
    for (int i = 0; i < removes.size(); i++) {
      WebDavProperty property = removes.get(i);
      property.remove(resource);
      response.addProperty(property, false);
    }
    
    ArrayList<Response> responses = new ArrayList<Response>();
    responses.add(response);
    
    davResponse().setMultistatus(new MultiStatus(responses));
    return true;        
  }
  
}
