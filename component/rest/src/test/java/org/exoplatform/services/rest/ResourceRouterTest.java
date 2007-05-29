/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.net.URI;
import java.util.Date;
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
  } 

  public void testBind() throws Exception {
    ResourceRouter reg = (ResourceRouter)container.getComponentInstanceOfType(ResourceRouter.class);
    assertNotNull(reg);
    reg.clear();
    List <ResourceDescriptor> list = reg.getAllDescriptors();
    AnnotatedParamWrapper dw = new AnnotatedParamWrapper();
    reg.bind(dw);
    assertEquals(1, list.size());
    ResourceDescriptor d = list.get(0);
    assertEquals("GET", d.getAcceptableMethod());
    assertEquals("method1", d.getServer().getName());
    assertEquals("/level1/{id}/level3/", d.getURIPattern().getString());
    
    reg.unbind(dw);
    assertEquals(0, list.size());
  }

  public void testBind2() throws Exception {
    ResourceRouter reg = (ResourceRouter)container.getComponentInstanceOfType(ResourceRouter.class);
    assertNotNull(reg);
    TestBindDummyResourceWrapper1 dw1 = new TestBindDummyResourceWrapper1();
    TestBindDummyResourceWrapper2 dw2 = new TestBindDummyResourceWrapper2();
    TestBindDummyResourceWrapper3 dw3 = new TestBindDummyResourceWrapper3();
    reg.bind(dw1);
    List <ResourceDescriptor> list = reg.getAllDescriptors();
    assertEquals(1, list.size());
    try {
      reg.bind(new TestBindDummyResourceWrapper2());
    }catch(InvalidResourceDescriptorException e) {;}
    assertEquals(1, list.size());
    reg.unbind(dw1);
    assertEquals(0, list.size());

    reg.bind(dw2);
    assertEquals(1, list.size());
    try {
      reg.bind(new TestBindDummyResourceWrapper1());
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

  }

  public void testParametrizedURIPattern0() throws Exception {
    // no params
    URIPattern pattern = new URIPattern("/level1/level2");
    assertEquals(0, pattern.getParamNames().size());
    assertTrue(pattern.matches("/level1/level2"));
    assertFalse(pattern.matches("/level11/level2"));
    assertFalse(pattern.matches("/level11/level2/level3"));
  }

  public void testParametrizedURIPattern1() throws Exception {
    // one param
    URIPattern pattern = new URIPattern("/level1/level2/{id}/");
    assertEquals(1, pattern.getParamNames().size());
    assertEquals("id", pattern.getParamNames().iterator().next());
    assertTrue(pattern.matches("/level1/level2/test/"));
    assertFalse(pattern.matches("/level1/level2/test"));
    assertFalse(pattern.matches("/level1/level2"));
    assertFalse(pattern.matches("/level1/"));
    Map<String, String> params = pattern.parse("/level1/level2/test/");
    assertEquals(1, params.size());
    assertEquals("test/", params.get("id"));
  }

  public void testParametrizedURIPattern2() throws Exception {
    // two params
    URIPattern pattern = new URIPattern("/level1/level2/{id}/level4/{id2}/");
    assertEquals(2, pattern.getParamNames().size());
    Iterator<String> it = pattern.getParamNames().iterator();
    while(it.hasNext()) {
      Object o = it.next();
      if(!o.equals("id") && !o.equals("id2"))
        fail("Key is not id nor id2");
    }
    assertFalse(pattern.matches("/level1/level2/test/level4/"));
    assertTrue(pattern.matches("/level1/level2/test3/level4/test5/"));
    assertFalse(pattern.matches("/level1/level2"));
    assertFalse(pattern.matches("/level1/level2/test/"));
    Map<String, String> params = pattern.parse("/level1/level2/test3/level4/test5/");
    assertEquals(2, params.size());
    assertEquals("test3", params.get("id"));
    assertEquals("test5/", params.get("id2"));
  }

  public void testServe() throws Exception {
    ResourceRouter reg = (ResourceRouter)container.getComponentInstanceOfType(ResourceRouter.class);
    assertNotNull(reg);

    List <ResourceDescriptor> list = reg.getAllDescriptors();
    AnnotatedParamWrapper dw = new AnnotatedParamWrapper();
    reg.bind(dw);
    assertEquals(1, list.size());

    Request req = new Request(new ResourceIdentifier("/level1/myID/level3/"),
        "GET", new StringRepresentation("text/plain"));
    Response resp = reg.serve(req);
    assertEquals("method1".length(), resp.getEntity().getLength());
    assertEquals("method1", resp.getEntity().getString());
    reg.unbind(dw);
    assertEquals(0, list.size());
    System.out.println("RESPONSE >>>>>>> " + resp.getEntity().getString());
  }

  public void testServeAnnotatedClass() throws Exception {
    ResourceRouter reg = (ResourceRouter)container.getComponentInstanceOfType(ResourceRouter.class);
    assertNotNull(reg);

    List <ResourceDescriptor> list = reg.getAllDescriptors();
    AnnotatedWrapper dw = new AnnotatedWrapper();
    reg.bind(dw);
    assertEquals(1, list.size());

    Request req = new Request(new ResourceIdentifier("/level1/level2/level3/" +
    		"hello_world_annotated_class"),
        "GET", new StringRepresentation("text/plain"));
    Response resp = reg.serve(req);
    assertEquals("method1".length(), resp.getEntity().getLength());
    assertEquals("method1", resp.getEntity().getString());
    reg.unbind(dw);
    assertEquals(0, list.size());
  }

  public void testServeWithParametrizedMapping() throws Exception {
    
    ResourceRouter reg = (ResourceRouter)container.getComponentInstanceOfType(ResourceRouter.class);
    assertNotNull(reg);
    reg.clear();
    List <ResourceDescriptor> list = reg.getAllDescriptors();
    AnnotatedParamWrapper1 dw1 = new AnnotatedParamWrapper1();
    reg.bind(dw1);
    Request req = new Request(new ResourceIdentifier("/level1/level2/"),
        "GET", new StringRepresentation("text/plain"));
    req.setAcceptedMediaType("text/html");
    Response resp = reg.serve(req);
    assertEquals("text/html", resp.getAcceptedMediaType());
    assertEquals("method1", resp.getEntity().getString());
    
    req = new Request(new ResourceIdentifier("/level1/level2/"),
        "GET", new StringRepresentation("text/plain"));
    req.setAcceptedMediaType("text/xml");
    resp = reg.serve(req);
    assertEquals("text/xml", resp.getAcceptedMediaType());
    assertEquals("method2", resp.getEntity().getString());
    
    req = new Request(new ResourceIdentifier("/level1/hello/level3/world/level4/good/"),
        "POST", new StringRepresentation("text/plain"));
    resp = reg.serve(req);
    assertEquals("method3", resp.getEntity().getString());
    reg.unbind(dw1);
    assertEquals(0, list.size());
  }

}
