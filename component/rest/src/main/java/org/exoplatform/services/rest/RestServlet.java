package org.exoplatform.services.rest;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import org.apache.commons.logging.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.InputStream;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.rest.data.StringRepresentation;
import org.exoplatform.services.rest.ResourceRouter;
import org.exoplatform.services.rest.ResourceIdentifier;

public class RestServlet extends HttpServlet {
	
  private ExoContainer container;
  private ResourceRouter resRouter;
  private static Log logger = ExoLogger.getLogger("RestServlet");
	
  public void init(){
    logger.info(">>>>> RestServlet init() <<<<<<<");
    try {
      container = ExoContainerContext.getCurrentContainer();
    }catch(Exception e) {
      logger.error("!!! Cann't get current container !!!");
      e.printStackTrace();
    }
    resRouter = (ResourceRouter)container.getComponentInstanceOfType(ResourceRouter.class);
    if(resRouter == null) {
      logger.info("!!! ResourceRouter is null !!!");
    }
    logger.info("CONTAINER:       " + container);
    logger.info("RESOURCE_ROUTER: " + resRouter);
  }
	
  public void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    throws IOException, ServletException {
    String httpMethod = httpRequest.getMethod();
    String contPath = httpRequest.getContextPath();
    String pathInfo = httpRequest.getPathInfo();
    String queryString = httpRequest.getQueryString();
    logger.info("HTTP_METHOD:  " + httpMethod);
    logger.info("CONT_PATH:    " + contPath);
    logger.info("PATH_INFO:    " + pathInfo);
    logger.info("QUERY_STRING: " + queryString);
    Request request = new Request(new ResourceIdentifier(pathInfo),
        new StringRepresentation("text/plain"), new ControlData(httpMethod, null));
    Response response = new Response(request);
    try {
      resRouter.serve(request, response);
    }catch(Exception e) {
      logger.error("!!! serve method error !!!");
      e.printStackTrace();
    }
    InputStream in = response.getEntity().getStream();
    httpResponse.setContentType("text/xml");
    java.io.OutputStream out = httpResponse.getOutputStream();
    byte[] buff = new byte[1024];
    while(true) {
      int rd = in.read(buff);
      if(rd < 0) break;
      out.write(buff, 0, rd);
    }
    out.flush();
    out.close();
  }
}
