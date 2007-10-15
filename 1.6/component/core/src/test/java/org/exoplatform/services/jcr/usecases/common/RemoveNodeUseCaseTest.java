/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.usecases.common;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: RemoveNodeUseCaseTest.java 12841 2007-02-16 08:58:38Z peterit $
 * 
 * 
 */

public class RemoveNodeUseCaseTest extends BaseUsecasesTest {

 
  /**
   * [BM] so looks like that when I have
   *  Parent/
   *       child1/
   *             prop1
   *       child2/
   * If I remove child2, then it can not get the prop1 anymore (that is  
   * probably on the same session object)
   * @throws Exception
   */
  public void testIfPropertyFromSiblingReachableAfterRemove() throws Exception {
    // make sub-root with unique name;
    Node subRootNode = root.addNode("testIfPropertyFromSiblingReachableAfterRemove");
    
    Node child1 = subRootNode.addNode("child1");
    child1.setProperty("prop1", "test");
    Node child2 = subRootNode.addNode("child2");
    
    child2.remove();
    
    session.save();
    
    // and test on current session 
    assertNotNull(child1.getProperty("prop1"));
    
    // test on another session
    Session session2 = repository.login(new SimpleCredentials("admin", "admin".toCharArray()), workspace.getName());
    
    child1 = session2.getRootNode().getNode("testIfPropertyFromSiblingReachableAfterRemove/child1");
    // there should be 2 child props: jcr:primaryType and prop1

    assertEquals(2, child1.getProperties().getSize());
    assertNotNull(child1.getProperty("prop1"));

    // clean
    subRootNode.remove();
    session.save();
    
  }



}
