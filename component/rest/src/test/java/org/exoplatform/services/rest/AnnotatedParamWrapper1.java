/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import org.exoplatform.services.rest.data.StringRepresentation;
import org.exoplatform.services.rest.wrapper.ResourceWrapper;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public class AnnotatedParamWrapper1 implements ResourceWrapper {

  @HTTPMethod("GET")
  @URITemplate("/level1/level2/")
  @ProduceMimeType("text/html")
  public Response method1(Representation rep) {
    System.out.println(">>>>> method1 called!!!");
    System.out.println("<<<<< produce type: html");
    Response res = new Response(RESTStatus.OK);
    res.setAcceptedMediaType("text/html");
    res.setEntity(new StringRepresentation("method1"));
    return res;
  }
  
  @HTTPMethod("GET")
  @URITemplate("/level1/level2/")
  @ProduceMimeType("text/xml")
  public Response method2(Representation rep) {
    System.out.println(">>>>> method1 called!!!");
    System.out.println("<<<<< produce type: xml");
    Response res = new Response(RESTStatus.OK);
    res.setAcceptedMediaType("text/xml");
    res.setEntity(new StringRepresentation("method2"));
    return res;
  }

  @HTTPMethod("POST")
  @URITemplate("/level1/{id1}/level3/{id2}/level4/{id3}/")
  @ConsumeMimeType
  public Response method3(@URIParam("id1") String param1,
      @URIParam("id2") String param2, @URIParam("id3") String param3) {
    System.out.println(">>>>> method2 called!!!");
    System.out.println("<<<<< id1 = " + param1);
    System.out.println("<<<<< id1 = " + param2);
    System.out.println("<<<<< id1 = " + param3);
    Response res = new Response(RESTStatus.OK);
    res.setEntity(new StringRepresentation("method3"));
    return res;
  }
}
