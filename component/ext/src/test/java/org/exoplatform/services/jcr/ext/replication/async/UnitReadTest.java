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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.storage.SolidLocalStorageImpl;
import org.exoplatform.services.jcr.impl.core.NodeImpl;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: UnitReadTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class UnitReadTest  extends BaseStandaloneTest  {

  public void testReadLog() throws Exception {

    TransactionChangesLog curLog;

    File f = new File("target/file");
    
    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
    
    
    
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);
    
    
    NodeImpl n1 = (NodeImpl) root.addNode("testNodeFirst","nt:folder");
    //n1.setProperty("prop1", "dfdasfsdf");
    //n1.setProperty("secondProp", "ohohoh");
    root.save();

    //NodeImpl n2 = (NodeImpl) root.addNode("testNodeSecond");
    //n2.setProperty("prop1", "dfdasfsdfSecond");
    //n2.setProperty("secondProp", "ohohohSecond");
    //root.save();

    session.move(n1.getPath(), "/testNodeRenamed");
    root.save();

    out.writeObject(pl.pushChanges().get(0));
    
    out.flush();
    out.close();
    
    
    ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));

    // 
    do {
      try {
        
        curLog = (TransactionChangesLog) in.readObject();
        System.out.println(curLog.toString());
      } catch (EOFException e) {
        // ok
        curLog = null;
        System.out.println("no more logs");
      } catch(Exception e){
        curLog = null;
        e.printStackTrace();
      }
    } while (curLog != null);
  }

}
