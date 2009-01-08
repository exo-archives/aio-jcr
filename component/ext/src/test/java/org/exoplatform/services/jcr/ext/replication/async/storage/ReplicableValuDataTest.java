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


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: ReplicableValuDataTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class ReplicableValuDataTest extends BaseStandaloneTest {
  
  private static final String TEST_PREFIX = "pref";
  private static final String TEST_SUFFIX = "suf";
  
  public void testStoreStringValue() throws Exception{
    String et = "hello";
    
    ReplicableValueData val = new ReplicableValueData(et);
    
    File file = File.createTempFile(TEST_PREFIX, TEST_SUFFIX);
    
    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
    
    out.writeObject(val);
    out.close();
    
    ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
    
    ReplicableValueData res = (ReplicableValueData)in.readObject();
    
    assertEquals(et, res.getString());
  }
  
  public void testBinaryValue(){
    
    
  }
  
  public void testBLOBValue() throws Exception{
    File f = this.createBLOBTempFile(1024);
    
    ReplicableValueData val = new ReplicableValueData(new FileInputStream(f));
    
    File file = File.createTempFile(TEST_PREFIX, TEST_SUFFIX);
    
    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
    
    out.writeObject(val);
    out.close();
    
    ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
    
    ReplicableValueData res = (ReplicableValueData)in.readObject();
    
    
    

    checkStreams(new FileInputStream(f), res.getAsStream());
    
  }
  
  public void checkStreams(InputStream etalon, InputStream check) throws IOException{
    
    int bufsize = 1024; 
    byte[] etBuf = new byte[bufsize];
    byte[] chBuf = new byte[bufsize];
    int el=0;
    int cl=0;
    while((el=etalon.read(etBuf))!=-1 && (cl=check.read(chBuf))!=-1){
      assertEquals(el,cl);
    }
    
  }
}
