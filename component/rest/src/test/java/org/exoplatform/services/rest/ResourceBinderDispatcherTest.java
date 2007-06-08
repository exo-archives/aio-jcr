/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.rest;

import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.rest.container.InvalidResourceDescriptorException;
import org.exoplatform.services.rest.container.ResourceDescriptor;
import org.exoplatform.services.rest.data.StringRepresentation;
import org.exoplatform.services.rest.data.BaseEntity;
import org.exoplatform.services.rest.data.StringEntityTransformer;


/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id:$
 */
public class ResourceBinderDispatcherTest extends TestCase {

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

  public void testTransformerBind() {
    TransformerBinder tbinder = 
      (TransformerBinder)container.getComponentInstanceOfType(TransformerBinder.class);
    assertNotNull(tbinder);
    StringEntityTransformer transf = new StringEntityTransformer();
    tbinder.bind(transf);
    assertEquals(1, tbinder.transformersNumber());
    tbinder.unbind(transf);
    assertEquals(0, tbinder.transformersNumber());
  }
  
  
  public void testBind() throws Exception {
    ResourceBinder binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(binder);
    binder.clear();
    List <ResourceDescriptor> list = binder.getAllDescriptors();
    AnnotatedParamContainer ac = new AnnotatedParamContainer();
    binder.bind(ac);
    assertEquals(1, list.size());
    ResourceDescriptor d = list.get(0);
    assertEquals("GET", d.getAcceptableMethod());
    assertEquals("method1", d.getServer().getName());
    assertEquals("/level1/{id}/level3/", d.getURIPattern().getString());
    
    binder.unbind(ac);
    assertEquals(0, list.size());
  }

  public void testBind2() throws Exception {
    ResourceDispatcher disp = (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
    assertNotNull(disp);
    ResourceBinder binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(binder);
    TestBindDummyResourceContainer1 ac1 = new TestBindDummyResourceContainer1();
    TestBindDummyResourceContainer2 ac2 = new TestBindDummyResourceContainer2();
    TestBindDummyResourceContainer3 ac3 = new TestBindDummyResourceContainer3();
    binder.bind(ac1);
    List <ResourceDescriptor> list = binder.getAllDescriptors();
    assertEquals(1, list.size());
    try {
      binder.bind(new TestBindDummyResourceContainer2());
    }catch(InvalidResourceDescriptorException e) {;}
    assertEquals(1, list.size());
    binder.unbind(ac1);
    assertEquals(0, list.size());

    binder.bind(ac2);
    assertEquals(1, list.size());
    try {
      binder.bind(new TestBindDummyResourceContainer1());
    }catch(InvalidResourceDescriptorException e) {;}
    assertEquals(1, list.size());
    binder.unbind(ac2);
    assertEquals(0, list.size());

    binder.bind(ac1);
    assertEquals(1, list.size());
    binder.bind(ac3);
    assertEquals(2, list.size());
    binder.unbind(ac1);
    binder.unbind(ac3);
    assertEquals(0, list.size());

    binder.bind(ac2);
    assertEquals(1, list.size());
    binder.bind(ac3);
    assertEquals(2, list.size());
    binder.unbind(ac2);
    binder.unbind(ac3);
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
    ResourceDispatcher disp = (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
    assertNotNull(disp);
    ResourceBinder binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(binder);

    List <ResourceDescriptor> list = binder.getAllDescriptors();
    AnnotatedParamContainer dw = new AnnotatedParamContainer();
    binder.bind(dw);
    assertEquals(1, list.size());

    Request request = Request.getInstance(null, new ResourceIdentifier("/level1/myID/level3/"),
        "GET", null, null);
    Response resp = disp.dispatch(request);
    assertEquals("method1", resp.getRepresentation().getData());
    binder.unbind(dw);
    assertEquals(0, list.size());
    System.out.println("RESPONSE >>>>>>> " + resp.getRepresentation().getData());
  }

  public void testServeAnnotatedClass() throws Exception {
    ResourceDispatcher disp = (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
    assertNotNull(disp);
    ResourceBinder binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(binder);

    List <ResourceDescriptor> list = binder.getAllDescriptors();
    AnnotatedContainer dw = new AnnotatedContainer();
    binder.bind(dw);
    assertEquals(1, list.size());

    ByteArrayInputStream ds = new ByteArrayInputStream("hello".getBytes());
    assertNotNull(ds);
    Request request = Request.getInstance(ds, 
        new ResourceIdentifier("/level1/level2/level3/hello"), "GET", null, null);
    Response resp = disp.dispatch(request);
    StringEntityTransformer transf = new StringEntityTransformer();
    assertTrue(transf.support(String.class));
    assertEquals("hello", transf.readFrom((java.io.InputStream)resp.getRepresentation().getData()));
    binder.unbind(dw);
    assertEquals(0, list.size());
  }

  
  public void testServeWithParametrizedMapping() throws Exception {
    ResourceDispatcher disp = (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
    assertNotNull(disp);
    ResourceBinder binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(binder);
    binder.clear();
    List <ResourceDescriptor> list = binder.getAllDescriptors();
    AnnotatedParamContainer1 ac1 = new AnnotatedParamContainer1();
    binder.bind(ac1);
    assertEquals(3, list.size());
    ByteArrayInputStream ds = new ByteArrayInputStream("hello".getBytes());
    Request request = Request.getInstance(null, new ResourceIdentifier("/level1/level2/"),
        "GET", null, null);
    request.setAcceptedMediaType("text/html");
    Response resp = disp.dispatch(request);
    assertEquals("text/html", resp.getAcceptedMediaType());
    assertEquals("method1", resp.getRepresentation().getData());
    
    request = Request.getInstance(null, new ResourceIdentifier("/level1/level2/"),
        "GET", null, null);
    request.setAcceptedMediaType("text/xml");
    resp = disp.dispatch(request);
    assertEquals("text/xml", resp.getAcceptedMediaType());
    assertEquals("method2", resp.getRepresentation().getData());
    
    request = Request.getInstance(null,new ResourceIdentifier("/level1/hello/level3/world/level4/good/"),
        "POST", null, null);
    resp = disp.dispatch(request);
    assertEquals("method3", resp.getRepresentation().getData());
    binder.unbind(ac1);
    assertEquals(0, list.size());
  }
  
}
