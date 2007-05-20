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
public class DummyResourceWrapper implements ResourceWrapper {

  public final static String TEST_HTTP_METHOD1 = "GET";
  public final static String TEST_URI1 = "/level1/{id}/level3/";
  public final static String TEST_METHOD_NAME1 = "method1";
  
  
  @HTTPMethod(name=TEST_HTTP_METHOD1, uri=TEST_URI1)
  public Response method1(Representation rep) {
    System.out.println(">>> method1 called!!");
    Response res = new Response(RESTStatus.OK);
    res.setEntity(new StringRepresentation(TEST_METHOD_NAME1));
    return res;
  }

}
