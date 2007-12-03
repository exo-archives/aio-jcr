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

package org.exoplatform.services.webdav.deltav.command;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.transformer.SerializableTransformer;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.resource.JCRResourceDispatcher;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class LabelCommand extends WebDavCommand {
  
  //private static Log log = ExoLogger.getLogger("jcr.LabelCommand");
  
  public LabelCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher, 
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
    //log.info("construct..");
  }
  
  @HTTPMethod(WebDavMethod.LABEL)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  @OutputTransformer(SerializableTransformer.class)
  public Response label(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      Document document,      
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader,
      @QueryParam("VERSIONID") String versionId
      ) {

    try {

      SessionProvider sessionProvider = getSessionProvider(authorization);
      
      Item item = new JCRResourceDispatcher(sessionProvider, webDavService.getRepository(repoName)).getItem(repoPath);
      
      if (!(item instanceof Node)) {
        throw new AccessDeniedException();
      }
      
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
      
      tuneSession(item.getSession(), lockTokens);
      
      Node node = (Node)item;
      
//      if (versionId != null) {
//        Version version = node.getVersionHistory().getVersion(versionId);
//        //node = version.getNode(DavConst.NodeTypes.JCR_FROZENNODE);
//        
//        //version.getVersionHistory().addVersionLabel()
//                
//      }
      
      
      return null;
      
    } catch (Exception exc) {
      
      //log.info("Unhandled exception. " + exc.getMessage(), exc);
      
      return responseByException(exc);
    }
  }

}

