/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import org.w3c.dom.Document; 
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringInputTransformer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class ResourceContainer2 implements ResourceContainer {

  @HTTPMethod("GET")
  @URITemplate("/level1/{id}/level3/")
  @ProducedMimeTypes("text/*")
  @InputTransformer(StringInputTransformer.class)
  @OutputTransformer(StringOutputTransformer.class)
  public Response method1(String str, @URIParam("id") String param, @HeaderParam("tESt") String test) {
    System.out.println(">>> method1 called: id = " + param);
    System.out.println(">>> request entity - type: " + str.getClass().toString()+ "; value: " + str);
    System.out.println(">>> header test: " + test);
    String e = ">>> this is response entity\n";
    Response resp = Response.Builder.ok(e, "text/plain").build();
    return resp;
  }

  @HTTPMethod("POST")
  @URITemplate("/level1/{id}/level3/")
  @ProducedMimeTypes("text/*")
  @InputTransformer(StringInputTransformer.class)
  @OutputTransformer(XMLOutputTransformer.class)
  public Response method2(String str,
      @URIParam("id") String param,
      @HeaderParam("tESt") String test) throws Exception {
    
    System.out.println("<<< method1 called: id = " + param);
    System.out.println("<<< request entity - type: " + str.getClass().toString()+ "; value: " + str);
    System.out.println("<<< header test: " + test);
    Document document =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      
    Element element = document.createElement("test");
    document.appendChild(element);
    Response resp = Response.Builder.ok(document, "text/xml").build();
    return resp;
  }
}
