package org.exoplatform.services.jcr.api.nodetypes;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.apache.lucene.search.MatchAllDocsQuery;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.core.query.QueryHandler;
import org.exoplatform.services.jcr.impl.core.query.lucene.FieldNames;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryHits;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestNodeTypeManager.java 11907 2008-03-13 15:36:21Z ksm $
 */

public class TestNodeTypeManager extends JcrAPIBaseTest {

  public void testGetNodeType() throws Exception {

    NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager();
    NodeType type = ntManager.getNodeType("nt:unstructured");
    assertEquals("nt:unstructured", type.getName());

    try {
      ntManager.getNodeType("nt:not-found");
      fail("exception should have been thrown");
    } catch (NoSuchNodeTypeException e) {
    }

  }

  public void testGetNodeTypes() throws Exception {
    NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager();
    assertTrue(ntManager.getAllNodeTypes().getSize() > 0);
    assertTrue(ntManager.getPrimaryNodeTypes().getSize() > 0);
    assertTrue(ntManager.getMixinNodeTypes().getSize() > 0);
    // assertEquals("nt",ntManager.getPrimaryNodeTypes().nextNodeType().getName().substring(0,2));
    // assertEquals("mix",ntManager.getMixinNodeTypes().nextNodeType().getName().substring(0,3));
  }

  public void testNodeTypesOrder() throws Exception {
    NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager();
    NodeTypeIterator nts = ntManager.getPrimaryNodeTypes();
    assertTrue(nts.getSize() > 0);
    assertEquals("nt:base", nts.nextNodeType().getName());
    // Prerequisites : the second entry in nodetypes.xml should be "nt:unstructured" !!!!!
    assertEquals("nt:unstructured", nts.nextNodeType().getName());
  }

  public void testNtQuery() throws Exception {
    NodeTypeManagerImpl ntManager = session.getWorkspace().getNodeTypeManager();
    QueryHandler qh = ntManager.getQueryHandlers().iterator().next();
    QueryHits hits = qh.executeQuery(new MatchAllDocsQuery(),
                                     true,
                                     new InternalQName[0],
                                     new boolean[0]);
    List<String> uuidList = new ArrayList<String>(hits.length());
    for (int i = 0; i < hits.length(); i++) {
      uuidList.add(hits.getFieldContent(i, FieldNames.UUID));
    }
    assertTrue(uuidList.size() > 0);
  }

  public void testNtQueryNtBase() throws Exception {
    NodeTypeManagerImpl ntManager = session.getWorkspace().getNodeTypeManager();
    QueryHandler qh = ntManager.getQueryHandlers().iterator().next();

    assertTrue(ntManager.getNodes(Constants.MIX_VERSIONABLE).size() == 0);
    Node t = root.addNode("tt");
    t.addMixin("mix:versionable");
    session.save();
    assertTrue(ntManager.getNodes(Constants.MIX_VERSIONABLE).size() != 0);

  }
}
