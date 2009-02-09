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
package org.exoplatform.services.jcr.ext.replication.async.transport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.exoplatform.services.jcr.ext.replication.async.AbstractTrasportTest;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: CheckSumTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class CheckSumTest extends AbstractTrasportTest{

  public void testChecksum() throws Exception {
    MessageDigest digest;
    
    
    digest = MessageDigest.getInstance("MD5");
    
    
    
    final int filesize = 50 * 1024;

    File f = this.createBLOBTempFile(filesize);

    
    File chLogFile = File.createTempFile("crctest","SUF");
    DigestOutputStream dout = new DigestOutputStream(new FileOutputStream(chLogFile), digest);   
    InputStream in = new FileInputStream(f);
   
    Random random = new Random();
    long position = 0;
    byte[] buf;
    int readed = -1;
    do {
      buf = new byte[random.nextInt(1024) + 1];
      readed = in.read(buf);
      if (readed != -1) {
        dout.write(buf,0,readed);
      }
    } while (readed != -1);

    dout.close();
    in.close();
    buf = null;
    byte[] digestwrite = digest.digest();
    
    //Read from changesfile
    digest = MessageDigest.getInstance("MD5");
    DigestInputStream din = new DigestInputStream(new FileInputStream (chLogFile),digest);
    
    //check
    in = new FileInputStream(f);
   
    int readet = -1;
    int readch = -1;
    
    do{
      byte[] bufet = new byte[2048];
      byte[] bufch = new byte[2048];
      
      readet = in.read(bufet);
      readch = din.read(bufch);
      assertEquals(readet,readch);
      assertTrue(java.util.Arrays.equals(bufet, bufch));
    }while(readet!=-1 && readch!=-1);
    
    in.close();
    din.close();
    
    byte[] digestRead = digest.digest();
    
    assertTrue(java.util.Arrays.equals(digestwrite, digestRead));
    
    String wr = new String(digestwrite,"UTF-8");
    String rd = new String(digestRead,"UTF-8");
    assertEquals(wr,rd); 
  }
  
  
  protected byte[] createBLOBTempData(int size) throws IOException {
    byte[] data = new byte[size]; // 1Kb
    Random random = new Random();
    random.nextBytes(data);
    return data;
  }
  
}
