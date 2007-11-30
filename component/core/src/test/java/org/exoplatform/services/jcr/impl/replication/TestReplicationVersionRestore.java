/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved. 
 * Please look at license.txt in info directory for more license detail.  
 */

package org.exoplatform.services.jcr.impl.replication;

import javax.jcr.Node;
import javax.jcr.version.Version;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 02.03.2007
 * 14:31:17
 * 
 * @version $Id: TestReplicationVersionRestore.java 02.03.2007 14:31:17 rainfox
 */

public class TestReplicationVersionRestore extends BaseReplicationTest {
  public void testRestore() throws Exception {
    Node srcVersionNode = root.addNode("Version node 1");
    srcVersionNode.setProperty("jcr:data", "Base version");
    srcVersionNode.addMixin("mix:versionable");
    session.save();

    Thread.sleep(2 * 1000);

    Node destVersionNode = root2.getNode("Version node 1");
    assertEquals("Base version", destVersionNode.getProperty("jcr:data").getString());

    srcVersionNode.checkin();
    session.save();

    srcVersionNode.checkout();
    srcVersionNode.setProperty("jcr:data", "version 1");
    session.save();

    Thread.sleep(2 * 1000);

    assertEquals("version 1", destVersionNode.getProperty("jcr:data").getString());
    
    
    srcVersionNode.checkin();
    session.save();
    
    srcVersionNode.checkout();
    srcVersionNode.setProperty("jcr:data", "version 2");
    session.save();
    
    Thread.sleep(2 * 1000);
    
    assertEquals("version 2", destVersionNode.getProperty("jcr:data").getString());
    
    Version baseVersion = srcVersionNode.getBaseVersion();
    srcVersionNode.restore(baseVersion, true);
    session.save();
    
    Thread.sleep(2 * 1000);
    
    assertEquals("version 1", destVersionNode.getProperty("jcr:data").getString());
    
    
    Version baseVersion1 = srcVersionNode.getBaseVersion();
    Version []predesessors = baseVersion1.getPredecessors();
    Version restoreToBaseVersion = predesessors[0];

    srcVersionNode.restore(restoreToBaseVersion, true);
    session.save();
    
    Thread.sleep(2 * 1000);
    
    assertEquals("Base version", destVersionNode.getProperty("jcr:data").getString());
  }
}
