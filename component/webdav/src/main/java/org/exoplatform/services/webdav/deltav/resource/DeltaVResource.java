/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.resource;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.common.request.documents.CommonPropDocument;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.response.Response;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class DeltaVResource extends AbstractNodeResource {
  
  public DeltaVResource(WebDavCommandContext context, Node node) throws RepositoryException {
    super(context, node);
  }
  
  public ArrayList<DavResource> getChildsVersions() throws RepositoryException {    
    ArrayList<DavResource> resources = new ArrayList<DavResource>();
    
    VersionHistory vHistory = getNode().getVersionHistory();
    VersionIterator vIter = vHistory.getAllVersions();
    
    while (vIter.hasNext()) {
      Version version = vIter.nextVersion();
      
      if (DavConst.NodeTypes.JCR_ROOTVERSION.equals(version.getName())) {
        continue;
      }
      
      VersionResource resource = new VersionResource(context, version, this);
      resources.add(resource);
    }
    
    return resources;
  }
  
  public ArrayList<Response> getVersionResponces(CommonPropDocument reqProps, int depth) throws RepositoryException {
    
    ArrayList<Response> responses = new ArrayList<Response>();
    
    ArrayList<DavResource> versions = getChildsVersions();
    
    for (int i = 0; i < versions.size(); i++) {
      Response response = versions.get(i).getResponse(reqProps);
      
      if (response != null) {
        responses.add(response);
      }      
    }
    
    return responses;
  }
  
}
