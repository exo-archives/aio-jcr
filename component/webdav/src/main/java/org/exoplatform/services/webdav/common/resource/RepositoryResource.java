/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.common.request.documents.CommonPropDocument;
import org.exoplatform.services.webdav.common.response.Response;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: RepositoryResource.java 12234 2007-01-23 12:20:00Z gavrikvetal $
 */

public class RepositoryResource extends DavCommonResource {
  
  public RepositoryResource(WebDavCommandContext context) {
    super(context);
  }
  
  public boolean isCollection() throws RepositoryException {
    return true;
  }

  public String getName() throws RepositoryException {
    return "";
  }
  
  public DavResourceInfo getInfo() throws RepositoryException {
//    throw new AccessDeniedException();
    DavResourceInfo info = new DavResourceInfoImpl();
//    
//    ArrayList<DavResource> resources = getChildsResources();
//    
//    info.setContentStream(new HtmlBuilder(context).getHtml("/", resources));
//    
    info.setContentStream(new ByteArrayInputStream("".getBytes()));
    return info;
  }  
  
  public Response getResponse(CommonPropDocument reqProps) throws RepositoryException {
    return null;
  }

  public ArrayList<DavResource> getChildsResources() throws RepositoryException {    
    String []workspaces = context.getAvailableWorkspaces();
    ArrayList<DavResource> childs = new ArrayList<DavResource>();
    
    for (String workspace : workspaces) {
      childs.add(new WorkspaceResource(context, workspace));
    }
    
    return childs;
  }
  
  public int getChildCount() {
    return context.getAvailableWorkspaces().length;
  }
  
}
