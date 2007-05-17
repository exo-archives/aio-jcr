/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.rest.data.StringRepresentation;
import org.exoplatform.services.rest.wrapper.ResourceDescriptor;
import org.exoplatform.services.rest.wrapper.InvalidResourceDescriptorException;



/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id:$
 */
public class ResourceRouterTest extends TestCase {
  

  private StandaloneContainer container;
  
  
  public void setUp() throws Exception {
    
    StandaloneContainer.setConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");
  	
    container = StandaloneContainer.getInstance();
  }
  
  public void testIdentifier() throws Exception {
    URI uri = new URI("http://localhost/level1/level2/id");
    System.out.println("getScheme "+uri.getScheme());
    System.out.println("getSchemeSpecificPart "+uri.getSchemeSpecificPart());
    System.out.println("getPath "+uri.getPath());
    System.out.println("getHost "+uri.getHost());
    System.out.println("getPort "+uri.getPort());
    System.out.println("getAuthority "+uri.getAuthority());
    System.out.println("getFragment "+uri.getFragment());
    System.out.println("getQuery "+uri.getQuery());
    System.out.println("getUserInfo "+uri.getUserInfo());
    System.out.println("relativize "+new URI("http://localhost/level1").relativize(uri).toASCIIString());
System.out.println(">>>>> testIdentifier ..... ok");
  }  
  
  public void testBind() throws Exception {
    
    ResourceRouter reg = (ResourceRouter)container.getComponentInstanceOfType(ResourceRouter.class);
    assertNotNull(reg);
    DummyResourceWrapper dw = new DummyResourceWrapper();
    reg.bind(dw);
    List <ResourceDescriptor> list = reg.getAllDescriptors();
    assertEquals(1, list.size());
    ResourceDescriptor d = list.get(0);
    assertEquals(DummyResourceWrapper.TEST_HTTP_METHOD1, d.getAcceptableMethod());
    assertEquals(DummyResourceWrapper.TEST_METHOD_NAME1, d.getServer().getName());
    assertEquals(DummyResourceWrapper.TEST_URI1, d.getURIPattern().getString());
    
    reg.unbind(dw);
    assertEquals(0, list.size());
System.out.println(">>>>> testBind ..... ok");
  }

  public void testBind2() throws Exception {
    
    ResourceRouter reg = (ResourceRouter)container.getComponentInstanceOfType(ResourceRouter.class);
    assertNotNull(reg);
    DummyResourceWrapper_1 dw1 = new DummyResourceWrapper_1();
    DummyResourceWrapper_2 dw2 = new DummyResourceWrapper_2();
    DummyResourceWrapper_3 dw3 = new DummyResourceWrapper_3();
    reg.bind(dw1);
    List <ResourceDescriptor> list = reg.getAllDescriptors();
    assertEquals(1, list.size());
    try {
      reg.bind(new DummyResourceWrapper_2());
    }catch(InvalidResourceDescriptorException e) {;}
    assertEquals(1, list.size());
    reg.unbind(dw1);
    assertEquals(0, list.size());

    reg.bind(dw2);
    assertEquals(1, list.size());
    try {
      reg.bind(new DummyResourceWrapper_1());
    }catch(InvalidResourceDescriptorException e) {;}
    assertEquals(1, list.size());
    reg.unbind(dw2);
    assertEquals(0, list.size());

    reg.bind(dw1);
    assertEquals(1, list.size());
    reg.bind(dw3);
    assertEquals(2, list.size());
    reg.unbind(dw1);
    reg.unbind(dw3);
    assertEquals(0, list.size());

    reg.bind(dw2);
    assertEquals(1, list.size());
    reg.bind(dw3);
    assertEquals(2, list.size());
    reg.unbind(dw2);
    reg.unbind(dw3);
    assertEquals(0, list.size());

System.out.println(">>>>> testBind2 ...... ok");
  }

  
//  public void testBindInvalid() throws Exception {
//    
//    ResourceRouter reg = (ResourceRouter)container.getComponentInstanceOfType(ResourceRouter.class);
//    reg.bind(new DummyResourceWrapper1());
//    reg.bind(new DummyResourceWrapper());
//    
//    reg.clear();
//    
//  }


//  public void testBindMultiple() throws Exception {
//    
//    ResourceRouter reg = (ResourceRouter)container.getComponentInstanceOfType(ResourceRouter.class);
//    reg.bind(new DummyResourceWrapper());
//    List <ResourceDescriptor> list = reg.getAllDescriptors();
//    assertEquals(1, list.size());
//    ResourceDescriptor d = list.get(0);
//    assertEquals(DummyResourceWrapper.TEST_HTTP_METHOD1, d.getAcceptableMethod());
//    assertEquals(DummyResourceWrapper.TEST_METHOD_NAME1, d.getServer().getName());
//    assertEquals(DummyResourceWrapper.TEST_URI1,d.getURIPattern().getString());
//    
//    reg.unbind(DummyResourceWrapper.TEST_URI1);
//    assertEquals(0, list.size());
//    
//  }

