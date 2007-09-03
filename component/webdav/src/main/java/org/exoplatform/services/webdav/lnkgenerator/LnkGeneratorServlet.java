/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lnkgenerator;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class LnkGeneratorServlet extends HttpServlet {
  
  private static Log log = ExoLogger.getLogger("jcr.LnkGeneratorServlet");

  public static final String PARAM_PATH = "path";
  
  @Override
  public void init() throws ServletException {
    log.info("Upping...");
  }  
  
  protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      String paramPath = request.getParameter(PARAM_PATH);
      
      String []paramPaths = paramPath.split("/");
      
      String host = request.getServerName();      
      String servletPath = request.getScheme() + "://" + request.getServerName() + ":" +
      request.getServerPort() + request.getContextPath() + "/" + paramPaths[1]; 
      
      String path = paramPath.substring(("/" + paramPaths[1]).length());
      
      LinkGenerator lnkGenerator = new LinkGenerator(host, servletPath, path);
      byte []linkContent = lnkGenerator.generateLinkContent();

      response.setHeader(DavConst.Headers.CONTENTLENGTH, "" + linkContent.length);
      response.setHeader(DavConst.Headers.CONTENTTYPE, "application/occet-stream");
      
      response.setStatus(WebDavStatus.OK);
      
      response.getOutputStream().write(linkContent);
      return;
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
    response.setStatus(WebDavStatus.INTERNAL_SERVER_ERROR);

  }

}

