/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.report.versiontree;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.request.documents.CommonPropDocument;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.MultiStatusResponse;
import org.exoplatform.services.webdav.common.response.ResponseBuilder;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VersionTreeResponseBuilder extends ResponseBuilder {
  
  public VersionTreeResponseBuilder(WebDavResource rootResource, CommonPropDocument reqProps) {
    super(rootResource, reqProps);
  }
  
  public ArrayList<MultiStatusResponse> getVersionResponses() throws RepositoryException {
    ArrayList<MultiStatusResponse> responses = new ArrayList<MultiStatusResponse>();
    
    if (!(rootResource instanceof DeltaVResource)) {
      return responses;
    }
    
    ArrayList<WebDavResource> resources = ((DeltaVResource)rootResource).getChildsVersions();
    
    for (int i = 0; i < resources.size(); i++) {
      WebDavResource curResource = resources.get(i);      
      responses.add(getResponse(curResource));
    }    
    
    return responses;
  }  
  
}
