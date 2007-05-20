/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import org.exoplatform.services.rest.data.StringRepresentation;
import org.exoplatform.services.rest.wrapper.ResourceWrapper;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class DummyResourceWrapper1 implements ResourceWrapper {

  public final static String TEST_HTTP_METHOD1 = "GET";
  public final static String TEST_URI1 = "/any/{id1}/";
  public final static String TEST_METHOD_NAME1 = "method1";
  
  @HTTPMethod(name=TEST_HTTP_METHOD1, uri=TEST_URI1)
  public Response method1(Representation rep) {
    System.out.println(">>> method1 called!!");
    Response res = new Response(RESTStatus.OK);
    res.setEntity(new StringRepresentation(TEST_METHOD_NAME1));
    return res;
  }
  
  public final static String TEST_HTTP_METHOD2 = "POST";
  public final static String TEST_URI2 = "/any/{id2}/";
  public final static String TEST_METHOD_NAME2 = "method2";
  
  @HTTPMethod(name=TEST_HTTP_METHOD2, uri=TEST_URI2)
  public Response method2(Representation rep) {
    System.out.println(">>> method2 called!!");
    Response res = new Response(RESTStatus.OK);
    res.setEntity(new StringRepresentation(TEST_METHOD_NAME2));
    return res;
  }

  public final static String TEST_HTTP_METHOD3 = "PUT";
  public final static String TEST_URI3 = "/any/{id3}/";
  public final static String TEST_METHOD_NAME3 = "method2";
  
  @HTTPMethod(name=TEST_HTTP_METHOD3, uri=TEST_URI3)
  public Response method3(Representation rep) {
    System.out.println(">>> method3 called!!");
    Response res = new Response(RESTStatus.OK);
    res.setEntity(new StringRepresentation(TEST_METHOD_NAME3));
    return res;
  }

}
