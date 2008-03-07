package org.exoplatform.services.jcr.usecases.common;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

public class NodeIsModifiedTest extends BaseUsecasesTest {
  
  private List<Node> nodes         = new ArrayList<Node>();

  private final int  RUNITERATIONS = 2;

  public void testNodeIsModified() throws Exception {
    Node rootNode = root.addNode("testNodeIsModifiedTest");
    for (int i = 0; i < RUNITERATIONS; i++) {
      Node parentNode = rootNode.addNode("parentNode" + i);
      session.save();
      assertFalse(parentNode.isModified());
      Node childNode = parentNode.addNode("childNode" + i);
      assertTrue(parentNode.isModified());
      nodes.add(parentNode);
      assertTrue(parentNode.isModified());
    }
    for (int i = 0; i < RUNITERATIONS; i++) {
      Node node = nodes.remove(0);
      assertTrue(node.isModified());
    }
  }
}
