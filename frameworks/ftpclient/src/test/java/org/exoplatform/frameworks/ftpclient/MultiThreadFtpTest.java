/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient;

import java.util.ArrayList;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.commands.CmdList;
import org.exoplatform.frameworks.ftpclient.commands.CmdMkd;
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdPasv;
import org.exoplatform.frameworks.ftpclient.commands.CmdRetr;
import org.exoplatform.frameworks.ftpclient.commands.CmdRmd;
import org.exoplatform.frameworks.ftpclient.commands.CmdStor;
import org.exoplatform.frameworks.ftpclient.commands.CmdSyst;
import org.exoplatform.frameworks.ftpclient.commands.CmdType;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;
import org.exoplatform.services.log.ExoLogger;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public class MultiThreadFtpTest extends TestCase {

  public static final int ITEMS_COUNT = 3;
  public static final int CLIENTS_COUNT = 10;
  
  protected class FtpThreadTest extends Thread {
    
    protected Log log;
    protected String testFolder;
    
    public FtpThreadTest(String testFolder) {
      log = ExoLogger.getLogger("jcr." + this);
      this.testFolder = testFolder;
    }
    
    protected void createFolders(String rootTestFolder) {
      try {
        FtpClientSession client = FtpTestConfig.getTestFtpClient();
        client.connect();

        assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(new CmdUser("admin")));
        assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(new CmdPass("admin")));

        assertEquals(FtpConst.Replyes.REPLY_215, client.executeCommand(new CmdSyst()));
        assertEquals(FtpConst.Replyes.REPLY_200, client.executeCommand(new CmdType("A")));
        
        assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(new CmdPasv()));
        
        log.info("Verify folder exists...");
        
        if (client.executeCommand(new CmdList(rootTestFolder)) == FtpConst.Replyes.REPLY_226) {
          log.info("Test folder exist. try delete it...");
          assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(new CmdRmd(rootTestFolder)));
          log.info("Deleted.");
        }

        for (int i1 = 1; i1 <= ITEMS_COUNT; i1++) {
          String folder1 = rootTestFolder + "/folder_" + i1;
          assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(new CmdMkd(folder1)));
          for (int i2 = 1; i2 <= ITEMS_COUNT; i2++) {
            String folder2 = folder1.substring(0);
            folder2 += "/subfolder_" + i2;
            assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(new CmdMkd(folder2)));
            
            for (int i3 = 0; i3 < ITEMS_COUNT; i3++) {
              
              assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(new CmdPasv()));
              
              String file3 = folder2.substring(0);
              file3 += "/test_file_" + i3 + ".txt";
              
              CmdStor cmdStor = new CmdStor(file3);
              
              byte []fileData = new byte[256 * 4];
              for (int c = 0; c < 256*4; c++ ) {
                fileData[c] = (byte)'A';
                fileData[c] = fileData[c] += (byte)i3;
              }
              
              cmdStor.setFileContent(fileData);              
              assertEquals(FtpConst.Replyes.REPLY_226, client.executeCommand(cmdStor));
              
              log.info("File [" + file3 + "] created");
              
            }
            
          }
        }

        client.close();
      } catch (Exception exc) {
        log.info("Unhandled exception. " + exc.getMessage(), exc);
      }      
    }
    
    public void readAllFolders(String rootTestFolder) {
      try {
        FtpClientSession client = new FtpClientSessionImpl("127.0.0.1", 21);
        client.connect();
        
        assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(new CmdUser("admin")));
        assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(new CmdPass("admin")));

        assertEquals(FtpConst.Replyes.REPLY_215, client.executeCommand(new CmdSyst()));
        assertEquals(FtpConst.Replyes.REPLY_200, client.executeCommand(new CmdType("A")));
        
        for (int i1 = 1; i1 <= ITEMS_COUNT; i1++) {          
          String folder1 = rootTestFolder + "/folder_" + i1;
          for (int i2 = 1; i2 <= ITEMS_COUNT; i2++) {
            String folder2 = folder1.substring(0);
            folder2 += "/subfolder_" + i2;
            for (int i3 = 0; i3 < ITEMS_COUNT; i3++) {
              String file3 = folder2.substring(0);
              file3 += "/test_file_" + i3 + ".txt";
              
              log.info("CurFileName - [" + file3 + "]");
              
              assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(new CmdPasv()));
              
              CmdRetr cmdRetr = new CmdRetr(file3);
              int reply = client.executeCommand(cmdRetr);
              
              log.info("RETR REPLY - " + reply);
              
            }
              
          }

        }
        
        client.close();
        
      } catch (Exception exc) {
        log.info("Unhandled exception. " + exc.getMessage(), exc);
      }
    }
    
    public void removeAllFolders(String rootTestFolder) {
      log.info("Clearing...");
      try {
        FtpClientSession client = new FtpClientSessionImpl("127.0.0.1", 21);
        client.connect();

        assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(new CmdUser("admin")));
        assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(new CmdPass("admin")));

        assertEquals(FtpConst.Replyes.REPLY_215, client.executeCommand(new CmdSyst()));
        assertEquals(FtpConst.Replyes.REPLY_200, client.executeCommand(new CmdType("A")));
        
        for (int i1 = 1; i1 <= ITEMS_COUNT; i1++) {          
          String folder1 = rootTestFolder + "/folder_" + i1;
          for (int i2 = 1; i2 <= ITEMS_COUNT; i2++) {
            String folder2 = folder1.substring(0);
            folder2 += "/subfolder_" + i2;
            for (int i3 = 0; i3 < ITEMS_COUNT; i3++) {
              String file3 = folder2.substring(0);
              file3 += "/test_file_" + i3 + ".txt";
              
              log.info("Try delete [" + file3 + "]...");
              assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(new CmdRmd(file3)));              
            }
            
            log.info("Try delete [" + folder2 + "]...");
            assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(new CmdRmd(folder2)));
          }
          
          log.info("Try delete [" + folder1 + "]...");
          assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(new CmdRmd(folder1)));          

        }
        
        assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(new CmdRmd(rootTestFolder)));
        
        client.close();
      } catch (Exception exc) {
        log.info("Unhandled exception. " + exc.getMessage(), exc);
      }
      log.info("Complete.");
    }
    
    public void run() {
      log.info("Start test...");
      createFolders(testFolder);
      readAllFolders(testFolder);
      removeAllFolders(testFolder);
      log.info("Test complete.");
    }
    
  }
  
  public void testMultiThread() throws Exception {
    Log log = ExoLogger.getLogger("jcr.MultiThreadFtpTest");
    log.info("Test...");

    ArrayList<FtpThreadTest> testers = new ArrayList<FtpThreadTest>();
    
    for (int i = 0; i < CLIENTS_COUNT; i++) {
      String testFolder = "/production/test_folder_" + i;       
      FtpThreadTest curTest = new FtpThreadTest(testFolder);
      testers.add(curTest);
      curTest.start();
    }
      
    try {
      boolean enable = true;
      while (enable) {
        
        enable = false;
        for (int i = 0; i < testers.size(); i++) {
          FtpThreadTest curTest = testers.get(i);
          if (curTest.isAlive()) {
            enable = true;
          }
        }
        
        Thread.sleep(100);
      }
    } catch (Exception exc) {
      log.info("Unhandled ecxeption. " + exc.getMessage(), exc);
    }
    
    log.info("Complete.");
  }
  
}
