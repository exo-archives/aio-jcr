/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.maven.rest.RESTArtifactLoaderService;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.rest.MultivaluedMetadata;
import org.exoplatform.services.rest.Request;
import org.exoplatform.services.rest.ResourceBinder;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.ResourceIdentifier;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="vkrasnikov@gmail.com">Volodymyr Krasnikov</a>
 * @version $Id: RESTArtifactLoader.java 09:56:01
 */

public class RESTArtifactLoaderTest extends BaseStandaloneTest{
	
	private RESTArtifactLoaderService RESTLoader;
	private ThreadLocalSessionProviderService threadLocalSessionProviderService;
	Credentials cred = new CredentialsImpl("exo", "exo".toCharArray());
	SessionProvider sp = new SessionProvider(cred);
	
	public RESTArtifactLoaderTest(){
				
	}
	@Override
	public void setUp() throws Exception {
		
		super.setUp();
		
		// set thread local SessionProvider variable, that it used in RESTArtifactLoader service 
		threadLocalSessionProviderService = (ThreadLocalSessionProviderService)container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
		threadLocalSessionProviderService.setSessionProvider(null, sp );
		
		
	}
	
	public void testInitRepositoryService() throws Exception{
		ManageableRepository rep = null;
		try {
			rep = ((RepositoryService) container.getComponentInstanceOfType(RepositoryService.class))
					.getDefaultRepository();
		} catch (RepositoryException e) {
			fail("Repository Exception");
		} catch (RepositoryConfigurationException e) {
			fail("Repository Configuration Exception");
		}
		assertNotNull(rep);
		
		SessionProvider sp2 = threadLocalSessionProviderService.getSessionProvider(null);
		
		assertTrue(sp.equals(sp2) );
		
       
	}
	
	public void testRESTArtifactLoader_getPOM() throws Exception{
		
		RESTLoader = (RESTArtifactLoaderService) container
				.getComponentInstanceOfType(RESTArtifactLoaderService.class);
		ResourceBinder binder = (ResourceBinder) container
				.getComponentInstanceOfType(ResourceBinder.class);
		ResourceDispatcher dispatcher = (ResourceDispatcher) container
				.getComponentInstanceOfType(ResourceDispatcher.class);
		assertNotNull(RESTLoader);
		assertNotNull(binder);
		assertNotNull(dispatcher);
		
		MultivaluedMetadata mv = new MultivaluedMetadata();
		String baseURI = "http://localhost:8080/rest";
	    String extURI = "/maven2/stax/stax/1.2.0/stax-1.2.0.pom/";
	    
	    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
	    Response response = dispatcher.dispatch(request);
	    assertEquals(200, response.getStatus());
		
	}
	
	public void testRESTArtifactLoader_getPOM_SHA1() throws Exception{
		
		RESTLoader = (RESTArtifactLoaderService) container
				.getComponentInstanceOfType(RESTArtifactLoaderService.class);
		ResourceBinder binder = (ResourceBinder) container
				.getComponentInstanceOfType(ResourceBinder.class);
		ResourceDispatcher dispatcher = (ResourceDispatcher) container
				.getComponentInstanceOfType(ResourceDispatcher.class);
		assertNotNull(RESTLoader);
		assertNotNull(binder);
		assertNotNull(dispatcher);
		
		String baseURI = "http://localhost:8080/rest";
	    String extURI = "/maven2/stax/stax/1.2.0/stax-1.2.0.pom.sha1";
	    
	    MultivaluedMetadata mv = new MultivaluedMetadata();
	    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
	    Response response = dispatcher.dispatch(request);
	    assertEquals(200, response.getStatus());
		
	}
	
	public void testRESTArtifactLoader_getPOM2() throws Exception{
		
		RESTLoader = (RESTArtifactLoaderService) container
				.getComponentInstanceOfType(RESTArtifactLoaderService.class);
		ResourceBinder binder = (ResourceBinder) container
				.getComponentInstanceOfType(ResourceBinder.class);
		ResourceDispatcher dispatcher = (ResourceDispatcher) container
				.getComponentInstanceOfType(ResourceDispatcher.class);
		assertNotNull(RESTLoader);
		assertNotNull(binder);
		assertNotNull(dispatcher);
		
		String baseURI = "http://localhost:8080/rest";
	    String extURI = "/maven2/stax/stax-api/1.0.1/stax-api-1.0.1.pom";
	    
	    MultivaluedMetadata mv = new MultivaluedMetadata();
	    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
	    Response response = dispatcher.dispatch(request);
	    assertEquals(200, response.getStatus());
		
	}
	
