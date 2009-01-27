package org.exoplatform.services.jcr.api.nodetypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.version.VersionException;

import org.apache.lucene.search.MatchAllDocsQuery;

import org.exoplatform.services.jcr.JcrAPIBaseTest;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeDataManagerImpl;
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
    //assertEquals("nt",ntManager.getPrimaryNodeTypes().nextNodeType().getName()
    // .substring(0,2));
    //assertEquals("mix",ntManager.getMixinNodeTypes().nextNodeType().getName().
    // substring(0,3));
  }

  // public void testNodeTypesOrder() throws Exception {
  // NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager();
  // NodeTypeIterator nts = ntManager.getPrimaryNodeTypes();
  // assertTrue(nts.getSize() > 0);
  // assertEquals("nt:base", nts.nextNodeType().getName());
  // // Prerequisites : the second entry in nodetypes.xml should be
  // // "nt:unstructured" !!!!!
  // assertEquals("nt:unstructured", nts.nextNodeType().getName());
  // }

  public void testNtQuery() throws Exception {
    NodeTypeDataManagerImpl ntManager = (NodeTypeDataManagerImpl) session.getWorkspace()
                                                                         .getNodeTypesHolder();
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
    NodeTypeDataManagerImpl ntManager = (NodeTypeDataManagerImpl) session.getWorkspace()
                                                                         .getNodeTypesHolder();

    assertTrue(ntManager.getNodes(Constants.MIX_VERSIONABLE).size() == 0);
    Node t = root.addNode("tt");
    t.addMixin("mix:versionable");
    session.save();
    assertTrue(ntManager.getNodes(Constants.MIX_VERSIONABLE).size() != 0);
  }

  public void testNtQueryFindNodeByProperty() throws ItemExistsException,
                                             PathNotFoundException,
                                             VersionException,
                                             ConstraintViolationException,
                                             LockException,
                                             RepositoryException,
                                             IOException {
    NodeTypeDataManagerImpl ntManager = (NodeTypeDataManagerImpl) session.getWorkspace()
                                                                         .getNodeTypesHolder();
    int refNodes = ntManager.getNodes(Constants.MIX_REFERENCEABLE).size();
    Node testNode1 = root.addNode("test1");
    testNode1.addMixin("mix:referenceable");
    testNode1.setProperty("p1", 1);
    Node testNode2 = root.addNode("test2");
    testNode2.addMixin("mix:referenceable");
    testNode2.setProperty("p2", 2);

    session.save();
    assertEquals(2, ntManager.getNodes(Constants.MIX_REFERENCEABLE).size() - refNodes);
    assertEquals(1, ntManager.getNodes(Constants.MIX_REFERENCEABLE,
                                       new InternalQName[] { new InternalQName("", "p1") },
                                       new InternalQName[0]).size());
    assertEquals(1, ntManager.getNodes(Constants.MIX_REFERENCEABLE,
                                       new InternalQName[] { new InternalQName("", "p2") },

                                       new InternalQName[0]).size());

    assertEquals(0, ntManager.getNodes(Constants.MIX_REFERENCEABLE,
                                       new InternalQName[0],
                                       new InternalQName[] { new InternalQName("", "p1"),
                                           new InternalQName("", "p2") }).size()
        - refNodes);

  }
}
