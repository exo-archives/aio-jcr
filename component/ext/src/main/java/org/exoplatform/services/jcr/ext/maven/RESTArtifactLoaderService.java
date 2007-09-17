/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven;

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
import org.codehaus.plexus.util.StringUtils;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
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
public class RESTArtifactLoaderService implements ResourceContainer, Startable {
	private static final String NT_FILE = "artifact-nodetypes.xml";
	private final static Logger LOGGER = Logger
			.getLogger(RESTArtifactLoaderService.class);
	private RepositoryService repositoryService;
	private RegistryService registryService;
	private SessionProvider sessionProvider;
	private ResourceDispatcher dispatcher;
	private String repoWorkspaceName;
	private String repoPath;
	
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
	public void start() {
		// TODO Auto-generated method stub
		sessionProvider = SessionProvider.createSystemProvider();
		InputStream xml = this.getClass().getResourceAsStream(NT_FILE);
		
		try {
			ManageableRepository rep = repositoryService.getCurrentRepository();
			
			rep.getNodeTypeManager().registerNodeTypes(xml,
					ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
			
			registryService.getEntry(sessionProvider,
					RegistryService.EXO_SERVICES, "ArtifactManaging");
			
		} catch (RepositoryException e) {
			LOGGER.error("FAILURE while starting RESTArtifactLoaderService", e);
		}
		finally{
			//sessionProvider.close();
		}
		
	}

	public void stop() {
		// TODO Auto-generated method stub
		
	}
	private Session currentSession(SessionProvider sp) throws RepositoryException {
		return (sp != null)?sp.getSession(repoWorkspaceName, repositoryService.getCurrentRepository()):
				sessionProvider.getSession(repoWorkspaceName, repositoryService.getCurrentRepository());
	}


	@HTTPMethod("GET")
	@URITemplate("/{path}/")
	public Response getResource(@URIParam("path")String mavenQuery) throws RepositoryException {
		
		/* Get file extension. Indeed maven ask for a regular file.
		 * Having extension, service selects a needed resource 
		 * such as pom, jar or sha1 checksum.
		 */ 
		LOGGER.debug("getResource: ".concat(mavenQuery));
		
		String mvn_resource = FilenameUtils.getExtension(mavenQuery);
		Response response = null;
		//Depends on requested resource, different content-type will be generated.
		if( mvn_resource.equals("pom") || mvn_resource.equals("xml") || mvn_resource.equals("sha1") ){
			response = getMavenResource(mavenQuery,"text/xml");
		}else if(mvn_resource.equals("jar")){
			response = getMavenResource(mavenQuery,"application/java-archive");
		}
		else return Response.Builder.notFound().build();
		 
		return response;
	}
	
	private Response getMavenResource(String mavenQuery, String mimeType) throws RepositoryException{
		LOGGER.debug("Generating response to maven query");
		MavenResourceBean resource = getMavenMavenEntity(null, mavenQuery);
		long contentLength = resource.getContentLength();
		Date date = resource.getLastModified().getTime();
		
		return Response.Builder.ok().contentLenght(contentLength).lastModified(date).entity(
				resource.getInputStream(), mimeType).transformer(
				new ArtifactOutputTransformer()).build();	
	}
		
	private MavenResourceBean getMavenMavenEntity(SessionProvider sp, String mavenQuery) throws RepositoryException{
		LOGGER.debug("Getting resource queried my maven client");		
		Session session = currentSession(sp);
		
		ArtifactEntity af = new ArtifactEntity(mavenQuery);
		
		Node version = (Node)session.getItem(af.getPath());
		Node dataNode = version.getNode(af.getPrimary().concat("/jcr:content") );
		
		InputStream in = dataNode.getProperty(af.queryResource()).getStream();
		long lenght = dataNode.getProperty(af.queryResource()).getLength();
		Calendar calendar = dataNode.getProperty("jcr:lastModified").getDate();
		
		return new MavenResourceBean(in, lenght, calendar);
	}
	private class ArtifactEntity{
		private String primary, secondary, path;
		public ArtifactEntity(String query){
			String ends = FilenameUtils.getExtension(query);	
			if( (ends == "jar")||(ends == "war") ){
				primary = ends;
				secondary = "";
			}
			if((ends == "sha1")||(ends == "md5")){
				secondary = ends;
				String[] strs = query.split("\\W");	//split string with non word symbols - dot
				primary = strs[strs.length - 2];
			}
			path = FilenameUtils.getFullPath(query);	//path to version Node
			int index = query.indexOf("/",1);	//skip first slash & name of repository
			path = query.substring(index);
		}
		public String getPrimary(){
			return primary;
		}
		public String getSecondary(){
			return secondary;
		}
		public String queryResource(){
			if(StringUtils.isEmpty(secondary))
				return "jcr:data";
			if(secondary.equals("sha1"))
				return "exo:sha1";
			if(secondary.equals("md5"))
				return "exo:md5";
			return null;
		}
		public String getPath(){
			return path;
		}
	}
	
}
