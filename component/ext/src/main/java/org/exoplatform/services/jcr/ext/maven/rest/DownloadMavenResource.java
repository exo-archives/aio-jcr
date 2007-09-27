/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="vkrasnikov@gmail.com">Volodymyr Krasnikov</a>
 * @version $Id: DownloadMavenResource.java 11:14:26
 */

public class DownloadMavenResource {
	private static final Log LOGGER = ExoLogger
			.getLogger(DownloadMavenResource.class);
	private static final long ARTIFACT_ERROR = 0;
	private static final long ARTIFACT_SUCCESS = 1;
	private Session session;
	private String mavenQuery;
	private String artifact_path;
	private Set<String> primary = new HashSet<String>();
	private Set<String> secondary = new HashSet<String>();
	private String mainRes, servRes; // prime artifact, sec artifact
	private long contentLength;
	private Calendar lastModified;
	private InputStream entity;
	private String mimeType;
	private boolean isPrimaryResource;

	public DownloadMavenResource(Session session, String mavenQuery) {
		this.mavenQuery = mavenQuery;
		this.session = session;
	}

	public Response getResponse() {
		try {
			getContentFromJCR();
			return doResponse();
		}
		catch(RepositoryException e){
			//!!! Dispatch download artifacts form remote repositories
			LOGGER.debug("Cannot resolve artifact, trying downloading from remote", e);
		}
		return Response.Builder.noContent().build();
	}

	private Response doResponse() {
		LOGGER.debug("Generating response");
		
		// because of I have not a correct JCR struct in repository - I use ftp uploading
		// without setting of correct properties such jcr:mimeType - it use default application/octet-stream
		//
		/*
		String jarResource = "application/java-archive";
		Response response = null;
		if( mimeType.equals(jarResource) ){
			response = Response.Builder.ok().contentLenght(contentLength).lastModified(
				lastModified.getTime()).entity(entity, mimeType).transformer(
				new ArtifactOutputTransformer()).build();
		}
		else{
			response = Response.Builder.ok().contentLenght(contentLength).lastModified(
					lastModified.getTime()).entity(entity, mimeType).transformer(
					new XMLOutputTransformer()).build();
		}
		return response;
		*/
		
		String tail = FilenameUtils.getExtension(mavenQuery);
		if(tail.equals("pom"))
			mimeType = "text/xml";
		else if(tail.equals("jar")) 
			mimeType = "application/java-archive";
		else Response.Builder.notFound().build();
		
		Response response = Response.Builder.ok().contentLenght(contentLength)
				.lastModified(lastModified.getTime()).entity(entity, mimeType)
				.transformer(new PassthroughOutputTransformer()).build();
		
		return response;
	}
	
	private void getContentFromJCR() throws RepositoryException{
				
		Node resourceNode = (Node)session.getItem( mavenQuery );
		Node content = resourceNode.getNode("jcr:content");
		
		assert content != null;
		
		entity = content.getProperty("jcr:data").getStream();
		
		try{
			contentLength = entity.available();
			
			System.out.println( new Long(contentLength).toString() );
			LOGGER.info( new Integer(entity.available()).toString() );
		}
		catch(IOException e){
			LOGGER.error("Cant get access to jcr:data", e);
		}
		
		lastModified = content.getProperty("jcr:lastModified").getDate();
		mimeType = content.getProperty("jcr:mimeType").getString();
		
	}
	
}
