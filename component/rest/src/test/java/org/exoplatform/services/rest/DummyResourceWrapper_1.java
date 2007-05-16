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
public class DummyResourceWrapper_1 implements ResourceWrapper {

  public final static String TEST_HTTP_METHOD1 = "GET";
  public final static String TEST_URI1 = "/level1/level2/level3/";
  public final static String TEST_METHOD_NAME1 = null;
  
  
  @HTTPMethod(name=TEST_HTTP_METHOD1, uri=TEST_URI1)
  public void method1(Request request, Response response) {
    System.out.println(">>> method1 called!!");
    response.setEntity(new StringRepresentation(TEST_METHOD_NAME1));
  }

}
