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
import javax.jcr.Session;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.WebDavHeaders;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class CopyCommand extends WebDavCommand {
  
  public CopyCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
  @HTTPMethod(WebDavMethod.COPY)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response copy(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.DESTINATION) String destinationHeader,
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader
      ) {
    
    try {
      String prefix = getPrefix(repoPath);
      
      if (!destinationHeader.startsWith(prefix)) {
        throw new AccessDeniedException();
      }
            
      String destination = destinationHeader.substring((prefix + "/").length());
      
      if (repoPath.split("/").length < 2) {
        throw new AccessDeniedException();
      }
      
      if (destination.split("/").length < 2) {
        throw new AccessDeniedException();
      }
      
      String srcWorkspace = repoPath.split("/")[0];
      String destWorkspace = destination.split("/")[0];
      
      String srcPath = repoPath.substring(srcWorkspace.length());
      String destPath = destination.substring(destWorkspace.length());
      
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);

      SessionProvider sessionProvider = getSessionProvider(authorization);
      
      ManageableRepository repository = webDavService.getRepository(repoName);      
            
      Session destSession = sessionProvider.getSession(destWorkspace, repository);
      
      tuneSession(destSession, lockTokens);
      
      destSession.getWorkspace().copy(srcWorkspace, srcPath, destPath);
      
      sessionProvider.close();
      
      return Response.Builder.withStatus(WebDavStatus.CREATED).build();
    } catch (Exception exc) {
      return responseByException(exc);
    }
    
  }
  
}
