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
package org.exoplatform.services.jcr.ext.replication;

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
