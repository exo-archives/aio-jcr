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

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.resource.ResourceContainer;


/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id: $
 */

@Path("/lnkproducer/")
public class LnkProducer implements ResourceContainer {

  private static Log log = ExoLogger.getLogger(LnkProducer.class);

  public LnkProducer() {
  }

  @GET
  @Path("/{linkFilePath}/")
  @Produces("application/octet-stream")
  public Response produceLink(@PathParam("linkFilePath") String linkFilePath,
                              @QueryParam("path") String path,
                              @Context UriInfo uriInfo) {

    String host = uriInfo.getRequestUri().getHost();
    String uri = uriInfo.getBaseUri().toString();

    try {
      LinkGenerator linkGenerator = new LinkGenerator(host, uri, path);
      byte[] content = linkGenerator.generateLinkContent();

      return Response.ok(content, MediaType.APPLICATION_OCTET_STREAM)
                     .header(HttpHeaders.CONTENT_LENGTH, Integer.toString(content.length))
                     .build();

    } catch (IOException exc) {
      log.error(exc.getMessage(), exc);
      throw new WebApplicationException(exc);
    }

  }
}
