/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi.version;

import javax.jcr.Node;
import javax.jcr.version.VersionHistory;

import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.jcr.benchmark.jcrapi.AbstractGetItemTest;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */

public class VersionHistoryGetVersionTest extends AbstractGetItemTest {

  @Override
  protected void createContent(Node parent, TestCase tc, JCRTestContext context) throws Exception {
    Node vnode = parent.addNode(context.generateUniqueName("versionableNode"));
    vnode.addMixin("mix:versionable");
    context.getSession().save();
    vnode.checkin(); // v.1
    vnode.checkout();
    
    addNode(vnode.getVersionHistory());    
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    final VersionHistory vh = (VersionHistory) nextNode();
    vh.getVersion("1");
  }

}
