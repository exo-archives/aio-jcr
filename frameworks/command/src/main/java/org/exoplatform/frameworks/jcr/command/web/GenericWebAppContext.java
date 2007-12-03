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
package org.exoplatform.frameworks.jcr.command.web;

import java.util.Enumeration;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.chain.web.servlet.ServletWebContext;
import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.jcr.JCRAppSessionFactory;
import org.exoplatform.frameworks.jcr.command.JCRAppContext;
import org.exoplatform.services.log.ExoLogger;
/**
 * Created by The eXo Platform SAS        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: GenericWebAppContext.java 5800 2006-05-28 18:03:31Z geaz $
 */

public class GenericWebAppContext extends ServletWebContext implements JCRAppContext {
  
  //private static final long serialVersionUID = 12L;
  
  public static final String JCR_CONTENT_MAPPING = "org.exoplatform.frameworks.web.repositoryMapping";
  
  private static Log log = ExoLogger.getLogger("jcr.JCRWebAppContext"); 

  protected JCRAppSessionFactory sessionFactory;
  
  protected String currentWorkspace;
  
  public GenericWebAppContext(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
      JCRAppSessionFactory sessionFactory) {

    initialize(servletContext, request, response);
    
    this.sessionFactory = sessionFactory;
    
    //log.info("WEb context ---------------");
    // initialize context with all props
    Enumeration en = servletContext.getInitParameterNames();
    while(en.hasMoreElements()) {
      String name = (String)en.nextElement();
      put(name, servletContext.getInitParameter(name));
      log.debug("ServletContext init param: "+name+"="+servletContext.getInitParameter(name));
    }
    
    en = servletContext.getAttributeNames();
    while(en.hasMoreElements()) {
      String name = (String)en.nextElement();
      put(name, servletContext.getAttribute(name));
      log.debug("ServletContext: "+name+"="+servletContext.getAttribute(name));
    }
    
      
    HttpSession session = request.getSession(false);
    if(session != null) {
      en = session.getAttributeNames();
      while (en.hasMoreElements()) {
        String name = (String) en.nextElement();
        put(name, session.getAttribute(name));
        log.debug("Session: "+name+"="+session.getAttribute(name));
      }
    }
  
    en = request.getAttributeNames();
    while(en.hasMoreElements()) {
      String name = (String)en.nextElement();
      put(name, request.getAttribute(name));
    }
    
    en = request.getParameterNames();
    while(en.hasMoreElements()) {
      String name = (String)en.nextElement();
      put(name, request.getParameter(name));
      log.debug("Request: "+name+"="+request.getParameter(name));
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.frameworks.jcr.command.JCRAppContext#getSession()
   */
  public Session getSession() throws LoginException, NoSuchWorkspaceException, RepositoryException {
    return sessionFactory.getSession(currentWorkspace);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.frameworks.jcr.command.JCRAppContext#setCurrentWorkspace(java.lang.String)
   */
  public void setCurrentWorkspace(String workspaceName) {
    this.currentWorkspace = workspaceName;
  }

}
