/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.jcr.rest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.RestCommandContext;
import org.exoplatform.services.rest.RestService;
import org.exoplatform.services.rest.RestServiceImpl;
import org.exoplatform.services.rest.common.command.RestCommand;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class ExoJcrRestServlet extends HttpServlet {
  
  private static Log log = ExoLogger.getLogger("jcr.ExoJcrRestServlet");
  
  protected ExoContainer container;
  protected RestService restService;
  
  @Override
  public void init() throws ServletException {
    log.info(">>>>>>>>>>>>>>>>>>>>> Init here!!!");
    try {
      container = ExoContainerContext.getCurrentContainer();
      
      log.info("CONTAINER: " + container);
      
      restService = (RestServiceImpl)container.getComponentInstanceOfType(RestServiceImpl.class);
      
      log.info("SERVICE: " + restService);
      
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage());
      exc.printStackTrace();
    }
  }
  
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String commandName = request.getMethod();
    log.info("RESTCOMMAND: [" + commandName + "]");
    if(restService == null) {
      String portalName = request.getContextPath().substring(1);
      PortalContainer pcontainer = RootContainer.getInstance().getPortalContainer(portalName) ;
      restService = (RestServiceImpl)pcontainer.getComponentInstanceOfType(RestServiceImpl.class);
    }

    
    RestCommand command = restService.getCommand(commandName);
    if (command == null) {
      response.setStatus(405);
      log.info("Command " + commandName + " not supplied.");
      return;
    }

    long timeStart = System.currentTimeMillis();
    try {
      
      RestCommandContext context = new RestCommandContext(
          getServletContext(), request, response, restService);
      command.execute(context);
    } catch (Exception exc) {
      log.info("EXECUTE COMMAND EXCEPTION. " + exc.getMessage());
      exc.printStackTrace();
    }
    try {
      long timeEnd = System.currentTimeMillis();
      long allTime = timeEnd - timeStart;
      long msec = allTime % 1000;
      long sec = allTime / 1000;
      long min = sec / 60;
      log.info(String.format("Command %s completed at MM:SS:MS %d:%d:%d", commandName, min, sec, msec));
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage());
      exc.printStackTrace();
    }
  }
  

}

