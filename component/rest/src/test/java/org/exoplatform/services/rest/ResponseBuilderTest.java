/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import junit.framework.TestCase;

import org.exoplatform.services.rest.transformer.*;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class ResponseBuilderTest extends TestCase {
  
  private final static String TEST_NAME = ">>>ResponseBuilderTest: ";
  StringEntityTransformerFactory transformerFactory = new StringEntityTransformerFactory();
  
  public void testError() {
    Response response = Response.Builder.serverError().build();
    assertEquals(RESTStatus.INTERNAL_ERROR, response.getStatus());

    response = Response.Builder.notFound().build();
    assertEquals(RESTStatus.NOT_FOUND, response.getStatus());

    response = Response.Builder.forbidden().build();
    assertEquals(RESTStatus.FORBIDDEN, response.getStatus());
  }
  
  public void testOk () throws Exception {
    
    Response response = Response.Builder.ok().build();
    assertEquals(RESTStatus.OK, response.getStatus());

    String entity = "oktest\n";
    response = Response.Builder.ok(entity).transformer(transformerFactory).build();
    assertEquals(RESTStatus.OK, response.getStatus());
    assertEquals("oktest\n", response.getEntity());
    System.out.print("\n" + TEST_NAME);
    response.writeEntity(System.out);
    
    response =
      Response.Builder.ok(entity, "text/plain").build();
    assertEquals("text/plain", response.getEntityMetadata().getMediaType());
  }
  

  public void testCreated () throws Exception {

    String location = "http://localhost/test";

    Response response = Response.Builder.created(location).build();
    assertEquals(location, response.getResponseHeaders().getFirst("Location"));
    
    response =
      Response.Builder.created(location, location).transformer(transformerFactory).build();
    assertEquals(location, response.getResponseHeaders().getFirst("Location"));
    assertEquals(location, response.getEntity());
    System.out.print("\n" + TEST_NAME);
    response.writeEntity(System.out);
  }

  
  public void testCustom() throws Exception {

    int st = 306; // this is for test builder with custom status
    Response response = Response.Builder.newInstance().status(st).build();
    assertEquals(st, response.getStatus());
    String entity = "customtest";
    response = Response.Builder.withStatus(st).entity(entity, "text/plain")
        .transformer(transformerFactory).build();
    
    assertEquals("customtest", response.getEntity());
    assertEquals("text/plain", response.getEntityMetadata().getMediaType());
    System.out.print("\n" + TEST_NAME);
    response.writeEntity(System.out);
  }
  
}
