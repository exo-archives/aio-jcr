/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.api.nodetypes;

import javax.jcr.Node;
import javax.jcr.nodetype.ConstraintViolationException;

import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestNodeTypeConstraints.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class TestNodeTypeConstraints extends JcrAPIBaseTest {

  public void testRemoveProtectedProperty() throws Exception {

    Node node1 = root.addNode("test", "nt:base");
//    log.debug(">>> node "+node1.getPrimaryNodeType().canRemoveItem("jct:primaryType"));
    try {
      node1.getProperty("jcr:primaryType").remove();
      fail("exception should have been thrown");
    } catch (ConstraintViolationException e) {
        log.debug("Exception FOUND: "+e);
    }
  }
/*
  public void testRemoveProtectedNode() throws Exception {
    NodeImpl node1 = (NodeImpl)root.addNode("test", "nt:unstructured");
    node1.addMixin("exo:accessControllable");
    node1.createChildNode("exo:permissions", "exo:userPermission", true);
    log.debug(">>> node "+node1.getMixinNodeTypes()[0].canRemoveItem("exo:permissions"));
    try {
      node1.getNode("exo:permissions").remove();
      fail("exception should have been thrown");
    } catch (ConstraintViolationException e) {
        log.debug("Exception FOUND: "+e);
    }
  }
*/
}
