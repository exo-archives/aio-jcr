package org.exoplatform.services.jcr.usecases.common;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SAS       .
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AddSameNameNodeTest extends BaseUsecasesTest {

  private Node testRoot = null;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    
    testRoot = session.getRootNode().addNode("AddSameNameNode test");
    session.save();
  }

  @Override
  protected void tearDown() throws Exception {
    
    if (session.getRootNode().hasNode(testRoot.getName())) {
      testRoot.remove();
      session.save();
    }
    
    super.tearDown();
  }
  

  public void testAddSameNameNode() throws Exception {
   
    Node file = testRoot.addNode("file1", "nt:file");
    
    Node content = file.addNode("jcr:content", "nt:unstructured");
    
    content.setProperty("any property", "any content");
    
    testRoot.save();
    
    try {
      assertEquals("Content must be equals",
        testRoot.getProperty("file1/jcr:content/any property").getString(), "any content");
    } catch(PathNotFoundException e) {
      fail(e.getMessage());
    }
    
    try {
      assertFalse("The node shouldn't has mix:versionable", testRoot.getNode("file1").isNodeType("mix:versionable"));
    } catch(PathNotFoundException e) {
      fail(e.getMessage());
    }
    
    // add second node with same name
    
    Node file1 = testRoot.addNode("file1", "nt:file");
    
    Node content1 = file1.addNode("jcr:content", "nt:unstructured");
    
    content1.setProperty("any property", "any content 1");
    
    testRoot.save();
    
    try {
      assertEquals("Content must be equals",
        testRoot.getProperty("file1[2]/jcr:content/any property").getString(), "any content 1");
    } catch(PathNotFoundException e) {
      fail(e.getMessage());
    }
    
    // index 2 mixins
    try {
      assertFalse("The node shouldn't has mix:versionable", testRoot.getNode("file1[2]").isNodeType("mix:versionable"));
    } catch(PathNotFoundException e) {
      fail(e.getMessage());
    }
    
    // index 1 mixins
    try {
      assertFalse("The node shouldn't has mix:versionable", testRoot.getNode("file1").isNodeType("mix:versionable"));
    } catch(PathNotFoundException e) {
      fail(e.getMessage());
    }
   
  }
}
