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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.serialization.UnknownClassIdException;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 16.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: JcrImplSerializationBaseTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public abstract class JcrImplSerializationBaseTest extends JcrImplBaseTest {

  protected void checkIterator(Iterator<ItemState> expected, Iterator<ItemState> changes) throws Exception {

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

  protected File serializeLogs(List<TransactionChangesLog> logs) throws IOException,
                                                                UnknownClassIdException {
    File jcrfile = File.createTempFile("jcr", "test");
    ObjectWriterImpl jcrout = new ObjectWriterImpl(new FileOutputStream(jcrfile));

      
    TransactionChangesLogWriter wr = new TransactionChangesLogWriter();
    for (TransactionChangesLog tcl : logs){
      wr.write(jcrout, tcl);
    }

    jcrout.flush();
    jcrout.close();

    return jcrfile;
  }

  protected List<TransactionChangesLog> deSerializeLogs(File jcrfile) throws IOException,
                                                                UnknownClassIdException {
    ObjectReaderImpl jcrin = new ObjectReaderImpl(new FileInputStream(jcrfile));

    List<TransactionChangesLog> readed = new ArrayList<TransactionChangesLog>();

    try {
    while (true) {
      TransactionChangesLog obj  = (TransactionChangesLog)(new TransactionChangesLogReader(null, 200*1024)).read(jcrin);
      //TransactionChangesLog obj = new TransactionChangesLog();
      //obj.readObject(jcrin);
      readed.add(obj); 
    }
    } catch (EOFException e) {
      //ok
    }
    
    return readed;
  }

 // public void test() throws Exception {
 // }
}
