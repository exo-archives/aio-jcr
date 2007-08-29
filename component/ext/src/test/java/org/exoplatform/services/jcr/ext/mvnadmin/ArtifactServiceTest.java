package org.exoplatform.services.jcr.ext.mvnadmin;

import java.io.File;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;

/*20.08.2007-15:23:16 Volodymyr*/
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
		
		ArtifactBean artifact = new ArtifactBean("amazon", "s3", "0.1",
				jarfile, pomfile);
		asImpl.importArtifact(artifact);

		assertTrue(true);
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
	
	private void printTree(Node parentNode, int space) throws RepositoryException{
		
		if (parentNode.hasNodes()) {
			for (NodeIterator nt = parentNode.getNodes(); nt.hasNext();) {
				Node node = nt.nextNode();
				if(node.isNodeType("exo:artifact"))
					printTree(node, space + 5);
			}
		}else{
			for (int i=0; i < space; i++){
				System.out.print(" ");
			}
			String name = parentNode.getName();
			System.out.println(name);
		}
		
	}
}
