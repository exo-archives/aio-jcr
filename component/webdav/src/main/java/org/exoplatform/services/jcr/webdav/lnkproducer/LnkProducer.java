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

package org.exoplatform.services.jcr.webdav.lnkproducer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.services.rest.resource.ResourceContainer;

//import org.exoplatform.services.rest.ContextParam;
//import org.exoplatform.services.rest.HTTPMethod;
//import org.exoplatform.services.rest.InputTransformer;
//import org.exoplatform.services.rest.OutputTransformer;
//import org.exoplatform.services.rest.QueryParam;
//import org.exoplatform.services.rest.ResourceDispatcher;
//import org.exoplatform.services.rest.Response;


//
//import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
//import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id: $
 */

@Path("/lnkproducer/")
public class LnkProducer implements ResourceContainer {

  public LnkProducer() {
  }

  @GET
  @Path("/{linkFilePath}/")
  @Consumes("*/*") //(PassthroughInputTransformer.class)
  @Produces("*/*") //(PassthroughOutputTransformer.class)
  public Response produceLink(@PathParam("linkFilePath") String linkFilePath,
                              @PathParam("path") String path,
                              @Context UriInfo baseURI,
                              String host) {

    //baseURI += "/jcr";
    String strUri = baseURI.getBaseUri().toString();

    try {
      LinkGenerator linkGenerator = new LinkGenerator(host, strUri, path);
      byte[] content = linkGenerator.generateLinkContent();

      ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content);

      return Response.ok()
                             .header("Content-Length", "" + content.length)
                             .entity(byteArrayInputStream 
                                     /**, "application/octet-stream"**/)
                             .build();

    } catch (IOException ioexc) {
      ioexc.printStackTrace();
      return Response.serverError().build();
    }

  }

}
