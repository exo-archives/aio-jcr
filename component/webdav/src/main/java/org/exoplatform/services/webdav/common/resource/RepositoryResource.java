/***************************************************************************
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.util.ArrayList;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: RepositoryResource.java 12234 2007-01-23 12:20:00Z gavrikvetal $
 */

public class RepositoryResource extends AbstractWebDavResource {
  
  private static Log log = ExoLogger.getLogger("jcr.RepositoryResource");
  
  protected String repoName;
  
  protected SessionProvider sessionProvider;
  
  public RepositoryResource(
      WebDavService webDavService, 
      String rootHref, 
      String repoName,
      SessionProvider sessionProvider) {
    
    super(webDavService, rootHref);
    
    this.repoName = repoName;
    this.sessionProvider = sessionProvider;
  }
  
  public boolean isCollection() throws RepositoryException {
    return true;
  }

  public String getName() throws RepositoryException {
    return repoName;
  }  
  
  @Override
  public ArrayList<WebDavResource> getChildResources() throws RepositoryException {    
    String []workspaces = webDavService.getRepository().getWorkspaceNames();    
    
    ArrayList<WebDavResource> childs = new ArrayList<WebDavResource>();
        
    for (String workspace : workspaces) {
      
      String childHref = getHref() + "/" + workspace;
      
      try {
        Session childSession = null;
        
        try {
          childSession = sessionProvider.getSession(workspace, webDavService.getRepository());
        } catch (LoginException exc) {
        }
        
        WebDavResource childResource = new WorkspaceResource(
            webDavService,
            childHref,
            workspace,
            childSession            
            );
        
        childs.add(childResource);
        
      } catch (Exception exc) {
        log.info("Unhandled exception. " + exc.getMessage(), exc);
      }
    }
    
    return childs;
  }  
  
}
