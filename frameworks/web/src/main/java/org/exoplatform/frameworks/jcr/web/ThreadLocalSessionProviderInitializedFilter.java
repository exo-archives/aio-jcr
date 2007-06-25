/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.auth.AuthenticationService;
import org.exoplatform.services.organization.auth.Identity;

/**
 * Created by The eXo Platform SARL . <br/>
 * Checks out if there are SessionProvider istance in current thread
 * using ThreadLocalSessionProviderService, if no, initializes it getting
 * current credentials from AuthenticationService and initializing 
 * ThreadLocalSessionProviderService with  newly created SessionProvider  
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ThreadLocalSessionProviderInitializedFilter implements Filter {

  private AuthenticationService authenticationService;

  private ThreadLocalSessionProviderService providerService;

  /* (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig config) throws ServletException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();

    authenticationService = (AuthenticationService) container
        .getComponentInstanceOfType(AuthenticationService.class);
    providerService = (ThreadLocalSessionProviderService) container
        .getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;

    String user = httpRequest.getRemoteUser();
    SessionProvider provider = providerService.getSessionProvider(null);
    // is there SessionProvider in current thread?
    if (provider == null) {
      // initialize thread local SessionProvider
      if (user != null) {
        Identity identity = null;
        try {
          identity = authenticationService.getIdentityBySessionId(user);
        } catch (Exception e) {
          throw new ServletException(e);
        }
        if(identity != null) {
          provider = new SessionProvider(null);
        }
      }
    }
    
    if (provider == null)
      provider = SessionProvider.createAnonimProvider();

    providerService.setSessionProvider(null, provider);

    chain.doFilter(request, response);

  }
  
  /* (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
  }

}
