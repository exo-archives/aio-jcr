package org.exoplatform.services.rest.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.Connector;
import org.exoplatform.services.rest.Request;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.EntityMetadata;

public class RestServlet extends HttpServlet implements Connector {
	
  private ExoContainer container;
  private ResourceDispatcher resDispatcher;
  private static Log logger = ExoLogger.getLogger("RestServlet");
	
  public void init(){
    try {
      container = ExoContainerContext.getCurrentContainer();
    }catch(Exception e) {
      logger.error("Cann't get current container");
      e.printStackTrace();
    }
    resDispatcher = (ResourceDispatcher)container.getComponentInstanceOfType(
        ResourceDispatcher.class);
    if(resDispatcher == null) {
      logger.error("RESOURCE_ROUTER is null");
    }
    logger.info("CONTAINER:       " + container);
    logger.info("RESOURCE_ROUTER: " + resDispatcher);
  }
	
  public void service(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    throws IOException, ServletException {
    
    Request request = RequestFactory.createRequest(httpRequest);
    try {
      Response<?> response = resDispatcher.dispatch(request);
      httpResponse.setStatus(response.getStatus());
      EntityMetadata metaData = response.getMetadata();
      if(metaData != null) {
        httpResponse.setContentType(metaData.getMediaType());
        if(metaData.getLocation() != null)
          httpResponse.setHeader("Location", metaData.getLocation());
      }
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
}
