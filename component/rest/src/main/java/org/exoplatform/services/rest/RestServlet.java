package org.exoplatform.services.rest;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.util.*;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.ExoContainer;
//import org.exoplatform.services.rest.data.StringRepresentation;
import org.exoplatform.services.rest.ResourceDispatcher;
//import org.exoplatform.services.rest.ResourceIdentifier;
import org.exoplatform.services.rest.servlet.*;

public class RestServlet extends HttpServlet {
	
  private ExoContainer container;
  private ResourceDispatcher resRouter;
  private static Log logger = ExoLogger.getLogger("RestServlet");
	
  public void init(){
    logger.info(">>>>>>>>>RestServlet init");
    try {
      container = ExoContainerContext.getCurrentContainer();
    }catch(Exception e) {
      logger.error("!!!!! Cann't get current container");
      e.printStackTrace();
    }
    resRouter = (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
    if(resRouter == null) {
      logger.info("!!!!! ResourceRouter is null");
    }
    logger.info("CONTAINER:       " + container);
    logger.info("RESOURCE_ROUTER: " + resRouter);
  }
	
  public void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    throws IOException, ServletException {
    
    Request request = ServletUtils.createRequest(httpRequest);
    try {
      Response response = resRouter.dispatch(request);
      InputStream in = response.getEntity().getStream();
      httpResponse.setContentType(response.getAcceptedMediaType());
      httpResponse.setStatus(response.getStatus());
      java.io.OutputStream out = httpResponse.getOutputStream();
      byte[] buff = new byte[1024];
      while(true) {
        int rd = in.read(buff);
        if(rd < 0) break;
        out.write(buff, 0, rd);
      }
      out.flush();
      out.close();
    }catch(Exception e) {
      logger.error("!!!!! serve method error");
      e.printStackTrace();
      httpResponse.sendError(500, "This request cann't be serve by service.\n" + "Check request parameters and try again.");
    }
  }
}
