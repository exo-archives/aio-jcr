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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.Command;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.frameworks.jcr.JCRAppSessionFactory;
import org.exoplatform.frameworks.jcr.SingleRepositorySessionFactory;
import org.exoplatform.frameworks.jcr.command.web.GenericWebAppContext;
import org.exoplatform.services.command.impl.CommandService;

/**
 * Created by The eXo Platform SAS .
 * Shortcut for 
 * http://localhost:8080/simple-cms/command?Command=displayResource&currentNode=/&path=test1.html/jcr:content&workspace=production
 * TODO make it as filter to controller?
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: DisplayJCRContentServlet.java 5800 2006-05-28 18:03:31Z geaz $
 */

public class DisplayJCRContentServlet extends HttpServlet {
  
  ServletConfig config;
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    
    JCRAppSessionFactory factory = (JCRAppSessionFactory)request.getSession()
        .getAttribute(SingleRepositorySessionFactory.SESSION_FACTORY);
    GenericWebAppContext ctx = new GenericWebAppContext(getServletContext(), request, response,
          factory);
    ExoContainer container = (ExoContainer) getServletContext().getAttribute(
        WebConstants.EXO_CONTAINER);
    if(container == null) {
      String portalName = getServletContext().getServletContextName();
      container = RootContainer.getInstance().getPortalContainer(
          portalName);
    }

    CommandService commandService = (CommandService)container.
    getComponentInstanceOfType(CommandService.class); 
    String catalogName = (String)ctx.get(WebConstants.CATALOG_NAME); 
    
    String wsName = (String)ctx.get("workspace");
    if(wsName != null)
      ctx.setCurrentWorkspace(wsName);

    String currentPath = (String)ctx.get("path");
    
    if(currentPath == null)
      throw new ServletException("Path undefined "+request.getParameter("path")+" Request: "+request.getRequestURI());

    try {
      //Command cmd = catalog.getCommand("displayResource");
      Command cmd;
      if(catalogName == null)
        cmd = commandService.getCatalog().getCommand("displayResource");
      else
        cmd = commandService.getCatalog(catalogName).getCommand("displayResource");
      
      if(cmd == null)
        throw new Exception("No 'displayResource' command found");
      ctx.put("path", currentPath);
      ctx.put("cache-control-max-age",getServletConfig().getInitParameter("cache-control-max-age"));
      cmd.execute(ctx);     
    } catch (Exception e) {
      e.printStackTrace();
      throw new ServletException(e);
    } 
  }
  
}
