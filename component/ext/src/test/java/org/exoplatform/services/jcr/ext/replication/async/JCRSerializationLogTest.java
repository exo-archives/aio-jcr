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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.JCRObjectOutputImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.JCRObjectInputImpl;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: JCRSerializationLogTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class JCRSerializationLogTest extends BaseStandaloneTest {

  private final int iter = 100;

  public void testWriteLog() throws Exception {
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    for (int i = 0; i < iter; i++) {

      File file = this.createBLOBTempFile(24);
      FileInputStream fis = new FileInputStream(file);

      NodeImpl node = (NodeImpl) root.addNode("file", "nt:file");
      NodeImpl cont = (NodeImpl) node.addNode("jcr:content", "nt:resource");
      cont.setProperty("jcr:mimeType", "text/plain");
      cont.setProperty("jcr:lastModified", Calendar.getInstance());
      cont.setProperty("jcr:encoding", "UTF-8");

      cont.setProperty("jcr:data", fis);
      root.save();

      file.delete();
    }

    List<TransactionChangesLog> logs = pl.pushChanges();

    Iterator<TransactionChangesLog> it = logs.iterator();

    // Serialize with JCR
    long jcrwrite = 0;
    long jcrread = 0;

    File jcrfile = File.createTempFile("jcr", "test");
    JCRObjectOutputImpl jcrout = new JCRObjectOutputImpl(new FileOutputStream(jcrfile));

    long t1 = System.currentTimeMillis();
    while (it.hasNext()) {
      jcrout.writeObject(it.next());
    }
    jcrwrite = System.currentTimeMillis() - t1;
    jcrout.close();

    // deserialize
    JCRObjectInputImpl jcrin = new JCRObjectInputImpl(new FileInputStream(jcrfile));

    List<TransactionChangesLog> readed = new ArrayList<TransactionChangesLog>();
    long t3 = System.currentTimeMillis();

    for(int i=0; i<iter; i++){
      TransactionChangesLog obj = (TransactionChangesLog) jcrin.readObject();
      assertNotNull(obj);
      readed.add(obj); 
    }
    
    jcrread = System.currentTimeMillis() - t3;
    jcrin.close();

    //check it
    //Iterator<TransactionChangesLog> it = readed.iterator();
    //while(it.hasNext()){
    //  checkIterator(logs.get(i).getAllStates().iterator(), obj.getAllStates().iterator());
    //}

    // java
    long javaWrite = 0;
    long javaRead = 0;

    File jfile = File.createTempFile("java", "test");
    ObjectOutputStream jout = new ObjectOutputStream(new FileOutputStream(jfile));

    it = logs.iterator();
    long t2 = System.currentTimeMillis();
    while (it.hasNext()) {
      jout.writeObject(it.next());
    }
    javaWrite = System.currentTimeMillis() - t2;
    jout.close();

    // deserialize
    ObjectInputStream jin = new ObjectInputStream(new FileInputStream(jfile));

    long t4 = System.currentTimeMillis();

    for(int i=0; i<iter; i++){
      TransactionChangesLog obj = (TransactionChangesLog) jin.readObject();
      assertNotNull(obj);
    }
    javaRead = System.currentTimeMillis() - t4;
    jin.close();

    System.out.println(" JCR s- " + (jcrwrite));
    System.out.println(" Java s- " + (javaWrite));
    System.out.println(" JCR file size - " + jcrfile.length());
    System.out.println(" Java file size - " + jfile.length());
    System.out.println(" JCR des- " + (jcrread));
    System.out.println(" Java des- " + (javaRead));

  }

  private void checkIterator(Iterator<ItemState> expected, Iterator<ItemState> changes) throws Exception {

    while (expected.hasNext()) {

      assertTrue(changes.hasNext());
      ItemState expect = expected.next();
      ItemState elem = changes.next();

      assertEquals(expect.getState(), elem.getState());
      // assertEquals(expect.getAncestorToSave(), elem.getAncestorToSave());
      ItemData expData = expect.getData();
      ItemData elemData = elem.getData();
      assertEquals(expData.getQPath(), elemData.getQPath());
      assertEquals(expData.isNode(), elemData.isNode());
      assertEquals(expData.getIdentifier(), elemData.getIdentifier());
      assertEquals(expData.getParentIdentifier(), elemData.getParentIdentifier());

      if (!expData.isNode()) {
        PropertyData expProp = (PropertyData) expData;
        PropertyData elemProp = (PropertyData) elemData;
        assertEquals(expProp.getType(), elemProp.getType());
        assertEquals(expProp.isMultiValued(), elemProp.isMultiValued());

        List<ValueData> expValDat = expProp.getValues();
        List<ValueData> elemValDat = elemProp.getValues();
        assertEquals(expValDat.size(), elemValDat.size());
        for (int j = 0; j < expValDat.size(); j++) {
          assertTrue(java.util.Arrays.equals(expValDat.get(j).getAsByteArray(),
                                             elemValDat.get(j).getAsByteArray()));

          // check is received property values ReplicableValueData
          // assertTrue(elemValDat.get(j) instanceof ReplicableValueData);
        }
      }
    }
    assertFalse(changes.hasNext());

  }
}
