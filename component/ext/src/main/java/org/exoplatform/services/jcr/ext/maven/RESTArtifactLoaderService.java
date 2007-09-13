/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.commons.io.FilenameUtils;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Volodymyr Krasnikov
 * @version $Id: RESTArtifactLoaderService.java 11:37:47
 */

@URITemplate("/repomaven2/")
public class RESTArtifactLoaderService implements ResourceContainer {

	private final static Logger LOGGER = Logger
			.getLogger(RESTArtifactLoaderService.class);
	private RepositoryService repositoryService;
	private RegistryService registryService;
	private ResourceDispatcher dispatcher;

	public RESTArtifactLoaderService(RepositoryService repositoryService,
			RegistryService registryServive, ResourceDispatcher dispatcher)
			throws Exception {
		this.registryService = registryServive;
		this.repositoryService = repositoryService;
		this.dispatcher = dispatcher;
		
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender appender = new ConsoleAppender(layout);
		LOGGER.addAppender(appender);
		LOGGER.setLevel(Level.DEBUG);

	}

	@HTTPMethod("GET")
	@URITemplate("/{path}/")
	public Response getResource(@URIParam("path")String artifactPath) {
		
		/* Get file extension. Indeed maven ask for a regular file.
		 * Having extension, service selects a needed resource 
		 * such as pom, jar or sha1 checksum.
		 */ 
		String mvn_resource = FilenameUtils.getExtension(artifactPath);
		Response response = null;
		//Depends on requested resource, different content-type will be generated.
		if(mvn_resource.equals("pom")){
			response = dispatchPom(artifactPath);
		}else if(mvn_resource.equals("sha1")){
			response = dispatchSHA1(artifactPath);
		}else if(mvn_resource.equals("jar")){
			response = dispatchJar(artifactPath);
		}
		 
		return response;
	}
	
	private Response dispatchPom(String artifactPath){
		return null;	
	}
	private Response dispatchJar(String artifactPath){
		return null;
	}
	private Response dispatchSHA1(String artifactPath){
		return null;
	}
}
