/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.jcr.ItemNotFoundException;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.Request;
import org.exoplatform.services.rest.ResourceBinder;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.ResourceIdentifier;
import org.exoplatform.services.rest.container.ResourceDescriptor;
import org.exoplatform.services.rest.MultivaluedMetadata;
import org.exoplatform.services.rest.Response;


public class RegistryTest extends BaseStandaloneTest{
  
  @Override
  public void setUp() throws Exception {

    super.setUp();
  }

  public void testInit() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);
    assertNotNull(regService);
      
//    ManageableRepository rep = ((RepositoryService) container
//    .getComponentInstanceOfType(RepositoryService.class)).getDefaultRepository();
    
    SessionProvider sp = new SessionProvider(credentials);
    assertNotNull(regService.getRegistry(sp).getNode());
    assertTrue(regService.getRegistry(sp).getNode().hasNode(RegistryService.EXO_SERVICES));
    assertTrue(regService.getRegistry(sp).getNode().hasNode(RegistryService.EXO_APPLICATIONS));
    assertTrue(regService.getRegistry(sp).getNode().hasNode(RegistryService.EXO_USERS));
    
    session.getWorkspace().getNodeTypeManager().getNodeType("exo:registry");
    session.getWorkspace().getNodeTypeManager().getNodeType("exo:registryEntry");
    session.getWorkspace().getNodeTypeManager().getNodeType("exo:registryGroup");

  }
   
  public void testRegister() throws Exception {
    RegistryService regService = (RegistryService) container
    .getComponentInstanceOfType(RegistryService.class);

//    ManageableRepository rep = ((RepositoryService) container
//        .getComponentInstanceOfType(RepositoryService.class)).getDefaultRepository();

    SessionProvider sp = new SessionProvider(credentials);

    try {
      Document doc = regService.getEntry(sp, RegistryService.EXO_USERS, "exo_user");
      fail("ItemNotFoundException should have been thrown");
    } catch (ItemNotFoundException e) {}
    
    File entryFile = new File("src/test/java/org/exoplatform/services/jcr/ext/registry/exo_user.xml"); 
    regService.createEntry(sp, RegistryService.EXO_USERS, 
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(entryFile));
    
    Document doc = regService.getEntry(sp, RegistryService.EXO_USERS, "exo_user");
    TransformerFactory.newInstance().newTransformer().transform(
        new DOMSource(doc), new StreamResult(System.out));

    regService.recreateEntry(sp, RegistryService.EXO_USERS, 
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(entryFile));

    regService.removeEntry(sp, RegistryService.EXO_USERS, "exo_user");
    
    try {
      regService.getEntry(sp, RegistryService.EXO_USERS, "exo_user");
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

    MultivaluedMetadata mv = new MultivaluedMetadata();
    System.out.println("-----REST-----");
    // registry should be empty
    Request request = new Request(null, new ResourceIdentifier("/registry/db1/"),
        "GET", mv, null, "http://localhost:8080/rest/registry/db1/");
    Response<?> response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
    response.writeEntity(System.out);
    // request to exo:services/exo_service
    // request status should be 404 (NOT_FOUND)
    request = new Request(null, new ResourceIdentifier("/registry/db1/" + RegistryService.EXO_SERVICES +
    		"/exo_service"), "GET", mv, null, "http://localhost:8080/rest/registry/db1/" + 
    RegistryService.EXO_SERVICES + "/exo_service");
    response = dispatcher.dispatch(request);
    assertEquals(404, response.getStatus());
    response.writeEntity(System.out);
    // create exo:services/exo_service
    FileInputStream fin =
      new FileInputStream(new File("src/test/java/org/exoplatform/services/jcr/ext/registry/exo_service.xml"));
    request = new Request(fin, new ResourceIdentifier("/registry/db1/" + RegistryService.EXO_SERVICES),
        "POST", mv, null, "http://localhost:8080/rest/registry/db1/");
    response = dispatcher.dispatch(request);
    assertEquals(201, response.getStatus());
    response.writeEntity(System.out);
    // request to exo:services/exo_service
    request = new Request(null, new ResourceIdentifier("/registry/db1/" + RegistryService.EXO_SERVICES +
        "/exo_service"), "GET", mv, null, "http://localhost:8080/rest/registry/db1/" + 
        RegistryService.EXO_SERVICES + "/exo_service");
    response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
    response.writeEntity(System.out);
    // registry
    request = new Request(null, new ResourceIdentifier("/registry/db1/"),
        "GET", mv, null, "http://localhost:8080/rest/registry/db1/");
    response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
    response.writeEntity(System.out);
    // recreate exo:services/exo_service
    fin = new FileInputStream(
        new File("src/test/java/org/exoplatform/services/jcr/ext/registry/exo_service.xml"));
    request = new Request(fin, new ResourceIdentifier("/registry/db1/" + RegistryService.EXO_SERVICES),
        "PUT", mv, null, "http://localhost:8080/rest/registry/db1/");
    response = dispatcher.dispatch(request);
    assertEquals(201, response.getStatus());
    response.writeEntity(System.out);
    // delete exo:services/exo_service
    request = new Request(null, new ResourceIdentifier("/registry/db1/" + RegistryService.EXO_SERVICES +
        "/exo_service"), "DELETE", mv, null, "http://localhost:8080/rest/registry/db1/" + 
    RegistryService.EXO_SERVICES + "/exo_service");
    response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
    response.writeEntity(System.out);
    // request to exo:services/exo_service
    // request status should be 404 (NOT_FOUND)
    request = new Request(null, new ResourceIdentifier("/registry/db1/" + RegistryService.EXO_SERVICES +
    		"/exo_service"), "GET", mv, null, "http://localhost:8080/rest/registry/db1/" + 
    RegistryService.EXO_SERVICES + "/exo_service");
    response = dispatcher.dispatch(request);
    assertEquals(404, response.getStatus());
    response.writeEntity(System.out);
  }
  
}
