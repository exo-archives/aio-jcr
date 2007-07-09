/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.impl.core.SessionImpl;



public class TestCopyDataVisitor extends JcrImplBaseTest{

  public void setUp() throws Exception {
    super.setUp();
}
  
  public void testCopyInWorkspace() throws Exception 
  {

    Node root = session.getRootNode();
    Node file = root.addNode("testCopy", "nt:folder").addNode("childNode2", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", session.getValueFactory().createValue("this is the content", PropertyType.BINARY));
    contentNode.setProperty("jcr:mimeType", session.getValueFactory().createValue("text/html"));
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));

    root.addNode("existNode", "nt:unstructured").addNode("childNode","nt:unstructured");
    //root.addNode("test", "nt:unstructured");
    session.save();

    workspace.copy("/testCopy", "/testCopy1");
    
    session = (SessionImpl)repository.login(credentials, WORKSPACE);
    assertNotNull(session.getItem("/testCopy1"));
    assertNotNull(session.getItem("/testCopy1/childNode2"));
    assertNotNull(session.getItem("/testCopy1/childNode2/jcr:content"));
    assertNotNull(session.getItem("/testCopy"));

    session.getRootNode().addNode("toCorrupt", "nt:unstructured");
    session.save();
    try {
      workspace.copy("/toCorrupt", "/test/childNode/corrupted");
      fail("exception should have been thrown");
    } catch (PathNotFoundException e) {
    }

    session.getRootNode().getNode("testCopy1").remove();
    session.getRootNode().getNode("toCorrupt").remove();
    session.getRootNode().getNode("existNode").remove();
    session.getRootNode().getNode("testCopy").remove();
    session.save();
    
  }
 
}