  public void testParametrizedURIPattern0() throws Exception {
    // no params
    URIPattern pattern = new URIPattern("/level1/level2");
    assertEquals(0, pattern.getParamNames().size());
    assertTrue(pattern.matches("/level1/level2"));
    assertFalse(pattern.matches("/level11/level2"));
    assertFalse(pattern.matches("/level11/level2/level3"));
System.out.println(">>>>> testParametrizedURIPattern0 ..... ok");
  }

  public void testParametrizedURIPattern1() throws Exception {
    // one param
    URIPattern pattern = new URIPattern("/level1/level2/{id}/");
    assertEquals(1, pattern.getParamNames().size());
    assertEquals("id", pattern.getParamNames().iterator().next());
    assertTrue(pattern.matches("/level1/level2/test/"));
    assertFalse(pattern.matches("/level1/level2/test"));
    assertFalse(pattern.matches("/level1/level2"));
    Map params = pattern.parse("/level1/level2/test/");
    assertEquals(1, params.size());
    assertEquals("test/", params.get("id"));
System.out.println(">>>>> testParametrizedURIPattern1 ..... ok");

    // TODO
//    URIPattern pattern1 = new URIPattern("/any");
//    assertFalse(pattern1.matches("/any/test/ttt"));
    
    
  }

  public void testParametrizedURIPattern2() throws Exception {
    // two params
    URIPattern pattern = new URIPattern("/level1/level2/{id}/level4/{id2}/");
    assertEquals(2, pattern.getParamNames().size());
    Iterator it = pattern.getParamNames().iterator();
    while(it.hasNext()) {
      Object o = it.next();
      if(!o.equals("id") && !o.equals("id2"))
        fail("Key is not id nor id2");
    }
    assertFalse(pattern.matches("/level1/level2/test/level4/"));
    assertTrue(pattern.matches("/level1/level2/test3/level4/test5/"));
    assertFalse(pattern.matches("/level1/level2"));
    assertFalse(pattern.matches("/level1/level2/test/"));
    Map params = pattern.parse("/level1/level2/test3/level4/test5/");
    assertEquals(2, params.size());
    assertEquals("test3", params.get("id"));
    assertEquals("test5/", params.get("id2"));
System.out.println(">>>>> testParametrizedURIPattern2 ..... ok");
  }
  

  public void testServe() throws Exception {
    
    ResourceRouter reg = (ResourceRouter)container.getComponentInstanceOfType(ResourceRouter.class);
    assertNotNull(reg);
    
    DummyResourceWrapper dw = new DummyResourceWrapper();
    reg.bind(dw);
    
//    Request req = new Request(new ResourceIdentifier(DummyResourceWrapper.TEST_URI1), 
    Request req = new Request(new ResourceIdentifier("/level1/myID/level3/"), 
        new StringRepresentation("text/plain"),
        new ControlData(DummyResourceWrapper.TEST_HTTP_METHOD1, null));
    Response resp = new Response(req);
    
    reg.serve(req, resp);
    
    assertEquals(DummyResourceWrapper.TEST_METHOD_NAME1, resp.getEntity().getString());
    assertEquals(DummyResourceWrapper.TEST_METHOD_NAME1.length(), resp.getEntity().getLength());

    reg.unbind(dw);

System.out.println(">>>>> testServe ..... ok");
    System.out.println("RESSSSP >>>>>>> "+resp+" "+resp.getEntity().getString());
  }

  public void testServeWithParametrizedMapping() throws Exception {
    
    ResourceRouter reg = (ResourceRouter)container.getComponentInstanceOfType(ResourceRouter.class);
    assertNotNull(reg);
    
    reg.bind(new DummyResourceWrapper1());
    
    Request req = new Request(new ResourceIdentifier("/any/myID/"), 
        new StringRepresentation("text/plain"),
        new ControlData(DummyResourceWrapper1.TEST_HTTP_METHOD2, null));
    Response resp = new Response(req);
    
    reg.serve(req, resp);
    
//    assertEquals("myID", resp.getEntity().getString());
System.out.println(">>>>> testServeWithParametrizedMapping ..... ok");
  }
}
