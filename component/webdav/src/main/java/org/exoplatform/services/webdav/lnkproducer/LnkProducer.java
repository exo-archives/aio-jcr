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

package org.exoplatform.services.webdav.lnkproducer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.ResourceDispatcher.Context;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

@URITemplate("/lnkproducer/")
public class LnkProducer implements ResourceContainer {
  
  private ResourceDispatcher resourceDispatcher;
  
  public LnkProducer(ResourceDispatcher resourceDispatcher) {
    this.resourceDispatcher = resourceDispatcher;
  }
  
  @HTTPMethod("GET")
  @URITemplate("/{linkFilePath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response produceLink(
      @URIParam("linkFilePath") String linkFilePath,
      @QueryParam("path") String path
      ) {
    
    String contextHref = resourceDispatcher.getRuntimeContext().getContextHref();
    
    contextHref += "/jcr";
    
    Context context = resourceDispatcher.getRuntimeContext();
    String hostName = context.getServerName();
    
    try {
      LinkGenerator linkGenerator = new LinkGenerator(hostName, contextHref, path);
      byte []content = linkGenerator.generateLinkContent();
      
      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);
      
      return Response.Builder.ok().
          header("Content-Length", "" + content.length).
          entity(byteArrayInputStream, "application/octet-stream").build();
          
    } catch (IOException ioexc) {
      return Response.Builder.serverError().build();
    }
    
  }
  
}
