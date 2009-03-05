/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.usecases.version;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.TesterItemsPersistenceListener;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 04.03.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: InvestigateVersionTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class InvestigateVersionTest extends BaseUsecasesTest {

  
  public void testVersion() throws Exception {
    TesterItemsPersistenceListener system_ws_pl = new TesterItemsPersistenceListener(this.session);
    
    SessionImpl session_ws1 = (SessionImpl) repository.login(credentials, "ws1");
    
    TesterItemsPersistenceListener ws1_pl = new TesterItemsPersistenceListener(session);
    
    
    Node srcVersionNode = session.getRootNode().addNode("Version node 1");
    srcVersionNode.setProperty("jcr:data", "Base version");
    srcVersionNode.addMixin("mix:versionable");
    session.save();

    srcVersionNode.checkin();
    session.save();

    /*srcVersionNode.checkout();
    srcVersionNode.setProperty("jcr:data", "version 1");
    session.save();

    srcVersionNode.checkin();
    session.save();

    srcVersionNode.checkout();
    srcVersionNode.setProperty("jcr:data", "version 2");
    session.save();

    Version baseVersion = srcVersionNode.getBaseVersion();
    srcVersionNode.restore(baseVersion, true);
    session.save();

    Version baseVersion1 = srcVersionNode.getBaseVersion();
    Version[] predesessors = baseVersion1.getPredecessors();
    Version restoreToBaseVersion = predesessors[0];

    srcVersionNode.restore(restoreToBaseVersion, true);
    session.save();*/
    
    
    log.info("System 'ws' workspace :");
    dump(system_ws_pl.pushChanges());
    
    log.info("'ws1' workspace :");
    dump(ws1_pl.pushChanges());
  }
  
  
  private void dump(List<TransactionChangesLog> list) {
    for (TransactionChangesLog tcl : list) {
      ChangesLogIterator it = tcl.getLogIterator();
      while (it.hasNextLog())
        log.info(it.nextLog().dump());
    }
  } 
}
