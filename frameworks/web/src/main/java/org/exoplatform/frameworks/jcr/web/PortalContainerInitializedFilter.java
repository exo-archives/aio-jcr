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
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS .<br/> Servlet Filter for initialization
 * PortalContainer instance in following way: - try to get current
 * PortalContainer instance using
 * ExoContainerContext.getContainerByName(contextName) - if not found try to get
 * RootContainer instance using ExoContainerContext.getTopContainer() and then
 * create PortalContainer after it - if neither Portal nor Root Container found
 * (possible if there is instantiated StandaloneContainer) throws
 * ServletException
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class PortalContainerInitializedFilter implements Filter {

  private static Log log = ExoLogger.getLogger("PortletContainerInitializedFilter");

  private String contextName;

  public void init(FilterConfig config) throws ServletException {
    contextName = config.getServletContext().getServletContextName();
  }

  /**
   * initializes PortalContainer instance.
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    PortalContainer pcontainer =
      (PortalContainer)ExoContainerContext.getContainerByName(contextName);
    log.info("get-by-name");
    if (pcontainer == null) {
      log.info("get-from-root");
      ExoContainer container = ExoContainerContext.getTopContainer();
      if (container instanceof RootContainer) {
        pcontainer = ((RootContainer) container).getPortalContainer(contextName);
        log.info("PortalContainer is created after RootContainer");
      }
    }
    if(pcontainer == null)  {
      throw new ServletException("Could not initialize PortalContainer."
      + "Current ExoContainer is: "
      + ExoContainerContext.getCurrentContainer());
    }
    ExoContainerContext.setCurrentContainer(pcontainer);
    log.info("Curent Container: " + ExoContainerContext.getCurrentContainer());
    PortalContainer.setInstance(pcontainer);
    
    chain.doFilter(request, response);
  }

  /**
   * destroys portal container instance.
   * 
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
    PortalContainer.setInstance(null);
  }

}
