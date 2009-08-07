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
package org.exoplatform.applications.jcr.browser;

import java.io.IOException;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.frameworks.jcr.web.WebConstants;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>
 * 
 * Date: 27.05.2008 <br/>
 * 
 * Inits JCRBrowser instance in HTTP session.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class JCRBrowserFilter implements Filter {

  private static final Log LOG = ExoLogger.getLogger("jcr.JCRBrowserFilter");

  public void doFilter(ServletRequest servletRequest,
                       ServletResponse servletResponse,
                       FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
    httpRequest.setCharacterEncoding("UTF-8");

    JCRBrowser jcrBrowser = (JCRBrowser) httpRequest.getSession().getAttribute("browser");

    ExoContainer container = (ExoContainer) httpRequest.getSession()
                                                       .getServletContext()
                                                       .getAttribute(WebConstants.EXO_CONTAINER);
    if (container == null) {
      String portalName = httpRequest.getSession().getServletContext().getServletContextName();
      container = RootContainer.getInstance().getPortalContainer(portalName);
    }

    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);

    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);

    try {

      if (jcrBrowser != null && jcrBrowser.getNode() != null) {
        // navigate through JCR Repository

        String repositoryName = (String) httpRequest.getParameter("repositoryName");
        String workspaceName = (String) httpRequest.getParameter("workspaceName");

        // check if browser related to repository/workspace given in attrs
        if (repositoryName != null
            && !jcrBrowser.getRepository().getConfiguration().getName().equals(repositoryName)) {
          // ask repositoryService and if not found lookup JNDI by given name
          try {
            jcrBrowser.setRepository(repositoryService.getRepository(repositoryName));
          } catch (RepositoryException e) {
            if (e.getMessage().indexOf("not found") > 0) {
              // check in JNDI
              LOG.warn("Repository '" + repositoryName
                  + "' is not local. Trying JNDI lookup with the name.");
              ManageableRepository jndiRepo;
              try {
                InitialContext ctx = new InitialContext();
                Object obj = ctx.lookup(repositoryName);
                if (obj instanceof ManageableRepository) {
                  jndiRepo = (ManageableRepository) obj;
                } else {
                  obj = ctx.lookup("java:comp/env/" + repositoryName);
                  if (obj instanceof ManageableRepository) {
                    jndiRepo = (ManageableRepository) obj;
                  } else {
                    LOG.warn("Can't cast object " + obj + " as ManageableRepository class object");
                    jndiRepo = null;
                  }
                }
                if (jndiRepo == null)
                  jcrBrowser.addError(e);
                else
                  jcrBrowser.setRepository(jndiRepo);
              } catch (NamingException jndie) {
                LOG.warn("Repository not bound in JNDI with one of names '" + repositoryName
                    + "', 'java:comp/env/" + repositoryName + "' or can't be connected.", jndie);
                try {
                  InitialContext ctx = new InitialContext();
                  Object obj = ctx.lookup("java:comp/env/jcr/" + repositoryName);
                  if (obj instanceof ManageableRepository) {
                    jndiRepo = (ManageableRepository) obj;
                  } else {
                    LOG.warn("Can't cast object " + obj + " as ManageableRepository class object");
                    jndiRepo = null;
                  }
                  if (jndiRepo == null) {
                    jcrBrowser.addError(e);
                    jcrBrowser.addError(jndie);
                  } else
                    jcrBrowser.setRepository(jndiRepo);
                } catch (NamingException jndie1) {
                  LOG.warn("Repository not bound in JNDI with name 'java:comp/env/jcr/"
                      + repositoryName + "' or can't be connected.", jndie1);
                  jcrBrowser.addError(e);
                  jcrBrowser.addError(jndie);
                  jcrBrowser.addError(jndie1);
                }
              }
            }
          }
        }

        if (jcrBrowser.getRepository() != null) {

          if (workspaceName != null
              && !jcrBrowser.getSession().getWorkspace().getName().equals(workspaceName)) {
            jcrBrowser.setSession(sessionProviderService.getSessionProvider(null)
                                                        .getSession(workspaceName,
                                                                    jcrBrowser.getRepository()));
          }

          String path = (String) httpRequest.getParameter("goParent");
          if (path != null) {
            jcrBrowser.setNode(jcrBrowser.getNode().getNode(path));
          } else {
            path = (String) httpRequest.getParameter("goNodePath");
            if (path != null)
              jcrBrowser.setNode((Node) jcrBrowser.getSession().getItem(path));
            // else seems nothing changed in JCR navigation
          }
        }
      } else {
        // start from root node

        ManageableRepository repository = repositoryService.getDefaultRepository();

        Session jcrSession = sessionProviderService.getSessionProvider(null)
                                                   .getSession(repository.getConfiguration()
                                                                         .getDefaultWorkspaceName(),
                                                               repository);

        if (jcrBrowser == null) {
          jcrBrowser = new JCRBrowser();
          jcrBrowser.setRepositoryService(repositoryService);
        }

        jcrBrowser.setRepository(repository);
        jcrBrowser.setSession(jcrSession); // and set node to a workspace root node
      }

    } catch (NoSuchWorkspaceException e) {
      LOG.error("JCR Browser error " + e, e);
      jcrBrowser.addError(e);
    } catch (LoginException e) {
      LOG.error("JCR Browser error " + e, e);
      jcrBrowser.addError(e);
    } catch (RepositoryException e) {
      LOG.error("JCR Browser error " + e, e);
      jcrBrowser.addError(e);
    } catch (RepositoryConfigurationException e) {
      LOG.error("JCR Browser error " + e, e);
      jcrBrowser.addError(e);
    } finally {
      httpRequest.getSession().setAttribute("browser", jcrBrowser);
    }

    chain.doFilter(servletRequest, servletResponse);
  }

  public void init(FilterConfig arg0) throws ServletException {

  }

  public void destroy() {

  }
}
