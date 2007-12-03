/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
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
 * Created by The eXo Platform SAS .<br/>
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
