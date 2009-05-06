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
package org.exoplatform.services.jcr.usecases.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.exoplatform.services.jcr.config.ContainerEntry;
import org.exoplatform.services.jcr.config.QueryHandlerEntry;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 06.05.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: ExportWorkspaceSystemViewTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class ExportWorkspaceSystemViewTest extends BaseUsecasesTest {

  public void testTwoRestores() throws Exception {
    { 
      SessionImpl sessionWS1 = (SessionImpl) repository.login(credentials, "ws1");
      
      sessionWS1.getRootNode()
                .addNode("asdasdasda", "nt:unstructured")
                .setProperty("data", "data_1");
      sessionWS1.save();
    
    
      // 1-st export
      File f1 = new File("target/1.xml");
      sessionWS1.exportWorkspaceSystemView(new FileOutputStream(f1), false, false);

      // 1-st import
      WorkspaceEntry ws1_restore_1 = makeWorkspaceEntry("ws1_restore_1", "jdbcjcr2export1");
      repository.configWorkspace(ws1_restore_1);
      
      repository.importWorkspace(ws1_restore_1.getName(), new FileInputStream(f1));
  
        // check
        SessionImpl back1 = (SessionImpl) repository.login(credentials, "ws1_restore_1");
        assertNotNull(back1.getRootNode().getNode("asdasdasda").getProperty("data"));
        
        // add date to restored workspace
        back1.getRootNode()
             .addNode("gdfgrghfhf", "nt:unstructured")
             .setProperty("data", "data_2");
        back1.save();
    }
    
    {
      // 2-st export
      SessionImpl back1 = (SessionImpl) repository.login(credentials, "ws1_restore_1");
      File f2 = new File("target/2.xml");
      back1.exportWorkspaceSystemView(new FileOutputStream(f2), false, false);

      // 2-st import
      WorkspaceEntry ws1_restore_2 = makeWorkspaceEntry("ws1_restore_2", "jdbcjcr2export2");
      repository.configWorkspace(ws1_restore_2);
      
      repository.importWorkspace(ws1_restore_2.getName(), new FileInputStream(f2));
      
      // check
        SessionImpl back2 = (SessionImpl) repository.login(credentials, "ws1_restore_2");
        assertNotNull(back2.getRootNode().getNode("gdfgrghfhf").getProperty("data"));
    }
  }
  
  private WorkspaceEntry makeWorkspaceEntry(String name, String sourceName) {
    WorkspaceEntry ws1e = (WorkspaceEntry) session.getContainer()
                                                     .getComponentInstanceOfType(WorkspaceEntry.class);

    WorkspaceEntry ws1back = new WorkspaceEntry();
    ws1back.setName(name);
    ws1back.setUniqueName(((RepositoryImpl) session.getRepository()).getName() + "_"
        + ws1back.getName()); 

    ws1back.setAccessManager(ws1e.getAccessManager());
    ws1back.setAutoInitializedRootNt(ws1e.getAutoInitializedRootNt());
    ws1back.setAutoInitPermissions(ws1e.getAutoInitPermissions());
    ws1back.setCache(ws1e.getCache());
    ws1back.setContainer(ws1e.getContainer());
    ws1back.setLockManager(ws1e.getLockManager());

    // Indexer
    ArrayList qParams = new ArrayList();
    qParams.add(new SimpleParameterEntry("indexDir", "target" + File.separator + name));
    QueryHandlerEntry qEntry = new QueryHandlerEntry("org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex",
                                                     qParams);

    ws1back.setQueryHandler(qEntry);

    ArrayList params = new ArrayList();
    for (Iterator i = ws1back.getContainer().getParameters().iterator(); i.hasNext();) {
      SimpleParameterEntry p = (SimpleParameterEntry) i.next();
      SimpleParameterEntry newp = new SimpleParameterEntry(p.getName(), p.getValue());

      if (newp.getName().equals("source-name"))
        newp.setValue(sourceName);
      else if (newp.getName().equals("swap-directory"))
        newp.setValue("target/temp/swap/" + name);

      params.add(newp);
    }

    ContainerEntry ce = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
                                           params);
    ws1back.setContainer(ce);

    return ws1back;
  }
}
