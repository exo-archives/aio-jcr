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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SpoolFile;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ReplicableValuDataTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class ReplicableValuDataTest extends BaseStandaloneTest {

  private static final String TEST_PREFIX = "pref";

  private static final String TEST_SUFFIX = "suf";

//  public void testStoreStringValue() throws Exception {
//    String et = "hello";
//
//    ReplicableValueData val = new ReplicableValueData(et.getBytes(), 12);
//
//    File file = File.createTempFile(TEST_PREFIX, TEST_SUFFIX);
//
//    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
//
//    out.writeObject(val);
//    out.close();
//
//    ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
//
//    ReplicableValueData res = (ReplicableValueData) in.readObject();
//
//    assertTrue(java.util.Arrays.equals(et.getBytes(), res.getAsByteArray()));
//  }

  public void testBLOBValue() throws Exception {
    SpoolFile f = new SpoolFile(this.createBLOBTempFile(1024).getAbsolutePath());
    ReplicableValueData val = new ReplicableValueData(f, 10, null);

    File file = File.createTempFile(TEST_PREFIX, TEST_SUFFIX);

    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));

    out.writeObject(val);
    out.close();

    try{
      val.getAsStream();
    }catch(NullPointerException e){
      // correct
    }
    
    ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));

    ReplicableValueData res = (ReplicableValueData) in.readObject();

  //  checkStreams(new FileInputStream(f), res.getAsStream());

  }

//  public void testValuesList() throws Exception {
//
//    // create values
//    List<ReplicableValueData> list = new ArrayList<ReplicableValueData>();
//
//    Random random = new Random();
//    byte[] bytes = new byte[2475];
//    random.nextBytes(bytes);
//    list.add(new ReplicableValueData(bytes,10));
//    
//    String str = "hello";
//    list.add(new ReplicableValueData(str.getBytes() , 4));
//
//    //boolean bool = true;
//    //list.add(new ReplicableValueData(bool));
//
//    Calendar c = Calendar.getInstance();
//    list.add(new ReplicableValueData(c.toString().getBytes(),1));
//
//    //double d = 4.15;
//    //list.add(new ReplicableValueData(d));
//
//    //long l = 4468672;
//    //list.add(new ReplicableValueData(l));
//
//    InternalQName name = new InternalQName("jcr", "system");
//    list.add(new ReplicableValueData(name.toString().getBytes(),5));
//
////    QPath path = QPath.parse("/node");
// //   list.add(new ReplicableValueData(path));
//
//   // Identifier id = new Identifier("some_id");
//    //list.add(new ReplicableValueData(id));
//
//    //AccessControlEntry ac = new AccessControlEntry("identity", "permission");
//   // list.add(new ReplicableValueData(ac));
//
//    // serialize it
//    File file = File.createTempFile(TEST_PREFIX, TEST_SUFFIX);
//
//    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
//
//    Iterator<ReplicableValueData> it = list.iterator();
//    while (it.hasNext()) {
//      out.writeObject(it.next());
//    }
//    out.close();
//
//    // read objects
//    ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
//
//    List<ReplicableValueData> res = new ArrayList<ReplicableValueData>();
//    try {
//      ReplicableValueData val ;
//      while((val = (ReplicableValueData) in.readObject())!=null){
//        res.add(val);
//      }
//    } catch (EOFException e) {
//      // do nothing
//    } finally {
//      in.close();
//    }
//
//    // check lists
//    assertEquals(list.size(), res.size());
//
//    for (int i = 0; i < list.size(); i++) {
//
//      assertTrue(java.util.Arrays.equals(list.get(i).getAsByteArray(), res.get(i).getAsByteArray()));
//
//    }
//  }

  public void checkStreams(InputStream etalon, InputStream check) throws IOException {

    int bufsize = 1024;
    byte[] etBuf = new byte[bufsize];
    byte[] chBuf = new byte[bufsize];
    int el = 0;
    int cl = 0;
    while ((el = etalon.read(etBuf)) != -1) {
      cl = check.read(chBuf);
      assertEquals(el, cl);
      assertTrue(java.util.Arrays.equals(etBuf, chBuf));
    }
    assertEquals(-1, check.read(chBuf));

  }

}
