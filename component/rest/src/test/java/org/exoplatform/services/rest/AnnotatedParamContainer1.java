/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.data.StringRepresentation;
import org.exoplatform.services.rest.data.BaseRepresentation;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

public class AnnotatedParamContainer1 implements ResourceContainer {

  @HTTPMethod("GET")
  @URITemplate("/level1/level2/")
  @ProduceMimeType("text/html")
  public Response method1() {
    System.out.println(">>>>> method1 called!!!");
    System.out.println("<<<<< produce type: html");
    Response res = Response.getInstance(RESTStatus.OK);
    res.setAcceptedMediaType("text/html");
    res.setRepresentation(new BaseRepresentation<String>("method1"));
    return res;
  }
  
  @HTTPMethod("GET")
  @URITemplate("/level1/level2/")
  @ProduceMimeType("text/xml")
  public Response method2() {
    System.out.println(">>>>> method1 called!!!");
    System.out.println("<<<<< produce type: xml");
    Response res = Response.getInstance(RESTStatus.OK, new StringRepresentation("method2"));
    res.setAcceptedMediaType("text/xml");
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
    Response res = Response.getInstance(RESTStatus.OK ,new StringRepresentation("method3"));
    return res;
  }
}
