/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.Random;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: TestChangesFile.java 111 2008-11-11 11:11:11Z serg $
 */
public class RandomChangesFileTest extends BaseStandaloneTest {

  private static final String CRC = "CRC";

  public void testWriteFile() throws Exception {
    RandomChangesFile file = new RandomChangesFile(CRC, System.currentTimeMillis());

    int size1 = 316;
    int size2 = 45;
    int size3 = 126;

    byte[] bufetalon = new byte[size1 + size2 + size3];

    byte[] buf1 = createBLOBTempData(size1);
    file.writeData(buf1, 0);
    System.arraycopy(buf1, 0, bufetalon, 0, size1);

    byte[] buf2 = createBLOBTempData(size2);
    file.writeData(buf2, size1);
    System.arraycopy(buf2, 0, bufetalon, size1, size2);

    byte[] buf3 = createBLOBTempData(size3);
    file.writeData(buf3, size1 + size2);
    System.arraycopy(buf3, 0, bufetalon, size1 + size2, size3);

    InputStream in = file.getInputStream();
    byte[] bufrez = new byte[size1 + size2 + size3];
    int readed = in.read(bufrez);
    assertEquals(size1 + size2 + size3, readed);
    assertEquals(true, java.util.Arrays.equals(bufetalon, bufrez));
  }

  public void testRandomWriteFile() throws Exception {
    RandomChangesFile file = new RandomChangesFile(CRC, System.currentTimeMillis());

    int size1 = 10;
    int size2 = 5;
    int size3 = 10;

    byte[] bufetalon = new byte[size1 + size2 + size3];

    byte[] buf1 = createBLOBTempData(size1);
    file.writeData(buf1, 0);
    System.arraycopy(buf1, 0, bufetalon, 0, size1);

    byte[] buf3 = createBLOBTempData(size3);
    file.writeData(buf3, size1 + size2);

    byte[] buf2 = createBLOBTempData(size2);
    file.writeData(buf2, size1);
    System.arraycopy(buf2, 0, bufetalon, size1, size2);

    System.arraycopy(buf3, 0, bufetalon, size1 + size2, size3);

    InputStream in = file.getInputStream();
    byte[] bufrez = new byte[size1 + size2 + size3];
    int readed = in.read(bufrez);
    assertEquals(size1 + size2 + size3, readed);
    assertEquals(true, java.util.Arrays.equals(bufetalon, bufrez));
  }

  public void testCorruptedWriteFile() throws Exception {
    RandomChangesFile file = new RandomChangesFile(CRC, System.currentTimeMillis());

    int size1 = 316;
    int size2 = 45;
    int size3 = 126;

    byte[] bufetalon = new byte[size1 + size2 + size3];

    byte[] buf1 = createBLOBTempData(size1);
    file.writeData(buf1, 0);
    System.arraycopy(buf1, 0, bufetalon, 0, size1);

    // byte[] buf2 = createBLOBTempData(size2);
    // file.writeData(buf2, size1);
    // System.arraycopy(buf2, 0, bufetalon,size1,size2);

    byte[] buf3 = createBLOBTempData(size3);
    file.writeData(buf3, size1 + size2);
    System.arraycopy(buf3, 0, bufetalon, size1 + size2, size3);

    InputStream in = file.getInputStream();
    byte[] bufrez = new byte[size1 + size2 + size3];
    int readed = in.read(bufrez);
    assertEquals(size1 + size2 + size3, readed);
    assertEquals(true, java.util.Arrays.equals(bufetalon, bufrez));
  }

  public void testWriteAlreadyOpenedFile() throws Exception {
    int size1 = 316;
    int size2 = 45;
    int size3 = 126;

    File f = File.createTempFile("TestChangesFile", "suf");

    RandomAccessFile ac = new RandomAccessFile(f, "rw");

    byte[] bufetalon = new byte[size1 + size2 + size3];

    byte[] buf2 = createBLOBTempData(size2);
    ac.seek(size1);
    ac.write(buf2);
    System.arraycopy(buf2, 0, bufetalon, size1, size2);

    ac.close();

    RandomChangesFile file = new RandomChangesFile(f, CRC, System.currentTimeMillis());

    byte[] buf1 = createBLOBTempData(size1);
    file.writeData(buf1, 0);
    System.arraycopy(buf1, 0, bufetalon, 0, size1);

    byte[] buf3 = createBLOBTempData(size3);
    file.writeData(buf3, size1 + size2);
    System.arraycopy(buf3, 0, bufetalon, size1 + size2, size3);

    InputStream in = file.getInputStream();
    byte[] bufrez = new byte[size1 + size2 + size3];
    int readed = in.read(bufrez);
    assertEquals(size1 + size2 + size3, readed);
    assertEquals(true, java.util.Arrays.equals(bufetalon, bufrez));
  }
  
