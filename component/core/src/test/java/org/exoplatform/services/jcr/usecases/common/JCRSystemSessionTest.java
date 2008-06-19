package org.exoplatform.services.jcr.usecases.common;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

public class JCRSystemSessionTest extends BaseUsecasesTest{
	
  protected void tearDown() throws Exception {
    
    // [PN] BaseUsecasesTest.tearDown() don't touch jcr:system descendants
    try {
      session.refresh(false);
      Node jcrSystem = session.getRootNode().getNode("jcr:system") ;
      jcrSystem.getNode("Node1").remove();
      session.save();
    } catch(RepositoryException e) {
      log.error("Error of tearDown " + e.getMessage());
    }
    
    super.tearDown();
  }
  
	public void testActionsOnJcrSystem() throws Exception {
            String workspaceName = repository.getSystemWorkspaceName() ;
            //---------------use System sestion
            Session session2 = repository.getSystemSession(workspaceName) ;
            //----------- Use addmin session 
            Session session= repository.login(new SimpleCredentials("admin", "admin".toCharArray()), workspaceName);
            Node node1 = session.getRootNode().addNode("Node1");
            session.save() ;
            //refresh session2 (session2 is systemSession )
            //session2.refresh(true) ;
            assertNotNull(session.getRootNode().getNode("Node1")) ;
            assertNotNull(session2.getRootNode().getNode("Node1")) ;
	}	
}
