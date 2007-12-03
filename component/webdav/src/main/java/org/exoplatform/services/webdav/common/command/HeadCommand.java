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

import java.util.Calendar;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;

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
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.resource.JCRResourceDispatcher;
import org.exoplatform.services.webdav.common.resource.resourcedata.JcrFileResourceData;
import org.exoplatform.services.webdav.common.resource.resourcedata.JcrPropertyData;
import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;
import org.exoplatform.services.webdav.common.resource.resourcedata.XmlItemData;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class HeadCommand extends WebDavCommand {
  
  public HeadCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
  @HTTPMethod(WebDavMethod.HEAD)
  @URITemplate("/{repoName}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response head() {
    return Response.Builder.ok().
      header(DavConst.Headers.LASTMODIFIED, "" + Calendar.getInstance()).
      header(DavConst.Headers.CONTENTTYPE, "text/html").
      build();
  }

    
  @HTTPMethod(WebDavMethod.HEAD)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response head(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization
      ) {

    return doHead(repoName, repoPath, authorization);
  }
  
  private Response doHead(String repoName, String repoPath, String authorization) {
    try {
      String href = getHref(repoPath);
      
      SessionProvider sessionProvider = getSessionProvider(authorization);
      
      Item item = new JCRResourceDispatcher(sessionProvider, webDavService.getRepository(repoName)).getItem(repoPath);
      
      ResourceData resourceData = null;
      
      boolean isCollection = false;
      
      if (item instanceof Node) {
        Node node = (Node)item;
        
        if (node.isNodeType(DavConst.NodeTypes.NT_FILE)) {
          resourceData = new JcrFileResourceData(node);
        } else {
          resourceData = new XmlItemData(href + node.getPath(), node);
          isCollection = true;
        }
      } else {
        resourceData = new JcrPropertyData((Property)item);
      }
      
      if (isCollection) {
        return Response.Builder.ok().
            header(DavConst.Headers.LASTMODIFIED, resourceData.getLastModified()).
            header(DavConst.Headers.CONTENTTYPE, resourceData.getContentType()).
            build();
      }

      return Response.Builder.ok().
          header(DavConst.Headers.LASTMODIFIED, resourceData.getLastModified()).
          header(DavConst.Headers.CONTENTTYPE, resourceData.getContentType()).
          header(DavConst.Headers.CONTENTLENGTH, "" + resourceData.getContentLength()).
          build();      
      
    } catch (Exception exc) {
      return responseByException(exc);
    }    
    
  }
  
}
