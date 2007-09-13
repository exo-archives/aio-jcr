/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lnkproducer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
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
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/lnkproducer/")
public class LnkProducer implements ResourceContainer {
  
  private static Log log = ExoLogger.getLogger("jcr.LnkProducer");
  
  private ResourceDispatcher resourceDispatcher;
  
  public LnkProducer(ResourceDispatcher resourceDispatcher) {
    log.info("Upping...");    
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
    
    String contextHref = resourceDispatcher.getRuntimeContext().getContextHref() + "/jcr";
    
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
