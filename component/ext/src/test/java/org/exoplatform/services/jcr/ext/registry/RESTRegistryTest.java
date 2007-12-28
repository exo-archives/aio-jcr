/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.ext.registry;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.MultivaluedMetadata;
import org.exoplatform.services.rest.Request;
import org.exoplatform.services.rest.ResourceBinder;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.ResourceIdentifier;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.container.ResourceDescriptor;


public class RESTRegistryTest extends BaseStandaloneTest{
  
	private ThreadLocalSessionProviderService sessionProviderService;
	
	private static final String SERVICE_XML="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
  "<exo_service xmlns:jcr=\"http://www.jcp.org/jcr/1.0\" jcr:primaryType=\"exo:registryEntry\"/>";

	
	@Override
  public void setUp() throws Exception {

    super.setUp();
    this.sessionProviderService = (ThreadLocalSessionProviderService)
    		container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    sessionProviderService.setSessionProvider(null, new SessionProvider(credentials));
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
    // registry should be empty
    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
    Response response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
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
//    FileInputStream fin = new FileInputStream(file);
    request = new Request(new ByteArrayInputStream(SERVICE_XML.getBytes()), 
    		new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES),
        "POST", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(201, response.getStatus());
    response.writeEntity(System.out);
    System.out.println("\nRESPONSE HEADERS, LOCATION: " + response.getResponseHeaders().getFirst("Location"));

    // request to exo:services/exo_service
    request = new Request(null,
        new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES +
        "/exo_service"), "GET", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
    response.writeEntity(System.out);
    System.out.println();

    // registry
    request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
    response.writeEntity(System.out);
    System.out.println();

    // recreate exo:services/exo_service
//    fin = new FileInputStream(file);
    request = new Request(new ByteArrayInputStream(SERVICE_XML.getBytes()), 
    		new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES),
        "PUT", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(201, response.getStatus());
    System.out.println("RESPONSE HEADERS, LOCATION: " + response.getResponseHeaders().getFirst("Location"));

    // delete exo:services/exo_service
    request = new Request(null, new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES +
        "/exo_service"), "DELETE", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(204, response.getStatus());
    response.writeEntity(System.out);
    System.out.println();
    
    // request to exo:services/exo_service
    // request status should be 404 (NOT_FOUND)
    request = new Request(null, new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES +
    		"/exo_service"), "GET", mv, null);
    response = dispatcher.dispatch(request);
    assertEquals(404, response.getStatus());
    response.writeEntity(System.out);
    System.out.println();
 
  }
  
  public void testCreateGetEntry() throws Exception {
    RESTRegistryService restRegService =
      (RESTRegistryService) container.getComponentInstanceOfType(RESTRegistryService.class);
    ResourceDispatcher dispatcher =
      (ResourceDispatcher) container.getComponentInstanceOfType(ResourceDispatcher.class);
    RegistryEntry testEntry = new RegistryEntry("test");
    String baseURI = "http://localhost:8080/rest";
    String extURI = "/registry/db1/";
    MultivaluedMetadata mv = new MultivaluedMetadata();
    Request request = new Request(null,
        new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES + "/group/test"), "GET", mv, mv);
    Response response = dispatcher.dispatch(request);
    assertEquals(404, response.getStatus());
    response.writeEntity(System.out);
    System.out.println();
    request = new Request(testEntry.getAsInputStream(),
        new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES + "/group/"), "POST", mv, mv);
    response = dispatcher.dispatch(request);
    assertEquals(201, response.getStatus());
    response.writeEntity(System.out);
    System.out.println();
    request = new Request(null,
        new ResourceIdentifier(baseURI, extURI + RegistryService.EXO_SERVICES + "/group/test"), "GET", mv, mv);
    response = dispatcher.dispatch(request);
    assertEquals(200, response.getStatus());
    response.writeEntity(System.out);
//    assertEquals(404, restRegService.getEntry(repository.getName(), RegistryService.EXO_SERVICES+"/group/test").getStatus());
//    Response resp = restRegService.createEntry(testEntry, repository.getName(), RegistryService.EXO_SERVICES+"/group");
//    assertEquals(201, resp.getStatus());
//    resp = restRegService.getEntry(repository.getName(), RegistryService.EXO_SERVICES+"/group/test");
//    assertEquals(200, resp.getStatus());
  	
  }

}
