/***************************************************************************
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavCommandContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: RepositoryResource.java 12234 2007-01-23 12:20:00Z gavrikvetal $
 */

public class RepositoryResource extends CommonResource {
  
  public RepositoryResource(WebDavCommandContext context) {
    super(context);
  }
  
  public boolean isCollection() throws RepositoryException {
    return true;
  }

  public String getName() throws RepositoryException {
    return "repository";
  }  
  
  @Override
  public ArrayList<WebDavResource> getChildResources() throws RepositoryException {
    String []workspaces = context.getAvailableWorkspaces();
    ArrayList<WebDavResource> childs = new ArrayList<WebDavResource>();
    
    for (String workspace : workspaces) {
      childs.add(new WorkspaceResource(context, workspace));
    }
    
    return childs;
  }
  
  public int getChildCount() {
    return context.getAvailableWorkspaces().length;
  }
  
}
