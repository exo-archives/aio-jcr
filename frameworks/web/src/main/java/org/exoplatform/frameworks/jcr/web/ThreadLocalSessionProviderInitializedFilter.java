/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.auth.AuthenticationService;
import org.exoplatform.services.organization.auth.Identity;

/**
 * Created by The eXo Platform SAS . <br/>
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
  
//  private static final Log LOGGER =
//    ExoLogger.getLogger("ThreadLocalSessionProviderInitializedFilter"); 

  /* (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig config) throws ServletException {
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   * javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {

    ExoContainer container = ExoContainerContext.getCurrentContainer();

    authenticationService = (AuthenticationService) container
        .getComponentInstanceOfType(AuthenticationService.class);
    providerService = (ThreadLocalSessionProviderService) container
        .getComponentInstanceOfType(ThreadLocalSessionProviderService.class);

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
          Credentials credentials = null;
          try {
            if ("BASIC".equals(httpRequest.getAuthType())) {
              String authHeader = httpRequest.getHeader("Authorization");
              if (authHeader != null) {
                String decodedAuth = "";
                String []basic = authHeader.split(" ");
                if (basic.length >= 2 && basic[0].equalsIgnoreCase(HttpServletRequest.BASIC_AUTH)) {
                  decodedAuth = new String(Base64.decodeBase64(basic[1].getBytes()));
                }                 
                String []authParams = decodedAuth.split(":");
                credentials = new SimpleCredentials(authParams[0], authParams[1].toCharArray());                  
              }
            }            
          } catch (Exception exc) {
            throw new ServletException(exc);
          }          
          provider = new SessionProvider(credentials);
        }
      }
    }
    
    if (provider == null) {
      provider = SessionProvider.createAnonimProvider();
    }
    
    providerService.setSessionProvider(null, provider);
    
    chain.doFilter(request, response);

  }
  
  /* (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
  }

}
