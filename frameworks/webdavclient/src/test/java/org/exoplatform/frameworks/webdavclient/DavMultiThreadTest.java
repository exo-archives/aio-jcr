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

package org.exoplatform.frameworks.webdavclient;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavGet;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavOptions;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DavMultiThreadTest extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.DavMultiThreadTest");
  
  public static String WORKSPACE = "/production";
  
  public static final int FCOUNT = 3;
  public static final int CLIENTS_COUNT = 10; 
  
  public void testDavMultiThread() throws Exception {    
    log.info("StartTest...");
    
    ArrayList<TestDavClient> clients = new ArrayList<TestDavClient>();
    for (int i = 0; i < CLIENTS_COUNT; i++) {
      TestDavClient davClient = new TestDavClient();
      davClient.start();
      clients.add(davClient);
    }
    
    boolean isNeedWait = true;
    while (isNeedWait) {
      isNeedWait = false;
      for (int i = 0; i < CLIENTS_COUNT; i++) {
        TestDavClient curClient = clients.get(i);
        if (curClient.isAlive()) {
          isNeedWait = true;
          break;
        }
      }
      Thread.sleep(100);
    }
    
    log.info("Test complete.");    
  }
  
  protected class TestDavClient extends Thread {
    
    private Log clientLog;
    
    public TestDavClient() {
      clientLog = ExoLogger.getLogger("jcr.DavclientTest_" + this.getId());
      clientLog.info("Client created.");
    }
    
    protected void log(String message) {
      clientLog.info(message);
    }
    
    public void run() {
      log("Client started.");
      
      try {
        log("OPTIONS test...");        
        {
          DavOptions davOptions = new DavOptions(TestContext.getContextAuthorized());
          assertEquals(davOptions.execute(), Const.HttpStatus.OK);
        }        
        
        log("PROPFIND test...");
        {
          DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
          davPropFind.setResourcePath(WORKSPACE);
          assertEquals(davPropFind.execute(), Const.HttpStatus.MULTISTATUS);
        }
        
        String rootTestFolder = WORKSPACE + "/test_folder_" + this.getId();
        
        log("MKCOL test...");
        {
          DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
          davMkCol.setResourcePath(rootTestFolder);
          assertEquals(davMkCol.execute(), Const.HttpStatus.CREATED);
        }
        
        for (int i1 = 0; i1 < FCOUNT; i1++) {
          String subFolder01 = rootTestFolder + "/" + "sub_folder_" + i1;
          {
            DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
            davMkCol.setResourcePath(subFolder01);
            assertEquals(davMkCol.execute(), Const.HttpStatus.CREATED);
          }
          
          for (int i2 = 0; i2 < FCOUNT; i2++) {
            String name = subFolder01 + "/sub_folder_02_" + i2;
            log("Creating: " + name);
            DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
            davMkCol.setResourcePath(name);
            assertEquals(davMkCol.execute(), Const.HttpStatus.CREATED);
          }
          
        }
        
        log("PUT test...");
        
        for (int i1 = 0; i1 < FCOUNT; i1++) {
          for (int i2 = 0; i2 < FCOUNT; i2++) {                        
            for (int i3 = 0; i3 < FCOUNT; i3++) {
              String fileName = rootTestFolder + "/" + "sub_folder_" + i1 + "/sub_folder_02_" + i2 + "/test_file_" + i3 + ".txt";
              
              String fileContent = "CONTENT FOR FILE " + fileName;
              
              char []fContChars = fileContent.toCharArray();
              byte []dataBytes = new byte[fContChars.length];
              for (int i = 0; i < dataBytes.length; i++) {
                dataBytes[i] = (byte)fContChars[i];
              }
              
              log("Creating: " + fileName);
              
              DavPut davPut = new DavPut(TestContext.getContextAuthorized());
              davPut.setResourcePath(fileName);
              davPut.setRequestDataBuffer(dataBytes);
              
              assertEquals(davPut.execute(), Const.HttpStatus.CREATED);
              
            }
          }
        }
        
        log("GET test...");
        
        for (int i1 = 0; i1 < FCOUNT; i1++) {
          for (int i2 = 0; i2 < FCOUNT; i2++) {                        
            for (int i3 = 0; i3 < FCOUNT; i3++) {
              String fileName = rootTestFolder + "/" + "sub_folder_" + i1 + "/sub_folder_02_" + i2 + "/test_file_" + i3 + ".txt";
              
              log("Reading: " + fileName);
              
              DavGet davGet = new DavGet(TestContext.getContextAuthorized());
              davGet.setResourcePath(fileName);
              assertEquals(davGet.execute(), Const.HttpStatus.OK);
              
              String fileContent = "CONTENT FOR FILE " + fileName;
              char []fContChars = fileContent.toCharArray();
              byte []dataBytes = new byte[fContChars.length];
              for (int i = 0; i < dataBytes.length; i++) {
                dataBytes[i] = (byte)fContChars[i];
              }

              byte []fileData = davGet.getResponseDataBuffer();
              boolean identical = true;
              
              for (int i = 0; i < dataBytes.length; i++) {
                if (fileData[i] != dataBytes[i]) {
                  identical = false;
                  break;
                }
              }
              
              if (!identical) {
                fail();
              }
              
            }
          }
        }
        
        log("REMOVE test...");
        
        for (int i1 = 0; i1 < FCOUNT; i1++) {
          String subFolder1 = rootTestFolder + "/" + "sub_folder_" + i1;
          for (int i2 = 0; i2 < FCOUNT; i2++) {                        
            String subFolder2 = subFolder1 + "/sub_folder_02_" + i2; 
            for (int i3 = 0; i3 < FCOUNT; i3++) {
              String fileName = subFolder2 + "/test_file_" + i3 + ".txt";
              
              DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
              davDelete.setResourcePath(fileName);
              
              assertEquals(davDelete.execute(), Const.HttpStatus.NOCONTENT);
            }
            
            DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
            davDelete.setResourcePath(subFolder2);
            
            assertEquals(davDelete.execute(), Const.HttpStatus.NOCONTENT);
          }
          
          DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
          davDelete.setResourcePath(subFolder1);

          assertEquals(davDelete.execute(), Const.HttpStatus.NOCONTENT);
        }
        
        DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
        davDelete.setResourcePath(rootTestFolder);
        
        assertEquals(davDelete.execute(), Const.HttpStatus.NOCONTENT);
        
      } catch (Exception exc) {
        clientLog.info("Unhandled exception. " + exc.getMessage());
      }
      
    }
    
  }
  
}
