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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.TesterItemsPersistenceListener;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.util.SIDGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 03.02.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: ChangesStreamTest.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class ChangesStreamTest extends BaseStandaloneTest {

  public void _testStream() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");
    TransientNodeData localItem1 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                             new InternalQName(null,
                                                                                               "item1")),
                                                         SIDGenerator.generate(),
                                                         0,
                                                         Constants.NT_UNSTRUCTURED,
                                                         new InternalQName[0],
                                                         0,
                                                         Constants.ROOT_UUID,
                                                         new AccessControlList());

    TransientPropertyData localProperty1 = new TransientPropertyData(QPath.makeChildPath(localItem1.getQPath(),
                                                                                         new InternalQName(null,
                                                                                                           "testProperty1")),
                                                                     SIDGenerator.generate(),
                                                                     0,
                                                                     PropertyType.STRING,
                                                                     localItem1.getIdentifier(),
                                                                     false);
    localProperty1.setValue(new ReplicableValueData("test string".getBytes(), 0));

    TransientPropertyData localProperty2 = new TransientPropertyData(QPath.makeChildPath(localItem1.getQPath(),
                                                                                         new InternalQName(null,
                                                                                                           "testProperty2")),
                                                                     SIDGenerator.generate(),
                                                                     0,
                                                                     PropertyType.STRING,
                                                                     localItem1.getIdentifier(),
                                                                     false);
    localProperty2.setValue(new ReplicableValueData("test string 2".getBytes(), 0));

    final ItemState item11Add = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(item11Add);
    final ItemState property1Add = new ItemState(localProperty1, ItemState.ADDED, false, null);
    localLog.add(property1Add);
    final ItemState property2Add = new ItemState(localProperty2, ItemState.ADDED, false, null);
    localLog.add(property2Add);

    // test

    File temp = File.createTempFile("cout", "test", new File("./target"));
    temp.deleteOnExit();

    ChangesOutputStream currentOut = new ChangesOutputStream(new FileOutputStream(temp));

    TransactionChangesLog tlog = new TransactionChangesLog(localLog);

    currentOut.writeObject(tlog);
    currentOut.close();
    currentOut = null;

    currentOut = new ChangesOutputStream(new FileOutputStream(temp, true));

    tlog = new TransactionChangesLog(localLog); // same

    currentOut.writeObject(tlog);
    currentOut.close();
    currentOut = null;
    // check

    ChangesInputStream currentIn = new ChangesInputStream(new FileInputStream(temp));

    // 1
    Object to = currentIn.readObject();
    assertTrue(TransactionChangesLog.class.isInstance(to));

    TransactionChangesLog dtlog = (TransactionChangesLog) to;

    assertTrue(dtlog.getAllStates().get(0).equals(item11Add));

    // 2
    to = currentIn.readObject();
    assertTrue(TransactionChangesLog.class.isInstance(to));

    dtlog = (TransactionChangesLog) to;

    assertTrue(dtlog.getAllStates().get(0).equals(item11Add));

  }

  public void testMove() throws Exception {
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    NodeImpl n1 = (NodeImpl) root.addNode("testNodeFirst", "nt:folder");
    // n1.setProperty("prop1", "dfdasfsdf");
    // n1.setProperty("secondProp", "ohohoh");
    root.save();

    // NodeImpl n2 = (NodeImpl) root.addNode("testNodeSecond");
    // n2.setProperty("prop1", "dfdasfsdfSecond");
    // n2.setProperty("secondProp", "ohohohSecond");
    // root.save();

    //session.move(n1.getPath(), "/testNodeRenamed");
    //root.addNode("testNodeSecond", "nt:unstructured");
    n1.remove();
    root.save();

    // test

    File temp = File.createTempFile("cout", "test", new File("./target"));
    temp.deleteOnExit();

    //final String block= "======BLOCK======";
    
    FileOutputStream fout = new FileOutputStream(temp);
    ChangesOutputStream currentOut = new ChangesOutputStream(fout);

    currentOut.writeObject(pl.pushChanges().get(0));
    //currentOut.writeObject(block);
    //currentOut.flush();

    //currentOut = new ChangesOutputStream(new FileOutputStream(fd));

    //TransactionChangesLog log1 = pl.pushChanges().get(1);
    //PlainChangesLog plog1 = new PlainChangesLogImpl("sessionId-1");
    //plog1.add(log1.getAllStates().get(0));
    //plog1.add(log1.getAllStates().get(1));
    //plog1.add(log1.getAllStates().get(2));
    
    currentOut.writeObject(pl.pushChanges().get(1));
    //currentOut.writeObject(block);
    
    currentOut.flush();
    currentOut = null;
    
    // check
    ChangesInputStream currentIn = new ChangesInputStream(new FileInputStream(temp));

    // 1
    Object to = currentIn.readObject();
    assertTrue(TransactionChangesLog.class.isInstance(to));

    TransactionChangesLog dtlog = (TransactionChangesLog) to;

    //assertTrue(dtlog.getAllStates().get(0).get.equals());

    // 2
    //String s = (String) currentIn.readObject();
    to = currentIn.readObject();
    assertTrue(TransactionChangesLog.class.isInstance(to));

    dtlog = (TransactionChangesLog) to;

    //assertTrue(dtlog.getAllStates().get(0).equals(item11Add));

    //String s2 = (String) currentIn.readObject();
  }

}
