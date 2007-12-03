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

package org.exoplatform.frameworks.webdavclient.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class LargeFileTest extends TestCase {
  
  public static final int BUFF_SIZE = 1024;
  public static final long BLOCKS_CNT = 1024 * 1024 * 5;
  //public static final int BLOCKS_CNT = 5;
  
  public static final String tempFileName = "c:/temp file " + System.currentTimeMillis() + ".exo";
  //public static final String tempFileName = "/temp file 1174472472093.exo";
   
  public static final String SRC_PATh = "/production/test file " + System.currentTimeMillis() + ".exe";
  
  public void testLargePut() throws Exception {    
    Log.info("LargeFileTest:testLargePut...");
    
    // PREPARE FILE...
    {
      byte []buffer = new byte[BUFF_SIZE];
      byte curVal = 0;
      for (int i = 0; i < BUFF_SIZE; i++) {
        buffer[i] = curVal;
        if (curVal == 255) {
          curVal = 0;
        } else {
          curVal++;
        }
      }
      
      File outFile = new File(tempFileName);
      if (outFile.exists()) {
        outFile.delete();
      }
      
      outFile.createNewFile();
      FileOutputStream outStream = new FileOutputStream(outFile);
      for (int i = 0; i < BLOCKS_CNT; i++) {
        outStream.write(buffer);
      }
      outStream.close();
    }
    
    Log.info("Putting>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    
    {
      File file = new File(tempFileName);
      FileInputStream inStream = new FileInputStream(file);
      
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(SRC_PATh);
      davPut.setRequestInputStream(inStream, BUFF_SIZE * BLOCKS_CNT);
      
      int status = davPut.execute();
      Log.info("STATUS: " + status);
    }    
    
    Log.info("done.");
  }

}
