package org.exoplatform.services.jcr.usecases.version;

import javax.jcr.Node;
import javax.jcr.version.Version;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SAS
 * 
 * @version $Id$
 */

public class RemoveVersionAfterRestoreTest extends BaseUsecasesTest {

  public void testRemoveVersionAfterRestore() throws Exception {
    Node node1 = root.addNode("Node1", "nt:unstructured");
    node1.addMixin("mix:versionable");
    root.save();
    Version ver1 = node1.checkin();
    node1.checkout();
    Version ver2 = node1.checkin();
    node1.checkout();
    Version ver3 = node1.checkin();
    node1.checkout();
    node1.restore(ver2, true);
    node1.getVersionHistory().removeVersion(ver1.getName());
    node1.getVersionHistory().removeVersion(ver3.getName());
    // session.save(); // unnecessary here
    assertNotNull(session.getRootNode().getNode("Node1"));
    node1.restore(ver2, true);
    assertNotNull(session.getRootNode().getNode("Node1"));
  }
}
