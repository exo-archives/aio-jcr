package org.exoplatform.services.rest.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;

import org.exoplatform.services.rest.Request;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.rest.ResourceDispatcher;

public class RestServlet extends HttpServlet {
	
  private ExoContainer container;
  private ResourceDispatcher resDispatcher;
  private static Log logger = ExoLogger.getLogger("RestServlet");
	
  public void init(){
    logger.info(">>>>>>>>>RestServlet init");
    try {
      container = ExoContainerContext.getCurrentContainer();
    }catch(Exception e) {
      logger.error("!!!!! Cann't get current container");
      e.printStackTrace();
    }
    resDispatcher = (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
    if(resDispatcher == null) {
      logger.info("!!!!! ResourceRouter is null");
    }
    logger.info("CONTAINER:       " + container);
    logger.info("RESOURCE_ROUTER: " + resDispatcher);
  }
	
  public void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    throws IOException, ServletException {
    
    Request request = RequestHandler.createRequest(httpRequest);
    try {
      Response response = resDispatcher.dispatch(request);
      InputStream in = (InputStream)response.getRepresentation().getData();
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
