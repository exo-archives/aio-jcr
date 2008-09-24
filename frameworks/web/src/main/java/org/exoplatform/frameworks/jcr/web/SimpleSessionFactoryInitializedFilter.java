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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.frameworks.jcr.SingleRepositorySessionFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS .
 * 
 * @deprecated use SessionProvider related mechanism instead TODO uses in exo.cs.web.portal
 *             exo.ecm.web.portal
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SimpleSessionFactoryInitializedFilter.java 7163 2006-07-19 07:30:39Z peterit $
 */

public class SimpleSessionFactoryInitializedFilter implements Filter {

  private static Log     log    = ExoLogger.getLogger("jcr.SimpleSessionFactoryInitializedFilter");

  private ServletContext servletContext;

  private String         userId = null;

  public void init(FilterConfig config) throws ServletException {
    servletContext = config.getServletContext();
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                                                                                           ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpSession httpSession = httpRequest.getSession();

    String portalName = servletContext.getServletContextName();
    ExoContainer container = ExoContainerContext.getContainerByName(portalName);
    if (container == null)
      container = ExoContainerContext.getTopContainer();

    ExoContainerContext.setCurrentContainer(container);

    ConversationRegistry stateRegistry = (ConversationRegistry) container.getComponentInstanceOfType(ConversationRegistry.class);

    if (httpRequest.getRemoteUser() != null) {
      if (ConversationState.getCurrent() == null) {
        log.warn("Cannot find the identity for user " + httpRequest.getRemoteUser()
            + ", trying to create the new one");
        ConversationState state = null;
        try {
          state = stateRegistry.getState(httpRequest.getRemoteUser());
          ConversationState.setCurrent(state);
        } catch (Exception e) {
          log.error("Can't find identity by sessionID ", e);
        }
      }
    }
    try {
      if (httpRequest.getSession().getAttribute(SingleRepositorySessionFactory.SESSION_FACTORY) == null
          || userChanged(httpRequest, stateRegistry)) {

        ManageableRepository rep;

        // (1)try to get Repository from JNDI
        String repositoryName = servletContext.getInitParameter(WebConstants.REPOSITORY_JNDI_NAME);

        if (repositoryName != null) {
          try {
            Context ctx = new InitialContext();
            rep = (ManageableRepository) ctx.lookup(repositoryName);
            log.info("Repository found in the JNDI context " + rep + " InitialContextFactory: "
                + System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
            RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
            repositoryService.setCurrentRepositoryName(repositoryName);
          } catch (NamingException e) {
            e.printStackTrace();
            throw new ServletException(e);
          } catch (RepositoryConfigurationException e) {
            e.printStackTrace();
            throw new ServletException(e);
          }

        } else {
          log.info("No Repository object found in the JNDI context. Try to get from container");
          RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
          try {
            rep = service.getDefaultRepository();
          } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
          }
        }

        httpRequest.getSession().setAttribute(SingleRepositorySessionFactory.SESSION_FACTORY,
                                              new SingleRepositorySessionFactory(rep));
      }

      chain.doFilter(request, response);

    } finally {
      PortalContainer.setInstance(null);
    }
  }

  public void destroy() {
  }

  private boolean userChanged(HttpServletRequest httpRequest, ConversationRegistry stateRegistry) {

    String newUser = httpRequest.getRemoteUser();

    boolean res = true;
    if (userId == null) {
      if (newUser == null)
        res = false;
    } else {
      res = !userId.equals(newUser);
    }
    if (res) {
      // refresh sessionContainer
      if (newUser != null) {
        ConversationState state = null;
        try {
          state = stateRegistry.getState(newUser);
          ConversationState.setCurrent(state);
        } catch (Exception e) {
          log.error("Can't find identity by sessionID ", e);
        }
      }
      userId = newUser;
    }
    return res;
  }
}
