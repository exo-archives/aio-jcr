/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import java.util.Random;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.tools.tree.NameTraversingVisitor;
import org.exoplatform.services.jcr.impl.tools.tree.TreeGenerator;
import org.exoplatform.services.jcr.impl.tools.tree.generator.WeightNodeGenerator;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestItemAccess extends JcrImplBaseTest {

  private TreeGenerator    nGen;

  private QPath[]          validNames;

  private String[]         validUuids;

  private Node             testGetItemNode;

  private static final int TEST_ITEMS_COUNT = 2000;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    testGetItemNode = root.addNode("testGetItemNode");
    root.save();
    //geneteting tree maxDepth = 5 and maxWidth = 12
    nGen = new TreeGenerator(testGetItemNode, new WeightNodeGenerator(5, 12));
    nGen.genereteTree();
    validNames = NameTraversingVisitor.getValidNames(testGetItemNode,
        NameTraversingVisitor.SCOPE_ALL);
    
    validUuids = NameTraversingVisitor.getValidUuids(testGetItemNode,
        NameTraversingVisitor.SCOPE_ALL);
  }

  public void testGetItemTest() throws RepositoryException {
    SessionImpl newSession = repository.login(session.getCredentials(), session.getWorkspace()
        .getName());

    Random random = new Random();
    SessionDataManager tm = newSession.getTransientNodesManager();
    for (int i = 0; i < TEST_ITEMS_COUNT; i++) {
      QPath itemPath = validNames[random.nextInt(validNames.length)];
      assertNotNull(tm.getItem(itemPath, true));
      // System.out.println(itemPath.getAsString());
    }
    for (int i = 0; i < TEST_ITEMS_COUNT; i++) {
      String validUuid = validUuids[random.nextInt(validUuids.length)];
      assertNotNull(tm.getItemData((validUuid)));
      // System.out.println(itemPath.getAsString());
    }
    newSession.logout();
  }

  public void testGetItemAfterMove() throws RepositoryException {
    QPath[] validNodesNames = NameTraversingVisitor.getValidNames(testGetItemNode,
        NameTraversingVisitor.SCOPE_NODES);
    Random random = new Random();
    QPath srcNode = validNodesNames[random.nextInt(validNames.length)];
    QPath dstNode = null;
    while (dstNode == null){
      dstNode =  validNodesNames[random.nextInt(validNames.length)];
      if(dstNode.isDescendantOf(srcNode,false)){
        dstNode = null;
      }
    }
    //session.move()
  }
}
