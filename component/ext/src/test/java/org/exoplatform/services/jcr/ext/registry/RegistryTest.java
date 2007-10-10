/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.jcr.ItemNotFoundException;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.Request;
import org.exoplatform.services.rest.ResourceBinder;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.ResourceIdentifier;
import org.exoplatform.services.rest.container.ResourceDescriptor;
import org.exoplatform.services.rest.MultivaluedMetadata;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;


public class RegistryTest extends BaseStandaloneTest{
  
	private ThreadLocalSessionProviderService sessionProviderService;
	
	@Override
  public void setUp() throws Exception {

    super.setUp();
    this.sessionProviderService = (ThreadLocalSessionProviderService)
    		container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    sessionProviderService.setSessionProvider(null, new SessionProvider(credentials));
  }

  public void testInit() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);
    assertNotNull(regService);
      
//    ManageableRepository rep = ((RepositoryService) container
//    .getComponentInstanceOfType(RepositoryService.class)).getDefaultRepository();
    
//    SessionProvider sp = new SessionProvider(credentials);
    assertNotNull(regService.getRegistry(sessionProviderService.getSessionProvider(null)).getNode());
    assertTrue(regService.getRegistry(sessionProviderService.getSessionProvider(null)).getNode().hasNode(RegistryService.EXO_SERVICES));
    assertTrue(regService.getRegistry(sessionProviderService.getSessionProvider(null)).getNode().hasNode(RegistryService.EXO_APPLICATIONS));
    assertTrue(regService.getRegistry(sessionProviderService.getSessionProvider(null)).getNode().hasNode(RegistryService.EXO_USERS));
    
    session.getWorkspace().getNodeTypeManager().getNodeType("exo:registry");
    session.getWorkspace().getNodeTypeManager().getNodeType("exo:registryEntry");
    session.getWorkspace().getNodeTypeManager().getNodeType("exo:registryGroup");

  }
   
  public void testRegister() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);

//    ManageableRepository rep = ((RepositoryService) container
//        .getComponentInstanceOfType(RepositoryService.class)).getDefaultRepository();

//    SessionProvider sp = new SessionProvider(credentials);

    try {
      regService.getEntry(sessionProviderService.getSessionProvider(null),
      		RegistryService.EXO_USERS, "exo_user");
      fail("ItemNotFoundException should have been thrown");
    } catch (ItemNotFoundException e) {}
    
    File entryFile = new File("src/test/java/org/exoplatform/services/jcr/ext/registry/exo_user.xml");
    
    regService.createEntry(sessionProviderService.getSessionProvider(null),
    		RegistryService.EXO_USERS, RegistryEntry.parse(new FileInputStream(entryFile)));
    
    RegistryEntry entry = regService.getEntry(sessionProviderService.getSessionProvider(null),
    		RegistryService.EXO_USERS, "exo_user");
    PassthroughOutputTransformer transformer = new PassthroughOutputTransformer();
    transformer.writeTo(entry.getAsInputStream(), System.out);

    regService.recreateEntry(sessionProviderService.getSessionProvider(null), RegistryService.EXO_USERS, 
        RegistryEntry.parse(new FileInputStream(entryFile)));

    regService.removeEntry(sessionProviderService.getSessionProvider(null),
    		RegistryService.EXO_USERS, "exo_user");
    
    try {
      regService.getEntry(sessionProviderService.getSessionProvider(null),
      		RegistryService.EXO_USERS, "exo_user");
      fail("ItemNotFoundException should have been thrown");
    } catch (ItemNotFoundException e) {
    }

  }

  public void testLocation() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);
    
    regService.addRegistryLocation("wrong", "wrong");
  }
  
  public void testRESTRegservice() throws Exception {
    RESTRegistryService restRegService =
      (RESTRegistryService) container.getComponentInstanceOfType(RESTRegistryService.class);
    ResourceBinder binder =
      (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    ResourceDispatcher dispatcher =
      (ResourceDispatcher) container.getComponentInstanceOfType(ResourceDispatcher.class);
    assertNotNull(restRegService);
    assertNotNull(binder);
    assertNotNull(dispatcher);
    
    List<ResourceDescriptor> list = binder.getAllDescriptors();
    assertEquals(5, list.size());

    System.out.println("\n-----REST-----");
    
    MultivaluedMetadata mv = new MultivaluedMetadata();
    String baseURI = "http://localhost:8080/rest";
    String extURI = "/registry/db1/";
    File file = new File("src/test/java/org/exoplatform/services/jcr/ext/registry/exo_service.xml");

    // registry should be empty
    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
    Response response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
    System.out.println(">>> Content-Length: " + response.getEntityMetadata().getLength());
    response.writeEntity(System.out);
    System.out.println();
    // request to exo:services/exo_service
    // request status should be 404 (NOT_FOUND)
    request = new Request(null,
        new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES +	"/exo_service"), "GET", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(404, response.getStatus());
    response.writeEntity(System.out);
    System.out.println();

    // create exo:services/exo_service
    FileInputStream fin = new FileInputStream(file);
    request = new Request(fin, new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES),
        "POST", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(201, response.getStatus());
    System.out.println(">>> Content-Length: " + response.getEntityMetadata().getLength());
    response.writeEntity(System.out);
    System.out.println("\nRESPONSE HEADERS, LOCATION: " + response.getResponseHeaders().getFirst("Location"));

    // request to exo:services/exo_service
    request = new Request(null,
        new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES +
        "/exo_service"), "GET", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
    System.out.println(">>> Content-Length: " + response.getEntityMetadata().getLength());
    response.writeEntity(System.out);
    System.out.println();

    // registry
    request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
    System.out.println(">>> Content-Length: " + response.getEntityMetadata().getLength());
    response.writeEntity(System.out);
    System.out.println();

    // recreate exo:services/exo_service
    fin = new FileInputStream(file);
    request = new Request(fin, new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES),
        "PUT", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(201, response.getStatus());
    System.out.println("RESPONSE HEADERS, LOCATION: " + response.getResponseHeaders().getFirst("Location"));

    // delete exo:services/exo_service
    request = new Request(null, new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES +
        "/exo_service"), "DELETE", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(204, response.getStatus());
    System.out.println(">>> Content-Length: " + response.getEntityMetadata().getLength());
    response.writeEntity(System.out);
    System.out.println();
    
    // request to exo:services/exo_service
    // request status should be 404 (NOT_FOUND)
    request = new Request(null, new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES +
    		"/exo_service"), "GET", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(404, response.getStatus());
    System.out.println(">>> Content-Length: " + response.getEntityMetadata().getLength());
    response.writeEntity(System.out);
    System.out.println();
 
  }
}
