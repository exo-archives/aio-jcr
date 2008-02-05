/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
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
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.auth.AuthenticationService;
import org.exoplatform.services.organization.auth.Identity;

/**
 * Checks out if username present in HttpServletRequest then initializes 
 * SessionProvider by getting current credentials from AuthenticationService
 * and keeps SessionProvider in ThreadLocalSessionProviderService.
 * Otherwise redirect request to alternative URL. Alternative web application
 * can ask about authentication again or not and gives or denies access to
 * requested resource. 
 * Filter requires parameter <code>context-name</code>, otherwise 
 * ServletException will be thrown.
 *   
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class AnonymousUserContextRedirectionFilter implements Filter {
  
  private final static String CONTEXT_NAME_PARAMETER = "context-name";
  
  private final static Log LOGGER = ExoLogger.getLogger("ws.AnonymousUserContextRedirectionFilter");
  
  private String contextName;
  
  private AuthenticationService authenticationService;

  private ThreadLocalSessionProviderService providerService;
  
  /* (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   * javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    
    authenticationService = (AuthenticationService) container
        .getComponentInstanceOfType(AuthenticationService.class);
    providerService = (ThreadLocalSessionProviderService) container
        .getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String user = httpRequest.getRemoteUser();

    SessionProvider provider = null;

    LOGGER.debug("Current user : " + user);
    
    // initialize thread local SessionProvider
    if (user != null) {
      Identity identity = null;
      try {
        identity = authenticationService.getIdentityBySessionId(user);
      } catch (Exception e) {
        throw new ServletException(e);
      }
      
      if(identity != null)       
        provider = new SessionProvider(null);
      else {
        LOGGER.warn("Identity is null from Authentication Service for " 
            + user + ".");
        provider = SessionProvider.createAnonimProvider();
      }
      
      providerService.setSessionProvider(null, provider);
      filterChain.doFilter(request, response);
      // remove session provider
      providerService.removeSessionProvider(null);
    } else {
      String pathInfo = httpRequest.getPathInfo();
      String query = httpRequest.getQueryString();
      ((HttpServletResponse) response).sendRedirect(
          contextName + "/" + pathInfo + ((query != null) ? "?" + query : ""));
    }

  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig filterConfig) throws ServletException {
    contextName = filterConfig.getInitParameter(CONTEXT_NAME_PARAMETER);
    if (contextName == null) {
      LOGGER.error("AnonymousUserContextRedirectionFilter is not deployed. Set Init-param '"
          + CONTEXT_NAME_PARAMETER
          + " pointed to the target context name in the web.xml");
      throw new ServletException("Filter error. Init-param '" + CONTEXT_NAME_PARAMETER + "' is null.");
    }
  }

}

