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

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.chain.Command;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.frameworks.jcr.command.web.GenericWebAppContext;
import org.exoplatform.services.command.impl.CommandService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: CommandControllerServlet.java 14756 2008-05-26 14:47:15Z pnedonosko $
 */

public class CommandControllerServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    ExoContainer container = (ExoContainer) getServletContext().getAttribute(WebConstants.EXO_CONTAINER);
    if (container == null) {
      String portalName = getServletContext().getServletContextName();
      container = RootContainer.getInstance().getPortalContainer(portalName);
    }

    SessionProviderService sessionProviderService =
        (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);

    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);

    GenericWebAppContext ctx;
    try {
      ctx = new GenericWebAppContext(getServletContext(),
                               request,
                               response,
                               sessionProviderService.getSessionProvider(null), // null for ThreadLocalSessionProvider
                               repositoryService.getDefaultRepository());
    } catch (final RepositoryException e) {
      throw new IOException() {
        @Override
        public Throwable getCause() {
          return e;
        }
      };
    } catch (final RepositoryConfigurationException e) {
      throw new IOException() {
        @Override
        public Throwable getCause() {
          return e;
        }
      };
    }

    CommandService commandService = (CommandService) container.getComponentInstanceOfType(CommandService.class);
    String catalogName = (String) ctx.get(WebConstants.CATALOG_NAME);

    // command from context
    String commandName = (String) ctx.get("Command");
    if (commandName == null)
      throw new ServletException("No Command found at the Context");
    Command cmd;
    if (catalogName == null)
      cmd = commandService.getCatalog().getCommand(commandName);
    else
      cmd = commandService.getCatalog(catalogName).getCommand(commandName);

    if (cmd == null)
      throw new ServletException("No Command found " + commandName);
    try {
      cmd.execute(ctx);
    } catch (Exception e) {
      e.printStackTrace();
      throw new ServletException(e);
    }
  }

}
