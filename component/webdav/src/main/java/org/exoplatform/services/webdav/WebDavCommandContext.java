/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav;

import java.util.ArrayList;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.web.servlet.ServletWebContext;
import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.request.WebDavRequest;
import org.exoplatform.services.webdav.common.request.WebDavRequestImpl;
import org.exoplatform.services.webdav.common.response.WebDavResponse;
import org.exoplatform.services.webdav.config.WebDavConfig;
import org.exoplatform.services.webdav.lock.FakeLockTable;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WebDavCommandContext.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class WebDavCommandContext extends ServletWebContext {
  
  private static Log log = ExoLogger.getLogger("jcr.DavContext");

  protected WebDavService davService;
  protected WebDavRequest davRequest;
  protected WebDavResponse davResponse;
  
  protected WebDavSessionProvider sessionProvider;
  
  public WebDavCommandContext(
      ServletContext context, 
      HttpServletRequest request, 
      HttpServletResponse response,
      WebDavService davService) throws Exception {
  
    super(context, request, response);
  
    this.davService = davService;
    
    davRequest = new WebDavRequestImpl(request, davService.getPropertyFactory());
    davResponse = new WebDavResponse(response);

    Repository repository = davService.getRepository();
    sessionProvider = new WebDavSessionProvider(davService, (Repository)davService.getRepository());    
  }

  public WebDavConfig getConfig() {
    return davService.getConfig();
  }
  
  public WebDavSessionProvider getSessionProvider() {
    return sessionProvider;
  }
  
  public FakeLockTable getLockTable() {
    return davService.getLockTable();
  }
  
  public WebDavRequest getWebDavRequest() {
    return davRequest;
  }
  
  public WebDavResponse getWebDavResponse() {
    return davResponse;
  }
  
  public ArrayList<String> getAvailableCommands() {
    return davService.getAvailableCommands();
  }
  
  public String [] getAvailableWorkspaces() {
    try {
      return davService.getRepository().getWorkspaceNames();
    } catch (RepositoryException exc) {
      log.info("Unhandled exception. " + exc.getMessage());
    } catch (RepositoryConfigurationException exc) {
      log.info("Unhandled exception. " + exc.getMessage());
    }
    return null;
  }
 
}
