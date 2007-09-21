/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;


/**
 * Created by The eXo Platform SARL        .
 * @author Volodymyr Krasnikov
 * @version $Id: ArtifactServiceTest.java 11:16:02
 */

public class ArtifactManagingServiceTest extends BaseStandaloneTest {

	private ArtifactManagingServiceImpl asImpl;
	private String repoPrefix = "/home/satay/java/exo-dependencies/repository";
	private int index_j = 0, index_p = 0;
	private int total = 0, suffixLength;
	private List<String> list;
	private SessionProvider sp;

	public void setUp() throws Exception {
		super.setUp();
			
		log.debug("Trying to get ArtifactService  via container!");
				
		list = getDefaultArtifactList();
		
		sp = new SessionProvider(credentials);
	}

	public void testFileExists(){
		
		for(Iterator<String> iterator = list.iterator(); iterator.hasNext(); ){
			String basepath = iterator.next();
			
			File jar = new File(basepath.concat(".jar"));
			assertTrue( jar.exists() );
			
			File pom = new File(basepath.concat(".pom"));
			assertTrue( pom.exists() );
			
		}
		
	}
	
	public void _testArtifactLoad() throws Exception {
		log.debug("Starting artifact loading test !");
		asImpl = (ArtifactManagingServiceImpl) container.getComponentInstanceOfType(ArtifactManagingServiceImpl.class);
		assertNotNull(asImpl);
			
		for(Iterator<String> iterator = list.iterator(); iterator.hasNext(); ){
			
			String basename = iterator.next();
			
			InputStream is_jar = new FileInputStream( basename.concat(".jar") );
			InputStream is_pom = new FileInputStream( basename.concat(".pom") );
			
			//this code parse full file name to appropriate structs - groupId, atrifactId, ver ...
	        String str = FilenameUtils.getFullPath( basename );
	        String els[] = str.split( File.separator );
	        String versionId = els[ els.length-1 ];
	        String artifactId = els[ els.length-2 ];
	        int lastGroupId = str.indexOf(versionId) - artifactId.length() - File.separator.length();
	        String groupId = str.substring(suffixLength + File.separator.length()  , lastGroupId);

			
			FolderDescriptor fld = new FolderDescriptor(groupId);
			ArtifactDescriptor artifact = new ArtifactDescriptor( fld, artifactId, versionId);
			
			asImpl.addArtifact(sp, artifact, is_jar, is_pom);
			
			String resPath;
			
			resPath = StringUtils.removeStart(basename.concat(".jar"), repoPrefix);
			Node jar = (Node) session.getItem(resPath);
			assertNotNull(jar);
			
			resPath = StringUtils.removeStart(basename.concat(".jar.sha1"), repoPrefix);
			Node jar_sha1 = (Node) session.getItem(resPath);
			assertNotNull(jar_sha1);
			
			resPath = StringUtils.removeStart(basename.concat(".pom"), repoPrefix);
			Node pom = (Node) session.getItem(resPath);
			assertNotNull(pom);
			
			resPath = StringUtils.removeStart(basename.concat(".pom.sha1"), repoPrefix);
			Node pom_sha1 = (Node) session.getItem(resPath);
			assertNotNull(pom_sha1);
			
			
		}
	
	}
	
	public void testArtifactRead() throws Exception{
		assertTrue(true);
	}
	public void testArtifactLoadHuge() throws Exception{
		assertTrue(true);
	}
	public void testArtifactReadHuge() throws Exception{
		assertTrue(true);
	}
	
	private List<String> getAllArtifactList(String repoPath, int limit){
		//!! urgent - must provide crosspaltform usage
		return null;
	}
	 
	private List<String> getDefaultArtifactList(){
		List<String> list = new ArrayList<String>();
				
		suffixLength = repoPrefix.length();
		
		list.add( repoPrefix + "/stax/stax/1.2.0/stax-1.2.0");
		list.add( repoPrefix + "/stax/stax-api/1.0.1/stax-api-1.0.1");
		
		total = list.size();
		
		return list;
	}
	
}
