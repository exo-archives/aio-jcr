/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.jcrapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.jcr.Node;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .<br/>
 * Tests unstructured node reading 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class ReadRandomNodeTest extends JCRTestBase {

  private static boolean initialized = false;
  private static List <String> names = new ArrayList<String>(); 
  
  private Random rand = new Random();
  private int nodesNum = 100;
  private Node root;
  

  /** 
   * creates jcr.nodes (100 by def) unstructured nodes to read
   * @see org.exoplatform.jcr.benchmark.JCRTestBase#doPrepare(com.sun.japex.TestCase, org.exoplatform.jcr.benchmark.JCRTestContext)
   */
  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    
    root = context.getSession().getRootNode();

    if(initialized)
      return;
    initialized = true;
    
    if(tc.hasParam("jcr.nodes"))
      nodesNum = tc.getIntParam("jcr.nodes");
    // TODO
    //int levelsNum = tc.getIntParam("jcr.levels");
    
    for(int i=0; i<nodesNum; i++) {
      Node newNode = root.addNode(context.generateUniqueName("node"), "nt:unstructured");
      names.add(newNode.getName());
    }
    root.save();

  }

  /** 
   * randomly gets nodes
   */
  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {

    int index = rand.nextInt(nodesNum);

    try {
      Node node = root.getNode(names.get(index));
      
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }
  
}
