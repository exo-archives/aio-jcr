package org.exoplatform.groovy.test

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam

import org.exoplatform.services.rest.resource.ResourceContainer


@Path("groovy-test")
public class Test2 implements ResourceContainer {
  
  
  public Test2() {
  }
  
  @GET
  @Path("/groovy2/{param}/")
  public String method(@PathParam("param") String name) {
    def String resp = "Hello from groovy to >>>>> " + name
    return resp
  }
  
}
