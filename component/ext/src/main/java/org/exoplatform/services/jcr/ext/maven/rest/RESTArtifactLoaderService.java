/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven.rest;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.codehaus.plexus.util.StringUtils;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Volodymyr Krasnikov
 * @version $Id: RESTArtifactLoaderService.java 11:37:47
 */

@URITemplate("/repomaven2/")
public class RESTArtifactLoaderService implements ResourceContainer{
	private static final String NT_FILE = "artifact-nodetypes.xml";
	private final static Log LOGGER = ExoLogger.getLogger(RESTArtifactLoaderService.class);
	//private RepositoryService repositoryService;
	private ThreadLocalSessionProviderService sessionProviderService;
	private RepositoryService repoService;
	private SessionProvider sessionProvider;
		
	private String repoWorkspaceName = "ws";
	private String repoPath;
	
	public RESTArtifactLoaderService(RepositoryService repoService,
			ThreadLocalSessionProviderService sessionProviderService)
			throws Exception {
		
		this.repoService = repoService;
		this.sessionProviderService = sessionProviderService;
		
		sessionProvider = sessionProviderService.getSessionProvider(null);
		
	}
	
	@HTTPMethod("GET")
	@URITemplate("/{path}/")
	public Response getResource(@URIParam("path")String mavenQuery) throws RepositoryException {
		//annotated methods are used as front dispatcher. 
		LOGGER.debug("getResource: ".concat(mavenQuery));
		return new DownloadMavenResource(currentSession(sessionProvider), mavenQuery).getResponse();
	}
	private Session currentSession(SessionProvider sp)
			throws RepositoryException {
		return sp.getSession(repoWorkspaceName, repoService.getCurrentRepository());
	}
		
}
