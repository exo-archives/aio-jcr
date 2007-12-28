/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.lock.LockException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.BadRequestException;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public abstract class WebDavCommand implements ResourceContainer {
  
  private static Log log = ExoLogger.getLogger("jcr.WebDavCommand");

  protected WebDavService webDavService;
  
  protected ResourceDispatcher resourceDispatcher;
  
  private ThreadLocalSessionProviderService sessionProviderService;
  
  public WebDavCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher, 
      ThreadLocalSessionProviderService sessionProviderService) {
    this.webDavService = webDavService;
    this.resourceDispatcher = resourceDispatcher;
    this.sessionProviderService = sessionProviderService;
  }
  
//  public SessionProvider getSessionProvider() {
//    return sessionProviderService.getSessionProvider(null);
//  }
  
  public SessionProvider getSessionProvider(String authorization) throws LoginException {    
    Credentials credentials = null;    
    String decodedAuth = "";
      
    if (authorization != null) {
      try {
        String []basic = authorization.split(" ");
        if (basic.length >= 2 && basic[0].equalsIgnoreCase("BASIC")) {
          decodedAuth = new String(Base64.decodeBase64(basic[1].getBytes()));
        }        
      } catch (Exception exc) {
        throw new LoginException();
      }
    } else {
      if (webDavService.getConfig().getDefIdentity() != null) {
        decodedAuth = webDavService.getConfig().getDefIdentity();
      }
    }
      
    String []authParams = decodedAuth.split(":");
    
    String userId = (authParams.length > 0) ? authParams[0] : "";
    String userPass = (authParams.length > 1) ? authParams[1] : "";
    
    credentials = new SimpleCredentials(userId, userPass.toCharArray());
    SessionProvider sessionProvider = new SessionProvider(credentials);
    return sessionProvider;
  }

  public String getPrefix(String repoPath) {
    String prefix = resourceDispatcher.getRuntimeContext().getAbsLocation();
    if (prefix.endsWith("/")) {
      prefix = prefix.substring(0, prefix.length() - 1);
    }
    
    prefix = prefix.substring(0, prefix.length() - repoPath.length());
    prefix = prefix.substring(0, prefix.length() - 1);
    return prefix;
  }  
  
  public String getHref(String repoPath) {
    String href = resourceDispatcher.getRuntimeContext().getAbsLocation();
    if (href.endsWith("/")) {
      href = href.substring(0, href.length() - 1);
    }
    
    href = href.substring(0, href.length() - repoPath.length()) + repoPath.split("/")[0];
    
    if (href.endsWith("/")) {
      href = href.substring(0, href.length() - 1);
    }
    
    return href;
  }
  
  protected void tuneSession(Session session, ArrayList<String> lockTokens) {
    String []presentLockTokens = session.getLockTokens();
    ArrayList<String> presentLockTokensList = new ArrayList<String>();
    for (int i = 0; i < presentLockTokens.length; i++) {
      String lockToken = presentLockTokens[i];
      presentLockTokensList.add(lockToken);
    }
    
    for (int i = 0; i < lockTokens.size(); i++) {
      String lockToken = lockTokens.get(i);
      if (!presentLockTokensList.contains(lockToken)) {
        session.addLockToken(lockToken);
      }
    }
  }
  
  protected ArrayList<String> getLockTokens(String lockTokenHeader, String ifHeader) {
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
  
  public Response responseByException(Exception exception) {
    try {
      throw exception;      
    } catch (BadRequestException exc) {
      return Response.Builder.badRequest().build();      
    } catch (PathNotFoundException exc) {
      return Response.Builder.withStatus(WebDavStatus.NOT_FOUND).build();      
    } catch (LoginException exc) {
      String wwwAuthencticate = webDavService.getConfig().getAuthHeader();
      return Response.Builder.withStatus(WebDavStatus.UNAUTHORIZED).
          header(DavConst.Headers.WWWAUTHENTICATE, wwwAuthencticate).build();      
    } catch (NoSuchWorkspaceException exc) {
      return Response.Builder.withStatus(WebDavStatus.FORBIDDEN).build();      
    } catch (AccessDeniedException exc) {
      return Response.Builder.withStatus(WebDavStatus.FORBIDDEN).build();      
    } catch (LockException exc) {
      return Response.Builder.withStatus(WebDavStatus.FORBIDDEN).build();      
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }    
    return Response.Builder.serverError().build();
  }
  
}
