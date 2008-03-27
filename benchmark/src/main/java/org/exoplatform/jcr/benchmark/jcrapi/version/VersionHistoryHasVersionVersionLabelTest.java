/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi.version;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.version.Version;
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

public class VersionHistoryHasVersionVersionLabelTest extends AbstractGetItemTest {

  private List<Version>   versions     = new ArrayList<Version>();
  
  @Override
  protected void createContent(Node parent, TestCase tc, JCRTestContext context) throws Exception {
    Node vnode = parent.addNode(context.generateUniqueName("versionableNode"));
    vnode.addMixin("mix:versionable");
    context.getSession().save();
    vnode.checkin(); // v.1
    vnode.checkout();
    vnode.addNode("Subnode").setProperty("Property", "property of subnode");
    vnode.save();
    versions.add(vnode.checkin()); // v.2
    vnode.checkout();
    
    VersionHistory vh = vnode.getVersionHistory();
    vh.addVersionLabel("1", "v.1", false);
    vh.addVersionLabel("2", "v.2", false);
    vh.addVersionLabel("2", "ver.2", false);
    
    addNode(vnode.getVersionHistory());    
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    final int iter = getCurrentIteration();
    final VersionHistory vh = (VersionHistory) nextNode();
    vh.hasVersionLabel(versions.get(iter), "ver.2");
  }

}
