/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

import junit.framework.TestCase;

import java.lang.reflect.Method;

import org.exoplatform.services.rest.data.URIRestorer;


public class URIRestorerTest extends TestCase {

	TestedClazz t;
	protected void setUp() throws Exception {
		super.setUp();
		t = new TestedClazz();
	}
	
	public void testRecovery() throws Exception {
	  try {
	    t.method1("myID0", "dummy", "myID1", "dummy", "myID2", "myID3");
	    fail("method1 should throws IllegalArgumentException");
	  } catch (IllegalArgumentException e) {}
/*	  
  try {
      t.method2("myID0", "dummy", "myID1", "dummy", "myID2", "myID3");
      fail("method2 should throws IllegalArgumentException");
    } catch (IllegalArgumentException e) {}
*/
    assertEquals("level0/level1/myID1/level2/myID2/myID3/",
        t.method2("myID0", "dummy", "myID1", "dummy", "myID2", "myID3"));
    
    assertEquals("level0/myID0/level1/myID1/level2/myID2/myID3/",
        t.method3("myID0", "dummy", "myID1", "dummy", "myID2", "myID3"));
	}

	@URITemplate("/level0/{id0}/")
	public class TestedClazz {

	  @URITemplate("/level1/{id1}/level2/{id2}/{id3}/")
    public String method1(
        @URIParam("id0") String param0,
        @QueryParam("param1") String qparam,
        @URIParam("id1") String param1,
        @HeaderParam("param1") String hparam,
        @URIParam("id2") String param2,
        @URIParam("id3") String param3) throws Exception {

	    // qparam, hparam used to make test more close to real
	    /*
	     *  In this test only param0, param1, param2 used to call method
	     *  recoveryURI. this is not allowed, becouse in annotation to method
	     *  were declared param0, param1, param2.
	     *  In this case URI can't re recovery unambiguously
	     *  !!! This test should de fail
	     */
	    String[] uriParams = {param0, param1, param2};
      Method m = getClass().getMethod("method1", String.class, String.class, String.class,
          String.class, String.class, String.class);
      return URIRestorer.restoreURI("", uriParams, m, getClass().getAnnotation(URITemplate.class));
    }

	  @URITemplate("/level1/{id1}/level2/{id2}/{id3}/")
    public String method2(
        @URIParam("id") String param0,
        @QueryParam("param1") String qparam,
        @URIParam("id1") String param1,
        @HeaderParam("param1") String hparam,
        @URIParam("id2") String param2,
        @URIParam("id3") String param3) throws Exception {
      
      // qparam, hparam used to make test more close to real
	    /*
	     *  In this test for parameter param0 can't will be ignore.
	     *  Annotation for that param 'id' but in URITemplate declared
	     *  'id1'. This method should return  level0/level1/myID1/level2/myID2/myID3/
	     */
      String[] uriParams = {param0, param1, param2, param3};
      Method m = getClass().getMethod("method2", String.class, String.class, String.class,
          String.class, String.class, String.class);
      
      return URIRestorer.restoreURI("", uriParams, m, getClass().getAnnotation(URITemplate.class));
    }
	  
		@URITemplate("/level1/{id1}/level2/{id2}/{id3}/")
		public String method3(
		    @URIParam("id0") String param0,
		    @QueryParam("param1") String qparam,
		    @URIParam("id1") String param1,
		    @HeaderParam("param1") String hparam,
		    @URIParam("id2") String param2,
		    @URIParam("id3") String param3) throws Exception {
			
      // qparam, hparam used to make test more close to real
			String[] uriParams = {param0, param1, param2, param3};
			Method m = getClass().getMethod("method3", String.class, String.class, String.class,
			    String.class, String.class, String.class);
			
			return URIRestorer.restoreURI("", uriParams, m, getClass().getAnnotation(URITemplate.class));
		}

	}
}

