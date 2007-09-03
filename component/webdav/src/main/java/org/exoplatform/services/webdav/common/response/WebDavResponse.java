/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.webdav.common.response;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.WebDavProperty;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WebDavResponse.java 12899 2007-02-20 15:13:30Z gavrikvetal $
 */

public class WebDavResponse extends HttpServletResponseWrapper {

  private static Log log = ExoLogger.getLogger("jcr.WebDavResponse");
  
  private HttpServletResponse response;

  public WebDavResponse(HttpServletResponse response) {
    super(response);
    this.response = response;
  }

  public void setMultistatus(MultiStatus multistatus) {
    try {
      //DavUtil.sendMultistatus(response, multistatus);
    } catch (Exception exc) {
      log.info("Can't send MULTISTATUS. " + exc.getMessage(), exc);
    }
  }

  public void setProperty(WebDavProperty property) {
    try {
      //DavUtil.sendSingleProperty(response, property);
    } catch (Exception exc) {
      log.info("Can't send property. " + exc.getMessage());
      exc.printStackTrace();
    }    
  }  

  public void setResponseHeader(String headerName, String headerValue) {
    if (headerValue == null) {      
      return;
    }
    addHeader(headerName, headerValue);    
  }
  
  public void answerUnAuthorized(String wwwAuthencticate) {
    addHeader(DavConst.Headers.WWWAUTHENTICATE, wwwAuthencticate);
    setStatus(WebDavStatus.UNAUTHORIZED);
  }  
  
  public void answerOk() {
    addHeader(DavConst.Headers.CACHECONTROL, "no-cache");
    setStatus(WebDavStatus.OK);
  }
  
  public void answerCreated() {
    setStatus(WebDavStatus.CREATED);
  }  
  
  public void answerNoContent() {
    setStatus(WebDavStatus.NO_CONTENT);
  }  
  
  public void answerNotFound() {
    setStatus(WebDavStatus.NOT_FOUND);
  }  

  public void answerForbidden() {
    setStatus(WebDavStatus.FORBIDDEN);
  }  

  public void answerPreconditionFailed() {
    setStatus(WebDavStatus.PRECONDITION_FAILED);
  }  
  
  public void answerNotImplemented() {
    setStatus(WebDavStatus.NOT_IMPLEMENTED);
  }
  
  public void writeToResponse(byte []data, int len) throws IOException {
    getOutputStream().write(data, 0, len);
  }

}
