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
  private ResourceBinder binder;
  private ResourceDispatcher dispatcher;
  
  public void setUp() throws Exception {
    StandaloneContainer.setConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");
    container = StandaloneContainer.getInstance();
    binder = (ResourceBinder)container.getComponentInstanceOfType(ResourceBinder.class);
    binder.clear();
    dispatcher = (ResourceDispatcher)container.getComponentInstanceOfType(ResourceDispatcher.class);
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
    assertNotNull(binder);
    binder.clear();
    List <ResourceDescriptor> list = binder.getAllDescriptors();
    ResourceContainer2 ac = new ResourceContainer2();
    binder.bind(ac);
    assertEquals(2, list.size());
    
    binder.clear();
    assertEquals(0, list.size());
  }

  public void testBind2() throws Exception {
    assertNotNull(dispatcher);
    assertNotNull(binder);
    List <ResourceDescriptor> list = binder.getAllDescriptors();
    assertEquals(0, list.size());
    ResourceContainer3 ac2 = new ResourceContainer3();
    try {
      binder.bind(ac2);
    }catch(InvalidResourceDescriptorException e) {;}
    assertEquals(4, list.size());
    binder.unbind(ac2);
    assertEquals(0, list.size());
  }

  public void testParametrizedURIPattern0() throws Exception {
    // no params
    URIPattern pattern = new URIPattern("/level1/level2/");
    assertEquals(0, pattern.getParamNames().size());
    assertTrue(pattern.matches("/level1/level2/"));
    assertFalse(pattern.matches("/level11/level2/"));
    assertFalse(pattern.matches("/level11/level2/level3/"));
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

  public void testParametrizedURIPattern3() throws Exception {
    // three params
    URIPattern pattern = new URIPattern("/level1/{id1}/{id2}/{id3}/");
    assertEquals(3, pattern.getParamNames().size());
    
    assertTrue(pattern.matches("/level1/t/e/st/"));
    assertTrue(pattern.matches("/level1/level/2/te/st/"));
    assertTrue(pattern.matches("/level1/le/vel/2/"));
    assertTrue(pattern.matches("/level1/level2/te/st/"));

    Map<String, String> params = pattern.parse("/level1/t/e/s/t/");
    assertEquals(3, params.size());
    assertEquals("t",  params.get("id1"));
    assertEquals("e",  params.get("id2"));
    assertEquals("s/t", params.get("id3"));
    try {
      params = pattern.parse("/level1/tes/t/");
      fail("Exception should be here!");
    } catch(Exception e) {}
  }

  public void testParametrizedURIPattern4() throws Exception {
    URIPattern pattern = new URIPattern("/level1/{id1}/");
    assertEquals(1, pattern.getParamNames().size());
    assertTrue(pattern.matches("/level1/l/e/v/e/l/2/t/e/s/t/"));
  }

  public void testServe0() throws Exception {
    assertNotNull(dispatcher);
    assertNotNull(binder);
    List <ResourceDescriptor> list = binder.getAllDescriptors();
    ResourceContainerGET resourceContainerGET = new ResourceContainerGET();
    binder.bind(resourceContainerGET);
    assertEquals(1, list.size());

    ResourceContainerPOST resourceContainerPOST = new ResourceContainerPOST();
    binder.bind(resourceContainerPOST);
    assertEquals(2, list.size());

    ResourceContainerGET resourceContainerGET1 = new ResourceContainerGET();
    ResourceContainerPOST resourceContainerPOST1 = new ResourceContainerPOST();
    try {
      binder.bind(resourceContainerGET1);
      fail("Bind for this component shuold be failed!");
    } catch(Exception e) {}
    assertEquals(2, list.size());
    try {
      binder.bind(resourceContainerPOST1);
      fail("Bind for this component shuold be failed!");
    } catch(Exception e) {}
    assertEquals(2, list.size());

    MultivaluedMetadata mm = new MultivaluedMetadata();
    Request request = new Request(null, new ResourceIdentifier("/level1/get/"),
        "GET", mm, null);
    dispatcher.dispatch(request);
    request = new Request(null, new ResourceIdentifier("/level1/post/"),
        "POST", mm, null);
    dispatcher.dispatch(request);
    binder.unbind(resourceContainerGET);
    binder.unbind(resourceContainerPOST);
    assertEquals(0, list.size());
  }

  public void testServe1() throws Exception {
    assertNotNull(dispatcher);
    assertNotNull(binder);

    List <ResourceDescriptor> list = binder.getAllDescriptors();
    ResourceContainer2 resourceContainer = new ResourceContainer2();
    binder.bind(resourceContainer);
    assertEquals(2, list.size());

    MultivaluedMetadata mm = new MultivaluedMetadata();
    mm.putSingle("accept", "text/html;q=0.8,text/xml,text/plain;q=0.5");
    mm.putSingle("test", "test_header");
    Request request = new Request(new ByteArrayInputStream("test string".getBytes()),
        new ResourceIdentifier("/level1/myID/level3/"), "GET", mm, null);
    Response resp = dispatcher.dispatch(request);
    resp.writeEntity(System.out);
    request = new Request(new ByteArrayInputStream("test string".getBytes()),
        new ResourceIdentifier("/level1/myID/level3/"), "POST", mm, null);
    resp = dispatcher.dispatch(request);
    System.out.println(">>> Content-Length: " + resp.getEntityMetadata().getLength());
    resp.writeEntity(System.out);
    System.out.println();
    binder.unbind(resourceContainer);
    assertEquals(0, list.size());
  }

  public void testServe2() throws Exception {
    assertNotNull(dispatcher);
    assertNotNull(binder);

    List <ResourceDescriptor> list = binder.getAllDescriptors();
    ResourceContainer3 resourceContainer = new ResourceContainer3();
    binder.bind(resourceContainer);
    assertEquals(4, list.size());

    MultivaluedMetadata mm = new MultivaluedMetadata();
    mm.putSingle("accept", "*/*");
    Request request = new Request(new ByteArrayInputStream("create something".getBytes()),
        new ResourceIdentifier("/level1/myID/le vel3/"), "POST", mm, null);
    Response resp = dispatcher.dispatch(request);
    assertEquals("http://localhost/test/_post", resp.getResponseHeaders().getFirst("Location"));

    request = new Request(new ByteArrayInputStream("recreate something".getBytes()),
        new ResourceIdentifier("/level1/myID/le vel3/"), "PUT", mm, null);
    resp = dispatcher.dispatch(request);
    System.out.println(">>> Content-Length: " + resp.getEntityMetadata().getLength());
    assertEquals("http://localhost/test/_put", resp.getResponseHeaders().getFirst("Location"));
    assertEquals("text/plain", resp.getEntityMetadata().getMediaType());
    System.out.println(">>> Content-Length: " + resp.getEntityMetadata().getLength());
    resp.writeEntity(System.out);

    request = new Request(new ByteArrayInputStream("delete something".getBytes()),
        new ResourceIdentifier("/level1/myID/le vel3/test"), "DELETE", mm, null);
    resp = dispatcher.dispatch(request);
    System.out.println(">>> Content-Length: " + resp.getEntityMetadata().getLength());
    resp.writeEntity(System.out);

    request = new Request(new ByteArrayInputStream("get something".getBytes()),
        new ResourceIdentifier("/level1/myID/le vel3/test"), "get", mm, null);
    resp = dispatcher.dispatch(request);
    System.out.println(">>> Content-Length: " + resp.getEntityMetadata().getLength());
    System.out.println(">>> Cache-Control: " + resp.getEntityMetadata().getCacheControl());
    resp.writeEntity(System.out);
    
    binder.unbind(resourceContainer);
    assertEquals(0, list.size());
  }

  public void testServe3() throws Exception {
    assertNotNull(dispatcher);
    assertNotNull(binder);

    List <ResourceDescriptor> list = binder.getAllDescriptors();
    ResourceContainer4 resourceContainer = new ResourceContainer4();
    binder.bind(resourceContainer);
    assertEquals(5, list.size());

    MultivaluedMetadata mm = new MultivaluedMetadata();
    mm.putSingle("accept", "*/*");
    Request request = new Request(null, new ResourceIdentifier("/level1/myID1/"), "GET", mm, null);
    dispatcher.dispatch(request);
    request = new Request(null, new ResourceIdentifier("/level1/myID1/myID2/"), "GET", mm, null);
    dispatcher.dispatch(request);
    request = new Request(null, new ResourceIdentifier("/level1/myID1/myID2/myID3/"), "GET", mm, null);
    dispatcher.dispatch(request);
    request = new Request(null, new ResourceIdentifier("/level1/myID1/myID2/myID3/myID4/"), "GET", mm, null);
    dispatcher.dispatch(request);
    request = new Request(null, new ResourceIdentifier("/level1/myID1/myID2/myID3/myID4/myID5/"), "GET", mm, null);
    dispatcher.dispatch(request);
    request = new Request(null, new ResourceIdentifier("/level1/myID1/myID2/myID3/myID4/m/y/I/D/5"), "GET", mm, null);
    dispatcher.dispatch(request);
    
    binder.unbind(resourceContainer);
    assertEquals(0, list.size());
  }
  
  public void testServeAnnotatedClass() throws Exception {
    assertNotNull(dispatcher);
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
    Response resp = dispatcher.dispatch(request);
    System.out.println(">>> Content-Length: " + resp.getEntityMetadata().getLength());
    assertEquals("text/plain", resp.getEntityMetadata().getMediaType());
//    resp.writeEntity(new FileOutputStream(new File("/tmp/test.txt")));
    resp.writeEntity(System.out);
    binder.unbind(dw);
    assertEquals(0, list.size());
  }

  public void testJAXBTransformetion() throws Exception {
    assertNotNull(dispatcher);
    assertNotNull(binder);

    List <ResourceDescriptor> list = binder.getAllDescriptors();
    ResourceContainerJAXB resourceContainer = new ResourceContainerJAXB();
    binder.bind(resourceContainer);
    assertEquals(1, list.size());

    FileInputStream f = new FileInputStream("src/test/resources/book-in.xml");
    
    MultivaluedMetadata mm = new MultivaluedMetadata();
    Request request = new Request(f, new ResourceIdentifier("/test/jaxb"), "GET", mm, null);
    Response resp = dispatcher.dispatch(request);
    System.out.println(">>> Content-Length: " + resp.getEntityMetadata().getLength());
    System.out.println(">>> Cache-Control: " + resp.getEntityMetadata().getCacheControl());
    assertEquals("text/xml", resp.getEntityMetadata().getMediaType());
//    resp.writeEntity(new FileOutputStream(new File("/tmp/output.xml")));
    resp.writeEntity(System.out);
    binder.unbind(resourceContainer);
    assertEquals(0, list.size());
  }
  
  public void testSerializable() throws Exception {
  	assertNotNull(dispatcher);
  	assertNotNull(binder);
  	
  	List <ResourceDescriptor> list = binder.getAllDescriptors();
  	ResourceContainerSimpleSerializableEntity resourceContainer = new ResourceContainerSimpleSerializableEntity();
  	binder.bind(resourceContainer);
  	assertEquals(1, list.size());
  	
    MultivaluedMetadata mm = new MultivaluedMetadata();
    Request request =
    	new Request(new ByteArrayInputStream("this is request data".getBytes()),
    			new ResourceIdentifier("/test/serializable"), "GET", mm, null);
    Response resp = dispatcher.dispatch(request);
    System.out.println(">>> Content-Length: " + resp.getEntityMetadata().getLength());
    resp.writeEntity(System.out);
  }

}
