/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import org.exoplatform.services.rest.data.StringRepresentation;
import org.exoplatform.services.rest.wrapper.ResourceWrapper;
import org.exoplatform.services.rest.wrapper.http.HTTPMethod;

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
  public void method1(Request request, Response response) {
    System.out.println(">>> method1 called!!");
    response.setEntity(new StringRepresentation(TEST_METHOD_NAME1));
  }
  
  public final static String TEST_HTTP_METHOD2 = "GET";
  public final static String TEST_URI2 = "/any/{id2}/";
  public final static String TEST_METHOD_NAME2 = "method2";
  
  @HTTPMethod(name=TEST_HTTP_METHOD2, uri=TEST_URI2)
  public void method2(Request request, Response response) {
    response.setEntity(new StringRepresentation((String)request.getResourceIdentifier().getParameters().get("id")));
  }


}
