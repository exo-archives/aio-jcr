package org.exoplatform.groovy.test

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam

import org.exoplatform.services.rest.resource.ResourceContainer


@Path("groovy-test")
public class Test1 implements ResourceContainer {
  
  
  public Test1() {
  }
  
  @GET
  @Path("/groovy1/{param}/")
  public String method(@PathParam("param") String name) {
    def String resp = "Hello from groovy to " + name
    return resp
  }
  
}