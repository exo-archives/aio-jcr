/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.usecases;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.jcr.Item;
import javax.jcr.Node;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class TraverseInnerFoldersTest extends JCRTestBase {
  /*
   * This test calculates the time (ms or tps) of random reading of node of type nt:folder 
   * (using getItem() method), the nodes have structure like tree.
   * Required parameters:
   * jcr.amountOfInnerFolders - the count of folders to create (for further getting it)
   * jcr.depthOfStructure - the count of levels of tree structure 
   *     
  */
  private int          amountOfInnerFolders = 0;

  private int          depthOfStructure     = 0;

  private List<String> names                = new ArrayList<String>();

  private Random       rand                 = new Random();

  private Node         rootNode             = null;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    // required params: jcr.amountOfInnerFolders, jcr.depthOfStructure
    amountOfInnerFolders = tc.getIntParam("jcr.amountOfInnerFolders");
    depthOfStructure = tc.getIntParam("jcr.depthOfStructure");
    int depth = depthOfStructure;
    int count = amountOfInnerFolders;
    rootNode = context.getSession().getRootNode().addNode(
        context.generateUniqueName("rootNode"), "nt:unstructured");
    createFoldersRecursively(context, rootNode, depth, count);
    context.getSession().save();
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    int index = rand.nextInt((int) Math.pow(amountOfInnerFolders, depthOfStructure));
    Item item = rootNode.getSession().getItem(names.get(index));
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    rootNode.remove();
    context.getSession().save();
  }

  private void createFoldersRecursively(JCRTestContext context, Node rootNode, int depth, int count)
      throws Exception {
    if (depth < 1) {
      return;
    }
    for (int i = 0; i < count; i++) {
      String name = context.generateUniqueName("folder-" + i);
      Node current = rootNode.addNode(name, "nt:folder");
      names.add(current.getPath());
      createFoldersRecursively(context, current, depth - 1, count);
    }
  }

}
