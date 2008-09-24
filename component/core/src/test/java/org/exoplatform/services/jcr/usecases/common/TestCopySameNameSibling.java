package org.exoplatform.services.jcr.usecases.common;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.Workspace;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

public class TestCopySameNameSibling extends BaseUsecasesTest {

  public void testCopySameNameSibling() throws RepositoryException {
    Session session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()),
                                       WORKSPACE);
    Node root = session.getRootNode();

    Node subRoot = root.addNode("jcrTest");
    Node testNode = subRoot.addNode("testNode");
    root.save();
    session.save();

    testNode = session.getRootNode().getNode("jcrTest/testNode");
    String srcPath = testNode.getPath();
    String destPath = subRoot.getPath() + srcPath.substring(srcPath.lastIndexOf("/"));
    Workspace workspace = session.getWorkspace();
    workspace.copy(srcPath, destPath);
    session.save();
    Node sameNameNode = session.getRootNode().getNode("jcrTest/testNode[2]");
    assertNotNull(sameNameNode);
    // copy same name testNode[2]
    srcPath = sameNameNode.getPath();
    // [VO] 27.07.06 Bug fix. Use old destPath.
    // destPath = subRoot.getPath() + srcPath.substring(srcPath.lastIndexOf("/")) ;
    try {
      workspace.copy(srcPath, destPath);
    } catch (Exception e) {
      fail("\n======>Can not copy. Exception is occur:" + e.getMessage());
    }

    subRoot.remove();
    session.save();
  }

}
