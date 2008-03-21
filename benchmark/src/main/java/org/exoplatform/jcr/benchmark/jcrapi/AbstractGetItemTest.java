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
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */

public abstract class AbstractGetItemTest extends JCRTestBase {

  protected Node         rootNode      = null;

  private List<String> names = new ArrayList<String>();
  
  private volatile int iteration = 0;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    int runIterations = tc.getIntParam("japex.runIterations");
    
    if (runIterations <= 0)
      throw new Exception("japex.runIterations should be a positive number, but " + runIterations);
    
    Session session = context.getSession();
    rootNode = session.getRootNode().addNode(context.generateUniqueName("rootNode"));
    session.save();
    
    Node parent = null;
    
    for (int i = 0; i < runIterations; i++) {
      if (i % 100 == 0) {
        // add 100 props and commit, 
        rootNode.save();
        // change the parent parent
        parent = rootNode.addNode(context.generateUniqueName("node"));
      }
      createContent(parent, tc, context);
    }
    session.save();
  }

  protected String nextName() {
    return names.get(iteration++);
  }
  
  protected void addName(String name) {
    names.add(name);
  }
  
  protected abstract void createContent(Node parent, TestCase tc, JCRTestContext context) throws Exception;
  
  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    rootNode.refresh(false);
    
    for (NodeIterator nodes = rootNode.getNodes(); nodes.hasNext();) {
      nodes.nextNode().remove();
      rootNode.save();
    }
    
    rootNode.remove();
    context.getSession().save();
    
    names.clear();
  }

}
