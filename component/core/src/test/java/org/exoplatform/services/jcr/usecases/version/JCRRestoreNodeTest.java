package org.exoplatform.services.jcr.usecases.version;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL
 * 
 * @version $Id: JCRRestoreNodeTest.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class JCRRestoreNodeTest extends BaseUsecasesTest{
  
  public void testRestoredNodeExists() throws Exception {
    
    Node node1 = root.addNode("Node1","nt:unstructured");
    node1.addMixin("mix:versionable");
    root.save();
    Version ver1 = node1.checkin(); 
    node1.checkout();
    Version ver2 = node1.checkin();
    node1.checkout();
    Version ver3 = node1.checkin();
    node1.checkout();
    //session.save(); // unnecessary here
    Node node2 = session.getRootNode().addNode("Node2","nt:unstructured");
    session.save();
    assertNotNull(session.getRootNode().getNode("Node1"));
    node1.restore(ver2, true);
    assertNotNull(session.getRootNode().getNode("Node1"));
  }	
}
