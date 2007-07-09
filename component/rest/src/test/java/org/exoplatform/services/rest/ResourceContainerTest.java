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
//import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.File;
//import org.w3c.dom.Document;

import junit.framework.TestCase;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.rest.container.InvalidResourceDescriptorException;
import org.exoplatform.services.rest.container.ResourceDescriptor;
//import org.exoplatform.services.rest.transformer.XMLInputTransformer;
//import org.exoplatform.services.rest.transformer.XMLOutputTransformer;


/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id:$
 */
public class ResourceContainerTest extends TestCase {

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
    ResourceBinder binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(binder);
    binder.clear();
    List <ResourceDescriptor> list = binder.getAllDescriptors();
    ResourceContainer2 ac = new ResourceContainer2();
    binder.bind(ac);
    assertEquals(1, list.size());
    ResourceDescriptor d = list.get(0);
    assertEquals("GET", d.getAcceptableMethod());
    assertEquals("method1", d.getServer().getName());
    assertEquals("/level1/{id}/level3/", d.getURIPattern().getString());
    
    binder.clear();
    assertEquals(0, list.size());
  }

  public void testBind2() throws Exception {
    ResourceDispatcher disp = (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
    assertNotNull(disp);
    ResourceBinder binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(binder);
    List <ResourceDescriptor> list = binder.getAllDescriptors();
    assertEquals(0, list.size());
    ResourceContainer3 ac2 = new ResourceContainer3();
    try {
      binder.bind(ac2);
    }catch(InvalidResourceDescriptorException e) {;}
    assertEquals(3, list.size());
    binder.unbind(ac2);
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
    assertEquals("test", params.get("id"));
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
    assertEquals("test5", params.get("id2"));
  }

  public void testServe() throws Exception {
    ResourceDispatcher disp = (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
    assertNotNull(disp);
    ResourceBinder binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(binder);

    List <ResourceDescriptor> list = binder.getAllDescriptors();
    ResourceContainer2 resourceContainer = new ResourceContainer2();
    binder.bind(resourceContainer);
    assertEquals(1, list.size());

    MultivaluedMetadata mm = new MultivaluedMetadata();
    mm.putSingle("accept", "text/html;q=0.8,text/xml,text/plain;q=0.5");
    Request request = new Request(new ByteArrayInputStream("test string".getBytes()),
        new ResourceIdentifier("/level1/myID/level3/"), "GET", mm, null);
    Response resp = disp.dispatch(request);
    resp.writeEntity(System.out);
    request = new Request(null, new ResourceIdentifier("/level1/myID/level3/"),
        "POST", mm, null);
    binder.unbind(resourceContainer);
    assertEquals(0, list.size());
  }

  public void testServe2() throws Exception {
    ResourceDispatcher disp =
      (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
    assertNotNull(disp);
    ResourceBinder binder =
      (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(binder);

    List <ResourceDescriptor> list = binder.getAllDescriptors();
    ResourceContainer3 resourceContainer = new ResourceContainer3();
    binder.bind(resourceContainer);
    assertEquals(3, list.size());

    MultivaluedMetadata mm = new MultivaluedMetadata();
    mm.putSingle("accept", "*/*");
    Request request = new Request(new ByteArrayInputStream("insert something".getBytes()),
        new ResourceIdentifier("/level1/myID/level3/"), "POST", mm, null);
    Response resp = disp.dispatch(request);
    assertEquals("http://localhost/test/_post", resp.getResponseHeaders().getFirst("Location"));

    request = new Request(new ByteArrayInputStream("create something".getBytes()),
        new ResourceIdentifier("/level1/myID/level3/"), "PUT", mm, null);
    resp = disp.dispatch(request);
    assertEquals("http://localhost/test/_put", resp.getResponseHeaders().getFirst("Location"));
    assertEquals("text/plain", resp.getEntityMetadata().getMediaType());
    resp.writeEntity(System.out);

    request = new Request(new ByteArrayInputStream("delete something".getBytes()),
        new ResourceIdentifier("/level1/myID/level3/test"), "DELETE", mm, null);
    resp = disp.dispatch(request);
    resp.writeEntity(System.out);
    binder.unbind(resourceContainer);
    assertEquals(0, list.size());
  }

  public void testServeAnnotatedClass() throws Exception {
    ResourceDispatcher disp = (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
    assertNotNull(disp);
    ResourceBinder binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(binder);

    List <ResourceDescriptor> list = binder.getAllDescriptors();
    ResourceContainerAnnot dw = new ResourceContainerAnnot();
    binder.bind(dw);
    assertEquals(1, list.size());

    ByteArrayInputStream ds = new ByteArrayInputStream("hello".getBytes());
    MultivaluedMetadata mm = new MultivaluedMetadata();
    mm.putSingle("accept", "text/plain");
    Request request = new Request(ds, 
        new ResourceIdentifier("/level1/level2/level3/myID1/myID2"), "GET", mm, null);
    Response resp = disp.dispatch(request);
    assertEquals("text/plain", resp.getEntityMetadata().getMediaType());
//    resp.writeEntity(new FileOutputStream(new File("/tmp/test.txt")));
    resp.writeEntity(System.out);
    binder.unbind(dw);
    assertEquals(0, list.size());
  }

  public void testJAXBTransformetion() throws Exception {
    ResourceDispatcher disp = (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
    assertNotNull(disp);
    ResourceBinder binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    assertNotNull(binder);

    List <ResourceDescriptor> list = binder.getAllDescriptors();
    ResourceContainerJAXB resourceContainer = new ResourceContainerJAXB();
    binder.bind(resourceContainer);
    assertEquals(1, list.size());

    FileInputStream f = new FileInputStream("src/test/resources/book-in.xml");
    
    MultivaluedMetadata mm = new MultivaluedMetadata();
    Request request = new Request(f, new ResourceIdentifier("/test/jaxb"), "GET", mm, null);
    Response resp = disp.dispatch(request);
    assertEquals("text/xml", resp.getEntityMetadata().getMediaType());
//    resp.writeEntity(new FileOutputStream(new File("/tmp/output.xml")));
    resp.writeEntity(System.out);
    binder.unbind(resourceContainer);
    assertEquals(0, list.size());
  }
  
  public void testTesting() throws Exception {
  	ResourceDispatcher disp = (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
  	assertNotNull(disp);
  	ResourceBinder binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
  	assertNotNull(binder);
  	
  	List <ResourceDescriptor> list = binder.getAllDescriptors();
  	ResourceContainerSimpleSerializableEntity resourceContainer = new ResourceContainerSimpleSerializableEntity();
  	binder.bind(resourceContainer);
  	assertEquals(1, list.size());
  	
    MultivaluedMetadata mm = new MultivaluedMetadata();
    Request request =
    	new Request(new ByteArrayInputStream("this is request data".getBytes()),
    			new ResourceIdentifier("/test/serializable"), "GET", mm, null);
    Response resp = disp.dispatch(request);
    resp.writeEntity(System.out);
  }

}