  public void testUppend() throws Exception {
    int size1 = 6;
    int size2 = 5;
    int size3 = 7;


    byte[] bufetalon = new byte[size1 + size2 + size3];

   
    RandomChangesFile file = new RandomChangesFile( CRC, System.currentTimeMillis());

    byte[] buf1 = createBLOBTempData(size1);
    System.arraycopy(buf1, 0, bufetalon, 0, size1);

    byte[] buf2 = createBLOBTempData(size2);
    System.arraycopy(buf2, 0, bufetalon, size1, size2);

    byte[] buf3 = createBLOBTempData(size3);
    System.arraycopy(buf3, 0, bufetalon, size1 + size2, size3);

    file.getOutputStream().write(buf1);
    file.finishWrite();
    
    file.getOutputStream().write(buf2);
    file.finishWrite();
    
    file.getOutputStream().write(buf3);
    file.finishWrite();
    
    // check file
    
    InputStream in = file.getInputStream();
    
    byte[] buf = new byte[1024];
   
    int readed = in.read(buf);
    
    byte[] bufrez = new byte[readed];
    System.arraycopy(buf, 0, bufrez,0, readed);
    
    assertEquals(size1 + size2 + size3, readed);
    assertEquals(true, java.util.Arrays.equals(bufetalon, bufrez));
    
  }
  
  
  public void testUppendObjects() throws Exception {
    String first = new String("first");
    String second = new String("second");
    String third = new String("third");


    RandomChangesFile file = new RandomChangesFile( CRC, System.currentTimeMillis());

    ObjectOutputStream str = new ObjectOutputStream( file.getOutputStream());
    str.writeObject(first);
    str.writeObject(second);
    str.close();
    file.finishWrite();
    
    str = new ObjectOutputStream( file.getOutputStream());
    str.writeObject(second);
    str.close();
    file.finishWrite();

    str = new ObjectOutputStream( file.getOutputStream());
    str.writeObject(third);
    str.close();
    file.finishWrite();

    
    // check file
    ObjectInputStream in = new ObjectInputStream(file.getInputStream());
    
    String rez = (String) in.readObject();
    assertEquals(first, rez);
    rez = (String) in.readObject();
    assertEquals(second, rez);
    rez = (String) in.readObject();
    assertEquals(second, rez);
    rez = (String) in.readObject();
    assertEquals(third, rez);
  }
  
  public void testReadUnclosedFile() throws Exception {
    String first = new String("first");
    String second = new String("second");
    String third = new String("third");

    File f  = new File("target/testunclosed");
    
    RandomChangesFile file = new RandomChangesFile( f ,CRC, System.currentTimeMillis());

    ObjectOutputStream str = new ObjectOutputStream( file.getOutputStream());
    str.writeObject(first);
    str.writeObject(second);
    str.writeObject(third);
    //str.close();
    //file.finishWrite();
    
    // uclose streams
    
    file = null;
    
    file = new RandomChangesFile( f ,CRC, System.currentTimeMillis());
    
    // check file
    ObjectInputStream in = new ObjectInputStream(file.getInputStream());
    
    String rez = (String) in.readObject();
    assertEquals(first, rez);
    rez = (String) in.readObject();
    assertEquals(second, rez);
    rez = (String) in.readObject();
    assertEquals(third, rez);
    
    try{
      rez = (String) in.readObject();
      fail();
    }catch(EOFException e){
      //ok
    }
  }

  protected byte[] createBLOBTempData(int size) throws IOException {
    byte[] data = new byte[size]; // 1Kb
    Random random = new Random();
    random.nextBytes(data);
    return data;
  }

}
