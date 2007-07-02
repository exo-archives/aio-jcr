package org.exoplatform.services.rest.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.Connector;
import org.exoplatform.services.rest.MultivaluedMetadata;
import org.exoplatform.services.rest.Request;
import org.exoplatform.services.rest.ResourceBinder;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;

public class RestServlet extends HttpServlet implements Connector {
  
  private ExoContainer container;
  private ResourceDispatcher resDispatcher;
  private ResourceBinder resBinder;
  private static Log logger = ExoLogger.getLogger("RestServlet");
  
  public void init(){
    try {
      container = ExoContainerContext.getCurrentContainer();
    }catch(Exception e) {
      logger.error("Cann't get current container");
      e.printStackTrace();
    }
    resBinder = (ResourceBinder)container.getComponentInstanceOfType(
        ResourceBinder.class);
    resDispatcher = (ResourceDispatcher)container.getComponentInstanceOfType(
        ResourceDispatcher.class);
    if(resBinder == null) {
      logger.error("RESOURCE_BINDER is null");
    }
    if(resDispatcher == null) {
      logger.error("RESOURCE_DISPATCHER is null");
    }
    logger.info("CONTAINER:           " + container);
    logger.info("RESOURCE_BINDER:     " + resBinder);
    logger.info("RESOURCE_DISPATCHER: " + resDispatcher);
  }
  
  public void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    throws IOException, ServletException {
    
    Request request = RequestFactory.createRequest(httpRequest);
    try {
      Response response = resDispatcher.dispatch(request);
      httpResponse.setStatus(response.getStatus());
      tuneResponse(httpResponse, response.getResponseHeaders());
      OutputStream out = httpResponse.getOutputStream();
      response.writeEntity(out);
      out.flush();
      out.close();
    }catch(Exception e) {
      logger.error("!!!!! serve method error");
      e.printStackTrace();
      httpResponse.sendError(500, "This request cann't be serve by service.\n" +
          "Check request parameters and try again.");
    }
  }
  
  
  private void tuneResponse(HttpServletResponse httpResponse,
      MultivaluedMetadata responseHeaders) {
    
    if(responseHeaders != null) {

      HashMap<String, String> headers =  responseHeaders.getAll();
      Set<String> keys = headers.keySet();
      Iterator<String> ikeys = keys.iterator();
      while (ikeys.hasNext()) {
        String key = ikeys.next();
        httpResponse.setHeader(key, headers.get(key));
      }
    }
  }
}
