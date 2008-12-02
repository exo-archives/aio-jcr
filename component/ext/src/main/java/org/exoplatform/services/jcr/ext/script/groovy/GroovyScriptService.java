/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.services.jcr.ext.script.groovy;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Path("script/groovy/{repository}/{workspace}")
public class GroovyScriptService implements ResourceContainer {

  /**
   * Session Provider service.
   */
  private final ThreadLocalSessionProviderService sessionProviderService;

  /**
   * See {@link GroovyScript2RestLoader}.
   */
  private final GroovyScript2RestLoader           groovyLoader;

  public GroovyScriptService(ThreadLocalSessionProviderService sessionProviderService,
                             GroovyScript2RestLoader groovyLoader) {

    this.sessionProviderService = sessionProviderService;
    this.groovyLoader = groovyLoader;
  }

  /**
   * This method is useful for clients that can send script in request body
   * without form-data. At required to set specific Content-type header
   * 'script/groovy'.
   * 
   * @return Response
   */
  @POST
  @Consumes("script/groovy")
  public Response addScript() {
    return Response.created(null).build();
  }

  /**
   * This method is useful for clients that send scripts as file in
   * 'multipart/*' request body.
   * 
   * @return Response
   */
  @POST
  @Consumes("multipart/*")
  public Response addScripts() {
    return Response.created(null).build();
  }
  
  @GET
  @Produces("script/groovy")
  @Path("{path:.*}")
  public InputStream getScript() {
    return null;
  }

  @DELETE
  @Path("{path:.*}")
  public void deleteScript() {
  }

  @GET
  @Path("deploy/{path:.*}")
  public void deploy() {
  }

  @GET
  @Path("undeploy/{path:.*}")
  public void undeploy() {
  }

}
