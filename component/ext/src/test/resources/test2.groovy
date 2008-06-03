package org.exoplatform.groovy.test

import org.exoplatform.services.rest.HTTPMethod
import org.exoplatform.services.rest.URITemplate
import org.exoplatform.services.rest.URIParam
import org.exoplatform.services.rest.Response
import org.exoplatform.services.rest.OutputTransformer
import org.exoplatform.services.rest.transformer.StringOutputTransformer
import org.exoplatform.services.rest.container.ResourceContainer

@URITemplate("/test/")
class Test2 implements ResourceContainer {
  
  @HTTPMethod("GET")
  @URITemplate("/groovy2/{param}/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response method(@URIParam("param") String name) {
    def Response resp = Response.Builder.ok("Hello from groovy to >>>>> " + name + "!", "text/plain").build()
    return resp
  }
  
}