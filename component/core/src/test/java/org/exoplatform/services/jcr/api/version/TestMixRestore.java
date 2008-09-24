package org.exoplatform.services.jcr.api.version;

import javax.jcr.Node;
import javax.jcr.version.Version;

/**
 * Created by The eXo Platform SAS 31.08.2006 [VO]
 * 
 * @author <a href="mailto:vitaliy.obmanjuk@exoplatform.com.ua">Vitaliy Obmanjuk</a>
 * @version $Id: TestMixRestore.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class TestMixRestore extends BaseVersionTest {

  private Node testVersionable = null;

  public void setUp() throws Exception {
    super.setUp();
    testVersionable = root.addNode("testVersionable", "nt:unstructured");
    testVersionable.addMixin("mix:versionable");
    root.save();
  }

  protected void tearDown() throws Exception {
    testVersionable.checkout();
    testVersionable.remove();
    root.save();
    super.tearDown();
  }

  public void testVersionHistoryTree() throws Exception {
    Node testref = testVersionable.addNode("testref");
    testref.addMixin("mix:referenceable");
    testVersionable.save();
    Version ver1 = testVersionable.checkin(); // v1
    testVersionable.restore(ver1.getName(), true);
    testVersionable.restore(ver1, true);
  }
}
