/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav;

import java.io.IOException;
import java.util.ArrayList;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.WebDavHeaders;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class WebDavSessionProviderFilter implements Filter {
  
  private static Log log = ExoLogger.getLogger("jcr.WebDavSessionProviderFilter");
  
  private ThreadLocalSessionProviderService providerService;

  public void init(FilterConfig config) throws ServletException {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    
    providerService = (ThreadLocalSessionProviderService) container
      .getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
  }
  
  public void destroy() {
  }

  public ArrayList<String> getLockTokens(String lockTokenHeader, String ifHeader) {
    ArrayList<String> lockTokens = new ArrayList<String>();
    
    if (lockTokenHeader != null) {      
      lockTokenHeader = lockTokenHeader.substring(1, lockTokenHeader.length() - 1);
      lockTokens.add(lockTokenHeader);      
    }
    
    if (ifHeader != null) {
      String headerLockToken = ifHeader.substring(ifHeader.indexOf("("));
      headerLockToken = headerLockToken.substring(2, headerLockToken.length() - 2);
      lockTokens.add(headerLockToken);
    }    
    
    return lockTokens;
  }  
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
    throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;

    Credentials credentials = null;    
    
    String authenticateHeader = httpRequest.getHeader(WebDavHeaders.AUTHORIZATION);
    
    String decodedAuth = "";
    
    if (authenticateHeader != null) {
      try {
        String []basic = authenticateHeader.split(" ");
        if (basic.length >= 2 && basic[0].equalsIgnoreCase(HttpServletRequest.BASIC_AUTH)) {
          decodedAuth = new String(Base64.decodeBase64(basic[1].getBytes()));
        }        
        
        String []authParams = decodedAuth.split(":");
        credentials = new SimpleCredentials(authParams[0], authParams[1].toCharArray());        
      } catch (Exception exc) {
        log.info("Can't parse authenticate header!!!");
      }
    }
    
    if (credentials == null) {
      credentials = new SimpleCredentials("", "".toCharArray());
    }

    String lockTokenHeader = httpRequest.getHeader(WebDavHeaders.LOCKTOKEN);
    
    String ifHeader = httpRequest.getHeader(WebDavHeaders.IF);
    
    ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
    
    SessionProvider provider = new SessionProvider(credentials, lockTokens); //providerService.getSessionProvider(null);

    providerService.setSessionProvider(null, provider);

    chain.doFilter(request, response);    
  }  
  
}
