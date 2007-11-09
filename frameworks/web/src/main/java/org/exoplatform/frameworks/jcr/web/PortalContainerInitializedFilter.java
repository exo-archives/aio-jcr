/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
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
 * Created by The eXo Platform SARL .<br/> Servlet Filter for initialization
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
