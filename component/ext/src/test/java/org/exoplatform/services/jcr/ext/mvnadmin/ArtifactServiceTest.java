/**
 * Copyright 2001-2007 The eXo Platform SASL   All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */
package org.exoplatform.services.jcr.ext.mvnadmin;

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

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.mvnadmin.service.ArtifactImporter;

/**
 * Created by The eXo Platform SARL        .
 * @author Volodymyr Krasnikov
 * @version $Id: ArtifactServiceTest.java 11:16:02
 */

public class ArtifactServiceTest extends BaseStandaloneTest {

	private ArtifactServiceImpl asImpl;

	public void setUp() throws Exception {
		super.setUp();

		asImpl = (ArtifactServiceImpl) container
				.getComponentInstanceOfType(ArtifactServiceImpl.class);
	}

	public void testImportArtifact() throws Exception {
		File jarfile = new File("/home/satay/java/tmp/s3-0.1.jar");
		File pomfile = new File("/home/satay/java/tmp/s3-0.1.pom");

		System.out.println("Starting Importing to Repository !!!");

		ArtifactBean artifact0 = new ArtifactBean("amazon", "s3", "0.1",
				jarfile, pomfile);
		ArtifactBean artifact1 = new ArtifactBean("amazon", "s3", "0.2",
				jarfile, pomfile);

		ArtifactBean artifact2 = new ArtifactBean("org.apache.struts",
				"searcher", "1.4", jarfile, pomfile);
		ArtifactBean artifact3 = new ArtifactBean(
				"org.maven.artifact.myapp.tester", "superApp", "2.4", jarfile,
				pomfile);
		ArtifactBean artifact4 = new ArtifactBean("com.oracle.db.first.test",
				"yast", "1.1", jarfile, pomfile);

		asImpl.importArtifact(artifact0);
		asImpl.importArtifact(artifact1);
		asImpl.importArtifact(artifact2);
		asImpl.importArtifact(artifact3);
		asImpl.importArtifact(artifact4);

		assertTrue(true);
	}

	private void printMetadata(Node parentNode) throws RepositoryException {
		for (NodeIterator nt = parentNode.getNodes(); nt.hasNext();) {
			Node node = nt.nextNode();
			if (node.isNodeType("exo:artifact")) {
				if (node.getProperty("exo:pathType").getLong() == ArtifactImporter.ARTIFACT_ID_TYPE) {
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

	public void testRepositoryBrowse() throws Exception {
		// only for confirm correct using of JCR !!!

		System.out.println("Starting Browsing Repository !!!");

		Session session = asImpl.getSession();

		assertTrue(session != null);

		Node root = session.getRootNode();
		root = root.getNode("ExoArtifactRepository");

		assertTrue(root.isNodeType("exo:artifact"));
		assertTrue(root.hasNodes());

		printTree(root, 0);

	}

	public void testGetMetadata() throws Exception {

		System.out.println("Starting Getting metadata\n\n");

		Session session = asImpl.getSession();
		assertTrue(session != null);
		Node root = session.getRootNode();
		root = root.getNode("ExoArtifactRepository");

		printMetadata(root);

		System.out.println("\n\n");
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

				Property prop = node.getProperty("exo:pathType");

				if (prop.getLong() == ArtifactImporter.ARTIFACT_ID_TYPE) {
					System.out.print(" : ");
					Property list = node.getProperty("exo:versionList");
					Value[] vers = list.getValues();
					for (Value val : vers) {
						String str = val.getString();
						System.out.print(str.concat(" "));
					}

				}

				if (prop.getLong() == ArtifactImporter.VERSION_ID_TYPE) {

				}

				System.out.print("\n");

				printTree(node, space + 5);
			}

		}

	}
}
