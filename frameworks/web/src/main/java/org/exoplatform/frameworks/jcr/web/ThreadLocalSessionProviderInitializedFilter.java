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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS . <br/> Checks out if there are
 * SessionProvider istance in current thread using
 * ThreadLocalSessionProviderService, if no, initializes it getting current
 * credentials from AuthenticationService and initializing
 * ThreadLocalSessionProviderService with newly created SessionProvider
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ThreadLocalSessionProviderInitializedFilter implements Filter {

  private ConversationRegistry stateRegistry;

  private SessionProviderService providerService;

  private static final Log log = ExoLogger
      .getLogger("jcr.ThreadLocalSessionProviderInitializedFilter");

  /*
   * (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig config) throws ServletException {
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    ExoContainer container = ExoContainerContext.getCurrentContainer();

    providerService = (SessionProviderService) container
        .getComponentInstanceOfType(SessionProviderService.class);
    stateRegistry = (ConversationRegistry) container
        .getComponentInstanceOfType(ConversationRegistry.class);

    HttpServletRequest httpRequest = (HttpServletRequest) request;

    ConversationState state = ConversationState.getCurrent();
    SessionProvider provider = null;

    // NOTE not create new HTTP session, if session is not created yet
    // this means some settings is incorrect, see web.xml for filter
    // org.exoplatform.services.security.web.SetCurrentIdentityFilter
    HttpSession httpsession = httpRequest.getSession(false); 
    if (state == null) {
      if (log.isDebugEnabled())
        log.debug("Current conversation state is not set");
      
      if (httpsession != null) { 
        String httpsessionId = httpsession.getId();
        // initialize thread local SessionProvider
        state = stateRegistry.getState(httpsessionId);
        if (state != null)
          provider = new SessionProvider(state);
        else if (log.isDebugEnabled())
          log.debug("WARN: Conversation State is null, id  " + httpsessionId);
      }
    } else {
      provider = new SessionProvider(state);
    }

    if (provider == null) {
      if (log.isDebugEnabled())
        log.debug("Create SessionProvider for anonymous.");
      provider = SessionProvider.createAnonimProvider();
    }
    if (ConversationState.getCurrent() != null)
      ConversationState.getCurrent().setAttribute(SessionProvider.SESSION_PROVIDER, provider);
    providerService.setSessionProvider(null, provider);
    
    chain.doFilter(request, response);
    
    // remove SessionProvider
    if (ConversationState.getCurrent() != null)
      ConversationState.getCurrent().removeAttribute(SessionProvider.SESSION_PROVIDER);
    
    providerService.removeSessionProvider(null);
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
  }

}
