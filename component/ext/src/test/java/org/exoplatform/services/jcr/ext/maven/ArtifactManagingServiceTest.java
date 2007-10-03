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
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Credentials;
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
import org.exoplatform.services.security.impl.CredentialsImpl;


/**
 * Created by The eXo Platform SARL        .
 * @author Volodymyr Krasnikov
 * @version $Id: ArtifactServiceTest.java 11:16:02
 */

public class ArtifactManagingServiceTest extends BaseStandaloneTest {

	private ArtifactManagingServiceImpl asImpl;
	private String preffix;
	private List<String> list;
	private SessionProvider sp;
	Credentials cred = new CredentialsImpl("exo", "exo".toCharArray());
	
	public void setUp() throws Exception {
		super.setUp();
			
		log.debug("Trying to get ArtifactService  via container!");
						
		list = getDefaultArtifactList();
		
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
	
	public void testArtifactLoad() throws Exception {
		log.info("Starting artifact loading test !");
		asImpl = (ArtifactManagingServiceImpl) container.getComponentInstanceOfType(ArtifactManagingServiceImpl.class);
		assertNotNull(asImpl);
		
		sp = new SessionProvider( cred );
			
		for(Iterator<String> iterator = list.iterator(); iterator.hasNext(); ){
			
			String basename = iterator.next();
						
			InputStream is_jar = new FileInputStream( basename.concat(".jar") );
			InputStream is_pom = new FileInputStream( basename.concat(".pom") );
			
			//this code parse full file name to appropriate structs - groupId, atrifactId, ver ...
	        
	        String str = StringUtils.removeStart(basename, preffix);
	        
	        String els[] = str.split( File.separator );
	        String versionId = els[ els.length-2 ];
	        String artifactId = els[ els.length-3 ];
	        
	        String groupId = "";
	        for(int i=0; i < els.length - 3; i++)
	        	groupId += els[i]+".";
			
			FolderDescriptor fld = new FolderDescriptor(groupId);
			ArtifactDescriptor artifact = new ArtifactDescriptor( fld, artifactId, versionId);
			
			asImpl.addArtifact(sp, artifact, is_jar, is_pom);
						
		}
		sp.close();
	}
		
	public void testExistsContentNodes() throws Exception{

		log.info("Starting artifact reading test !");
		asImpl = (ArtifactManagingServiceImpl) container.getComponentInstanceOfType(ArtifactManagingServiceImpl.class);
		assertNotNull(asImpl);
						
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
		
			String basename = iterator.next();

			String str = "/" + StringUtils.removeStart(basename, preffix);
			
			Node jar = (Node) session.getItem(str.concat(".jar"));
			assertNotNull(jar);

			Node jar_sha1 = (Node) session.getItem(str.concat(".jar.sha1"));
			assertNotNull(jar_sha1);

			Node pom = (Node) session.getItem(str.concat(".pom"));
			assertNotNull(pom);

			Node pom_sha1 = (Node) session.getItem(str.concat(".pom.sha1"));
			assertNotNull(pom_sha1);
			
		}
	}
	
	
	public void testReadContent() throws Exception{
		log.info("Starting artifact reading test !");
		asImpl = (ArtifactManagingServiceImpl) container.getComponentInstanceOfType(ArtifactManagingServiceImpl.class);
		assertNotNull(asImpl);
						
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			
			String basename = iterator.next();
			String str = "/" + StringUtils.removeStart(basename, preffix);
			
			Node content;
			InputStream is = null;
			
			Node jar = (Node) session.getItem( str.concat(".jar") );
			content = jar.getNode("jcr:content");
			is = content.getProperty("jcr:data").getStream();
			assertTrue(is.available() > 0);
			is.close();
			
			Node jar_sha1 = (Node) session.getItem(str.concat(".jar.sha1") );
			content = jar_sha1.getNode("jcr:content");
			is = content.getProperty("jcr:data").getStream();
			assertTrue(is.available() > 0);
			is.close();
			
			Node pom = (Node) session.getItem(str.concat(".pom") );
			content = pom.getNode("jcr:content");
			is = content.getProperty("jcr:data").getStream();
			assertTrue(is.available() > 0);
			is.close();
			
			Node pom_sha1 = (Node) session.getItem(str.concat(".pom.sha1"));
			content = pom_sha1.getNode("jcr:content");
			is = content.getProperty("jcr:data").getStream();
			assertTrue(is.available() > 0);
			is.close();
			
		}

	}
	
		
	private List<String> getAllArtifactList(String repoPath, int limit){
		//!! urgent - must provide crosspaltform usage
		return null;
	}
	 
	private List<String> getDefaultArtifactList(){
		List<String> list = new ArrayList<String>();
		
		URL url = getClass().getResource("/");
		
		preffix = url.getPath();
		
		list.add( preffix + "stax/stax/1.2.0/stax-1.2.0" );
		list.add( preffix + "stax/stax-api/1.0.1/stax-api-1.0.1" );
		
		list.add( preffix + "com/sun/japex/japex/1.0.25/japex-1.0.25" );
						
		return list;
	}
	

	private void printTree(Node parentNode, int space)
			throws RepositoryException {
		for (NodeIterator nt = parentNode.getNodes(); nt.hasNext();) {
			Node node = nt.nextNode();
			if (node.isNodeType("exo:artifact")) {
				for (int i = 0; i < space; i++) {
					System.out.print("-");
				}
				String nodeName = node.getName();

				System.out.print(nodeName.concat("/"));
				
				if ( node.isNodeType("exo:artifactId")) {
					System.out.print(" : ");
					Property list = node.getProperty("exo:versionList");
					Value[] vers = list.getValues();
					for (Value val : vers) {
						String str = val.getString();
						System.out.print(str.concat(" "));
					}
				}
				System.out.print("\n");
				printTree(node, space + 5);
			}

		}

	}
	
}
