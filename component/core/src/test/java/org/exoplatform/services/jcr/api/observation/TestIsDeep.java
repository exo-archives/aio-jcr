/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.api.observation;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventListenerIterator;

import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * Created by The eXo Platform SARL
 * 10.05.2006
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestIsDeep.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestIsDeep extends JcrAPIBaseTest {

  private Node testObservation;
  
  public void setUp() throws Exception {
    super.setUp();

    testObservation = root.addNode("testObservation");
    root.save();
 }
 
 public void tearDown() throws Exception {
   EventListenerIterator listeners = this.workspace.getObservationManager().getRegisteredEventListeners();
   while(listeners.hasNext()) {
     this.workspace.getObservationManager().removeEventListener(listeners.nextEventListener()); 
   }
   testObservation.remove();
   root.save();
   
   super.tearDown();
 }
  
  public void testIsDeepFalseNodeAdd() throws RepositoryException {
    Integer counter = 0;
    SimpleListener listener = new SimpleListener("IsDeepFalseNodeAdd", log, counter);
    
    workspace.getObservationManager().addEventListener(listener, 
        Event.NODE_ADDED, testObservation.getPath() + "/n1", false, null, null, false);
    
    Node n1 = testObservation.addNode("n1"); // /testObservation/n1
    Node n1n2 = n1.addNode("n2"); // /testObservation/n1/n2
    testObservation.save();
    
    assertTrue("A events count expected 1. Was: " + listener.getCounter(), listener.getCounter() == 1);
    
    checkItemsExisted(new String[] {n1.getPath(), n1n2.getPath()}, null);
  }
  
  public void testIsDeepTrueNodeAdd() throws RepositoryException {
    Integer counter = 0;
    SimpleListener listener = new SimpleListener("IsDeepTrueNodeAdd", log, counter);
    
    workspace.getObservationManager().addEventListener(listener, 
        Event.NODE_ADDED, testObservation.getPath() + "/n1", true, null, null, false);
    
    Node n1 = testObservation.addNode("n1"); // /testObservation/n1
    Node n1n2 = n1.addNode("n2"); // /testObservation/n1/n2
    testObservation.save();
    
    assertTrue("A events count expected 2. Was: " + listener.getCounter(), listener.getCounter() == 2);
    
    checkItemsExisted(new String[] {n1.getPath(), n1n2.getPath()}, null);
  }
  
  public void testIsDeepFalseNodeRemove() throws RepositoryException {
    Integer counter = 0;
    SimpleListener listener = new SimpleListener("IsDeepFalseNodeRemove", log, counter);
    
    workspace.getObservationManager().addEventListener(listener, 
        Event.NODE_REMOVED, testObservation.getPath() + "/n1", false, null, null, false);
    
    Node n1 = testObservation.addNode("n1"); // /testObservation/n1
    Node n1n2 = n1.addNode("n2"); // /testObservation/n1/n2
    testObservation.save();
    
    n1n2.remove();
    testObservation.save();    
    assertTrue("A events count expected 0. Was: " + listener.getCounter(), listener.getCounter() == 0);    
    checkItemsExisted(new String[] {n1.getPath()}, new String[] {n1n2.getPath()});
    
    n1.remove();
    testObservation.save();
    assertTrue("A events count expected 1. Was: " + listener.getCounter(), listener.getCounter() == 1);    
    checkItemsExisted(null, new String[] {n1.getPath(), n1n2.getPath()});    
  }  
  
  public void testIsDeepTrueNodeRemove() throws RepositoryException {
    Integer counter = 0;
    SimpleListener listener = new SimpleListener("IsDeepTrueNodeRemove", log, counter);
    
    workspace.getObservationManager().addEventListener(listener, 
        Event.NODE_REMOVED, testObservation.getPath() + "/n1", true, null, null, false);
    
    Node n1 = testObservation.addNode("n1"); // /testObservation/n1
    Node n1n2 = n1.addNode("n2"); // /testObservation/n1/n2
    testObservation.save();
    
    n1n2.remove();
    testObservation.save();    
    assertTrue("A events count expected 1. Was: " + listener.getCounter(), listener.getCounter() == 1);    
    checkItemsExisted(new String[] {n1.getPath()}, new String[] {n1n2.getPath()});
    
    n1.remove();
    testObservation.save();
    assertTrue("A events count expected 2. Was: " + listener.getCounter(), listener.getCounter() == 2);    
    checkItemsExisted(null, new String[] {n1.getPath(), n1n2.getPath()});    
  }  
}
