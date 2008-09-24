package org.exoplatform.services.jcr.api;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestAll extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite("exo.jcr tests");

    suite.addTestSuite(org.exoplatform.services.jcr.api.accessing.TestAccessRepository.class);

    suite.addTestSuite(org.exoplatform.services.jcr.api.reading.TestItem.class);
    suite.addTestSuite(org.exoplatform.services.jcr.api.reading.TestItemVisitor.class);
    suite.addTestSuite(org.exoplatform.services.jcr.api.reading.TestNode.class);

    suite.addTestSuite(org.exoplatform.services.jcr.api.reading.TestProperty.class);
    suite.addTestSuite(org.exoplatform.services.jcr.api.reading.TestSession.class);

    suite.addTestSuite(org.exoplatform.services.jcr.api.reading.TestValue.class);
    suite.addTestSuite(org.exoplatform.services.jcr.api.reading.TestWorkspace.class);

    suite.addTestSuite(org.exoplatform.services.jcr.api.writing.TestSession.class);

    suite.addTestSuite(org.exoplatform.services.jcr.api.writing.TestItem.class);

    suite.addTestSuite(org.exoplatform.services.jcr.api.writing.TestAddNode.class);

    suite.addTestSuite(org.exoplatform.services.jcr.api.writing.TestAssignMixin.class);

    suite.addTestSuite(org.exoplatform.services.jcr.api.writing.TestCorrespondingNode.class);

    suite.addTestSuite(org.exoplatform.services.jcr.api.writing.TestRemove.class);

    suite.addTestSuite(org.exoplatform.services.jcr.api.writing.TestNodeReference.class);

    suite.addTestSuite(org.exoplatform.services.jcr.api.writing.TestSetProperty.class);
    suite.addTestSuite(org.exoplatform.services.jcr.api.writing.TestValue.class);

    suite.addTestSuite(org.exoplatform.services.jcr.api.writing.TestCopyNode.class);

    return suite;
  }
}
