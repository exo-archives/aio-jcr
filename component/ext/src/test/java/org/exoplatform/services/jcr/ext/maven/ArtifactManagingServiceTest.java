/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.maven;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * @author Volodymyr Krasnikov
 * @version $Id: ArtifactServiceTest.java 11:16:02
 */

public class ArtifactManagingServiceTest extends BaseStandaloneTest {

	private ArtifactManagingServiceImpl asImpl;
	private static Log LOGGER = ExoLogger.getLogger(ArtifactManagingServiceTest.class);

	public void setUp() throws Exception {
		super.setUp();
			
		LOGGER.debug("Trying to get ArtifactService  via container!");
		asImpl = (ArtifactManagingServiceImpl) container.getComponentInstanceOfType(ArtifactManagingServiceImpl.class);
	}

	public void testImportArtifact() throws Exception {
		LOGGER.debug("Starting first test!");
		
		File jarfile = new File("/home/satay/java/tmp/s3-0.1.jar");
		File pomfile = new File("/home/satay/java/tmp/s3-0.1.pom");

		
		assertTrue(true);
	}

	private void printMetadata(Node parentNode) throws RepositoryException {
		for (NodeIterator nt = parentNode.getNodes(); nt.hasNext();) {
			Node node = nt.nextNode();
			if (node.isNodeType("exo:artifact")) {
				if (node.isNodeType("exo:artifactId")) {
					printXML(node);
				} else
					printMetadata(node);
			}
		}
	}

	private void printXML(Node node) throws RepositoryException {
		System.out.println();
		Node data_node = node.getNode("maven-metadata.xml/jcr:content");
		Property data = data_node.getProperty("jcr:data");
		InputStream is = data.getStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try {
			try {
				while (reader.ready()) {
					System.out.println(reader.readLine());
				}
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println();
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

				if (node.isNodeType("exo:versionId")) {

				}

				System.out.print("\n");

				printTree(node, space + 5);
			}

		}

	}
}
