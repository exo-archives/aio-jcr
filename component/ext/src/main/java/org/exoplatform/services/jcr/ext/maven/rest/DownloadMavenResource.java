/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven.rest;

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
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="vkrasnikov@gmail.com">Volodymyr Krasnikov</a>
 * @version $Id: DownloadMavenResource.java 11:14:26
 */

public class DownloadMavenResource {
	private static final Logger LOGGER = Logger
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

		setTestArtifactTypes(); // fake - remove it with real configuration

		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender appender = new ConsoleAppender(layout);
		LOGGER.addAppender(appender);
		LOGGER.setLevel(Level.DEBUG);

	}

	public Response getResponse() throws RepositoryException {
		try{
			if (parse()) {
				long hresult = getContentFromJCR();
				if (hresult != DownloadMavenResource.ARTIFACT_ERROR)
					return doResponse();
			}
		}
		catch(NoSuchArtifactExcetion e){
			
			//!!! Dispatch download artifacts form remote repositories
			
			LOGGER.debug("Cannot resolve artifact, trying downloading from remote", e);
		}
		return Response.Builder.noContent().build();
	}

	private boolean parse() {
		String tail = FilenameUtils.getExtension(mavenQuery);
		boolean op_status = false;
		if (primary.contains(tail)) {
			// we have request like this /maven2/stax/stax/1.2.0/stax-1.2.0.pom
			mainRes = tail;
			servRes = "";
			op_status = true;
			
			isPrimaryResource = true;
		}
		if (secondary.contains(tail)) {
			// we have request like this
			// /maven2/stax/stax/1.2.0/stax-1.2.0.pom.sha1
			servRes = tail;
			String[] strs = mavenQuery.split("\\W"); // split string with non
			// word symbols - dot
			mainRes = strs[strs.length - 2]; // pom or jar or ...
			op_status = true;
			
			isPrimaryResource = false;
		}

		artifact_path = FilenameUtils.getFullPath(mavenQuery); // path to
		// version Node
		int index = mavenQuery.indexOf("/", 1); // skip first slash & name of
		// repository
		artifact_path = mavenQuery.substring(index + 1); // skip first
		// leading slash

		return op_status;
	}

	private Response doResponse() {
		LOGGER.debug("Generating response");
		if( isPrimaryResource ){
			mimeType = "application/java-archive";
			return Response.Builder.ok().contentLenght(contentLength).lastModified(
				lastModified.getTime()).entity(entity, mimeType).transformer(
				new ArtifactOutputTransformer()).build();
		}
		else{
			mimeType = "text/xml";
			return Response.Builder.ok().contentLenght(contentLength).lastModified(
					lastModified.getTime()).entity(entity, mimeType).transformer(
					new StringOutputTransformer()).build();
		}
	}

	public void setPrimaryArtifactTypes(Collection<String> pc) {
		primary.addAll(pc);
	}

	public void setSecondaryArtifactTypes(Collection<String> sc) {
		secondary.addAll(sc);
	}

	private void setTestArtifactTypes() {
		primary.add("jar");
		primary.add("war");
		primary.add("pom");
		primary.add("xml");
		secondary.add("sha1");
		secondary.add("md5");
	}
	private String queryEntity(){
		if(StringUtils.isEmpty(servRes))
			return "jcr:data";
		if(servRes == "sha1")
			return "exo:sha1";
		if(servRes == "md5")
			return "exo:md5";
		return null;
	}
	

	private long getContentFromJCR() throws RepositoryException,
			NoSuchArtifactExcetion {
		
		LOGGER.debug("Getting artifact from :".concat(artifact_path));
		/* stax/stax/1.2.0/ */
		String[] folders = artifact_path.split("/");

		Node current_node = session.getRootNode(); // root node at the start

		// traversal down-run to version node
		for (String folder : folders) {
			if (current_node.hasNode(folder))
				current_node = current_node.getNode(folder);
			else throw new NoSuchArtifactExcetion("There is no such folder is repo: ".concat(folder));
		}
		// I am in version node;
		Node resourceNode = current_node.getNode( mainRes );
		resourceNode = resourceNode.getNode("jcr:content");
		
		entity = resourceNode.getProperty( queryEntity() ).getStream();
		contentLength = resourceNode.getProperty( queryEntity() ).getLength();
		lastModified = resourceNode.getProperty("jcr:lastModified").getDate();

		return DownloadMavenResource.ARTIFACT_SUCCESS;
	}
	
	
	
}
