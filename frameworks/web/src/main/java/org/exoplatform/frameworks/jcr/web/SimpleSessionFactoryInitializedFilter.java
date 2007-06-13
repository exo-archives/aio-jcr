/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.web;

import java.io.IOException;

import javax.jcr.SimpleCredentials;
import javax.jcr.Credentials;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;
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
import org.exoplatform.services.organization.auth.AuthenticationService;
import org.exoplatform.services.organization.auth.Identity;
import org.exoplatform.services.security.SecurityService;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SimpleSessionFactoryInitializedFilter.java 7163 2006-07-19
 *          07:30:39Z peterit $
 */

public class SimpleSessionFactoryInitializedFilter implements Filter {

  private static Log     log    = ExoLogger.getLogger("jcr.SimpleSessionFactoryInitializedFilter");

  // public static final String DEFAULT_USER_NAME =
  // "org.exoplatform.sample.defaultUserName";
  // public static final String DEFAULT_USER_PASSWORD =
  // "org.exoplatform.sample.defaultUserPassword";

  private ServletContext servletContext;

  private String         userId = null;

  public void init(FilterConfig config) throws ServletException {
    servletContext = config.getServletContext();
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpSession httpSession = httpRequest.getSession();

    String portalName = servletContext.getServletContextName();
    ExoContainer container = ExoContainerContext.getContainerByName(portalName);
    if (container == null)
      container = ExoContainerContext.getTopContainer();

    ExoContainerContext.setCurrentContainer(container);

    AuthenticationService authenticationService = (AuthenticationService) container
        .getComponentInstanceOfType(AuthenticationService.class);
    if (httpRequest.getRemoteUser() != null) {
      if (authenticationService.getCurrentIdentity() == null) {
        log.warn("Cannot find the identity for user " + httpRequest.getRemoteUser()
            + ", trying to create the new one");
        Identity identity = null;
        try {
        identity = authenticationService.getIdentityBySessionId(httpRequest.getRemoteUser());
        }catch (Exception e) {
          log.error("Can't find identity by sessionID ", e);
        }
        authenticationService.setCurrentIdentity(identity);
      }
    }
    //
    /*
     * SessionManagerContainer sessionManagerContainer =
     * (SessionManagerContainer) container; SessionManager sessionManager =
     * sessionManagerContainer.getSessionManager(); SessionContainer
     * sessionContainer =
     * sessionManager.getSessionContainer(httpSession.getId()); if
     * (sessionContainer == null) { sessionContainer =
     * sessionManagerContainer.createSessionContainer(httpSession .getId(),
     * "default"); sessionContainer.setClientInfo(new
     * HttpClientInfo(httpRequest)); } // attach to current thread
     * SessionContainer.setInstance(sessionContainer);
     */

    try {
      if (httpRequest.getSession().getAttribute(SingleRepositorySessionFactory.SESSION_FACTORY) == null
          || userChanged(httpRequest, authenticationService)) {

        ManageableRepository rep;
        // cred = new SimpleCredentials(servletContext
        // .getInitParameter(DEFAULT_USER_NAME), servletContext
        // .getInitParameter(DEFAULT_USER_PASSWORD).toCharArray());

        // (1)try to get Repository from JNDI
        String repositoryName = servletContext.getInitParameter(WebConstants.REPOSITORY_JNDI_NAME);

        if (repositoryName != null) {
          try {
            Context ctx = new InitialContext();
            rep = (ManageableRepository) ctx.lookup(repositoryName);
            log.info("Repository found in the JNDI context " + rep + " InitialContextFactory: "
                + System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
            RepositoryService repositoryService = (RepositoryService) container
            .getComponentInstanceOfType(RepositoryService .class);
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
          RepositoryService service = (RepositoryService) container
              .getComponentInstanceOfType(RepositoryService.class);
          try {
            rep = service.getDefaultRepository();
          } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
          }
        }

        // httpRequest.getSession().setAttribute(
        // SingleRepositorySessionFactory.SESSION_FACTORY,
        // new SingleRepositorySessionFactory(rep, cred));
        httpRequest.getSession().setAttribute(SingleRepositorySessionFactory.SESSION_FACTORY,
            new SingleRepositorySessionFactory(rep));
            //new SingleRepositorySessionFactory(rep, new Credentials("admin", "admin".toCharArray())));
      }

      chain.doFilter(request, response);

    } finally {
      // SessionContainer.setInstance(null);
      PortalContainer.setInstance(null);
    }

  }

  public void destroy() {
  }

  // private boolean userChanged(HttpServletRequest httpRequest,
  // SessionContainer sessionContainer) {
  private boolean userChanged(HttpServletRequest httpRequest,
      AuthenticationService authenticationService) {

    String newUser = httpRequest.getRemoteUser();

    boolean res = true;
    if (userId == null) {
      if (newUser == null)
        res = false;
    } else {
      res = !userId.equals(newUser);
    }
    // System.out.println("---------------- "+res+" "+userId+" "+newUser);
    if (res) {
      // refresh sessionContainer
      if (newUser != null) {
        Identity identity = null;
        try {
        identity = authenticationService.getIdentityBySessionId(newUser);
        }catch (Exception e) {
          log.error("Can't find identity by sessionID ", e);
        }
        authenticationService.setCurrentIdentity(identity);
      }
      // securityService.setCurrentUser(newUser);
      /*
       * sessionContainer.setClientInfo(new HttpClientInfo(httpRequest));
       * SessionContainer.setInstance(sessionContainer);
       */
      userId = newUser;
    }
    return res;
  }

}
