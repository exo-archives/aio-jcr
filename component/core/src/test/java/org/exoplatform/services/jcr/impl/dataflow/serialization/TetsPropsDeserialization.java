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
package org.exoplatform.services.jcr.impl.dataflow.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: TetsPropsDeserialization.java 111 2008-11-11 11:11:11Z serg $
 */
public class TetsPropsDeserialization extends JcrImplSerializationBaseTest {

  
  public void testPropReSetVal() throws Exception{
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);
    
    File content1 = this.createBLOBTempFile(300);
    File content2 = this.createBLOBTempFile(301);
    
    
    Node srcVersionNode = root.addNode("nt_file_node", "nt:file");
    Node contentNode = srcVersionNode.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", new FileInputStream(content1));
    contentNode.setProperty("jcr:mimeType", "text/plain");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory()
                                                       .createValue(Calendar.getInstance()));
    srcVersionNode.addMixin("mix:versionable");

   session.save();

    
    contentNode.setProperty("jcr:data", new FileInputStream(content2));
    session.save();
    
    List<TransactionChangesLog> logs = pl.pushChanges();
    
    File jcrfile = super.serializeLogs(logs);

    List<TransactionChangesLog> destLog = super.deSerializeLogs(jcrfile);

    assertEquals(logs.size(), destLog.size());

    for (int i = 0; i < logs.size(); i++)
      checkIterator(logs.get(i).getAllStates().iterator(), destLog.get(i)
                                                                    .getAllStates()
                                                                    .iterator());
  }
  
}