	public void testRESTArtifactLoader_getPOM2_SHA1() throws Exception{
		
		RESTLoader = (RESTArtifactLoaderService) container
				.getComponentInstanceOfType(RESTArtifactLoaderService.class);
		ResourceBinder binder = (ResourceBinder) container
				.getComponentInstanceOfType(ResourceBinder.class);
		ResourceDispatcher dispatcher = (ResourceDispatcher) container
				.getComponentInstanceOfType(ResourceDispatcher.class);
		assertNotNull(RESTLoader);
		assertNotNull(binder);
		assertNotNull(dispatcher);
		
		String baseURI = "http://localhost:8080/rest";
	    String extURI = "/maven2/stax/stax-api/1.0.1/stax-api-1.0.1.pom.sha1";
	    
	    MultivaluedMetadata mv = new MultivaluedMetadata();
	    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
	    Response response = dispatcher.dispatch(request);
	    assertEquals(200, response.getStatus());
		
	}
	
	public void testRESTArtifactLoader_getPOM2_JAR2() throws Exception{
		
		RESTLoader = (RESTArtifactLoaderService) container
				.getComponentInstanceOfType(RESTArtifactLoaderService.class);
		ResourceBinder binder = (ResourceBinder) container
				.getComponentInstanceOfType(ResourceBinder.class);
		ResourceDispatcher dispatcher = (ResourceDispatcher) container
				.getComponentInstanceOfType(ResourceDispatcher.class);
		assertNotNull(RESTLoader);
		assertNotNull(binder);
		assertNotNull(dispatcher);
		
		String baseURI = "http://localhost:8080/rest";
	    String extURI = "/maven2/stax/stax-api/1.0.1/stax-api-1.0.1.jar";
	    
	    MultivaluedMetadata mv = new MultivaluedMetadata();
	    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
	    Response response = dispatcher.dispatch(request);
	    assertEquals(200, response.getStatus());
		
	}
	
	public void testRESTArtifactLoader_getPOM2_JAR2_SHA1() throws Exception{
		
		RESTLoader = (RESTArtifactLoaderService) container
				.getComponentInstanceOfType(RESTArtifactLoaderService.class);
		ResourceBinder binder = (ResourceBinder) container
				.getComponentInstanceOfType(ResourceBinder.class);
		ResourceDispatcher dispatcher = (ResourceDispatcher) container
				.getComponentInstanceOfType(ResourceDispatcher.class);
		assertNotNull(RESTLoader);
		assertNotNull(binder);
		assertNotNull(dispatcher);
		
		String baseURI = "http://localhost:8080/rest";
	    String extURI = "/maven2/stax/stax-api/1.0.1/stax-api-1.0.1.jar.sha1";
	    
	    MultivaluedMetadata mv = new MultivaluedMetadata();
	    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
	    Response response = dispatcher.dispatch(request);
	    assertEquals(200, response.getStatus());
		
	}
	
	public void testRESTArtifactLoader_getPOM2_JAR() throws Exception{
		
		RESTLoader = (RESTArtifactLoaderService) container
				.getComponentInstanceOfType(RESTArtifactLoaderService.class);
		ResourceBinder binder = (ResourceBinder) container
				.getComponentInstanceOfType(ResourceBinder.class);
		ResourceDispatcher dispatcher = (ResourceDispatcher) container
				.getComponentInstanceOfType(ResourceDispatcher.class);
		assertNotNull(RESTLoader);
		assertNotNull(binder);
		assertNotNull(dispatcher);
		
		String baseURI = "http://localhost:8080/rest";
	    String extURI = "/maven2/stax/stax/1.2.0/stax-1.2.0.jar";
	    
	    MultivaluedMetadata mv = new MultivaluedMetadata();
	    Request request = new Request(null, new ResourceIdentifier(baseURI, extURI), "GET", mv, null);
	    Response response = dispatcher.dispatch(request);
	    assertEquals(200, response.getStatus());
		
	}
	
	
	
	

}

