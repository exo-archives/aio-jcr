package org.exoplatform.frameworks.jcr.webdav;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.WebDavServiceImpl;
import org.exoplatform.services.webdav.common.command.WebDavCommand;

public class ExoJCRWebdavServlet extends HttpServlet {

    private static Log log = ExoLogger.getLogger("jcr.ExoJCRWebdavServlet");

    protected ExoContainer container = null;
    protected WebDavServiceImpl davService = null;

    @Override
    public void init() throws ServletException {
      try {
        container = ExoContainerContext.getCurrentContainer();
        davService = (WebDavServiceImpl)container.getComponentInstanceOfType(WebDavServiceImpl.class);
      } catch (Exception exc) {
        log.info("Unhandled exception. " + exc.getMessage());
        exc.printStackTrace();
      }
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      String commandName = request.getMethod();
      log.info("DAVCOMMAND - " + commandName);
      if(davService == null) {
        String portalName = request.getContextPath().substring(1);
        PortalContainer pcontainer =  RootContainer.getInstance().getPortalContainer(portalName) ;
        davService = (WebDavServiceImpl)pcontainer.getComponentInstanceOfType(WebDavServiceImpl.class);
      }

      WebDavCommand command = davService.getCommand(commandName);
      if (command == null) {
        response.setStatus(405);
        log.info("Command " + commandName + " not supplied.");
        return;
      }

      long timeStart = System.currentTimeMillis();
      try {
        WebDavCommandContext context = new WebDavCommandContext(
            getServletContext(), request, response, davService);

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
