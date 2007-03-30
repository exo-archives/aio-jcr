/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.replication;

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SARL
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.org.ua
 *          reshetnyak.alex@gmail.com		
 * 28.02.2007 10:59:36 
 * @version $Id: TestReplicationCopyNode.java 28.02.2007 10:59:36 rainfox 
 */
public class TestReplicationCopyMoveNode extends BaseReplicationTest{
  
  public void testSessionMove() throws Exception {
    
    Node file = root.addNode("testSessionMove", "nt:folder").addNode("childNode2", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", "this is the content");
    contentNode.setProperty("jcr:mimeType", "text/html");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));

    session.save();

    session.move("/testSessionMove", "/testSessionMove1");
    
    session.save();
        
    assertNotNull(session.getItem("/testSessionMove1"));
    assertNotNull(session.getItem("/testSessionMove1/childNode2/jcr:content"));

    Thread.sleep(5*1000);
    
    // COMPARE REPLICATION DATA
    assertNotNull(session2.getItem("/testSessionMove1"));
    assertNotNull(session2.getItem("/testSessionMove1/childNode2/jcr:content"));
    
    Node srcNode = root.getNode("testSessionMove1").getNode("childNode2").getNode("jcr:content");
    Node destNode = root2.getNode("testSessionMove1").getNode("childNode2").getNode("jcr:content");
    
    assertEquals(srcNode.getProperty("jcr:data").getString(), destNode.getProperty("jcr:data").getString());
    assertEquals(srcNode.getProperty("jcr:mimeType").getString(), destNode.getProperty("jcr:mimeType").getString());
    assertEquals(srcNode.getProperty("jcr:lastModified").getString(), destNode.getProperty("jcr:lastModified").getString());
  }
  
  public void testCopy() throws Exception {

    Node file = root.addNode("testCopy", "nt:folder").addNode("childNode2", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", "this is the content");
    contentNode.setProperty("jcr:mimeType", "text/html");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));

    session.save();

    workspace.copy("/testCopy", "/testCopy1");
    
    Thread.sleep(5*1000);

    // COMPARE REPLICATION DATA
    assertNotNull(session2.getItem("/testCopy1"));
    assertNotNull(session2.getItem("/testCopy1/childNode2"));
    assertNotNull(session2.getItem("/testCopy1/childNode2/jcr:content"));
    assertNotNull(session2.getItem("/testCopy"));
    
    Node srcNode = root.getNode("testCopy1").getNode("childNode2").getNode("jcr:content");
    Node destNode = root2.getNode("testCopy1").getNode("childNode2").getNode("jcr:content");
    
    assertEquals(srcNode.getProperty("jcr:data").getString(), destNode.getProperty("jcr:data").getString());
    assertEquals(srcNode.getProperty("jcr:mimeType").getString(), destNode.getProperty("jcr:mimeType").getString());
    assertEquals(srcNode.getProperty("jcr:lastModified").getString(), destNode.getProperty("jcr:lastModified").getString());
  }
  
  public void testMove() throws Exception {
    
    Node file = root.addNode("testMove", "nt:folder").addNode("childNode2", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", "this is the content");
    contentNode.setProperty("jcr:mimeType", "text/html");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(Calendar.getInstance()));
   
    session.save();

    workspace.move("/testMove", "/testMove1");

    Thread.sleep(5*1000);
    
    // COMPARE REPLICATION DATA
    assertNotNull(session2.getItem("/testMove1"));
    assertNotNull(session2.getItem("/testMove1/childNode2"));
    assertNotNull(session2.getItem("/testMove1/childNode2/jcr:content"));
    
    Node srcNode = root.getNode("testMove1").getNode("childNode2").getNode("jcr:content");
    Node destNode = root2.getNode("testMove1").getNode("childNode2").getNode("jcr:content");
    
    assertEquals(srcNode.getProperty("jcr:data").getString(), destNode.getProperty("jcr:data").getString());
    assertEquals(srcNode.getProperty("jcr:mimeType").getString(), destNode.getProperty("jcr:mimeType").getString());
    assertEquals(srcNode.getProperty("jcr:lastModified").getString(), destNode.getProperty("jcr:lastModified").getString());
  }
  
}
