/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.ftpclient;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.commands.CmdCdUp;
import org.exoplatform.frameworks.ftpclient.commands.CmdCwd;
import org.exoplatform.frameworks.ftpclient.commands.CmdDele;
import org.exoplatform.frameworks.ftpclient.commands.CmdHelp;
import org.exoplatform.frameworks.ftpclient.commands.CmdList;
import org.exoplatform.frameworks.ftpclient.commands.CmdMkd;
import org.exoplatform.frameworks.ftpclient.commands.CmdMode;
import org.exoplatform.frameworks.ftpclient.commands.CmdNlst;
import org.exoplatform.frameworks.ftpclient.commands.CmdNoop;
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdPasv;
import org.exoplatform.frameworks.ftpclient.commands.CmdPort;
import org.exoplatform.frameworks.ftpclient.commands.CmdPwd;
import org.exoplatform.frameworks.ftpclient.commands.CmdQuit;
import org.exoplatform.frameworks.ftpclient.commands.CmdRest;
import org.exoplatform.frameworks.ftpclient.commands.CmdRetr;
import org.exoplatform.frameworks.ftpclient.commands.CmdRmd;
import org.exoplatform.frameworks.ftpclient.commands.CmdRnFr;
import org.exoplatform.frameworks.ftpclient.commands.CmdRnTo;
import org.exoplatform.frameworks.ftpclient.commands.CmdSize;
import org.exoplatform.frameworks.ftpclient.commands.CmdStat;
import org.exoplatform.frameworks.ftpclient.commands.CmdStor;
import org.exoplatform.frameworks.ftpclient.commands.CmdStru;
import org.exoplatform.frameworks.ftpclient.commands.CmdSyst;
import org.exoplatform.frameworks.ftpclient.commands.CmdType;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class eXoFtpDetailTest extends TestCase {

  public static final String USER_ID = "admin";
  public static final String USER_PASS = "admin";
  public static final String TEST_WORKSPACE = "production";
  
  public void testUSER_PASS() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testUSER_PASS()");
    
    log.info("Test...");
    
    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdUser cmdUser = new CmdUser("");
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass("");
      assertEquals(FtpConst.Replyes.REPLY_503, client.executeCommand(cmdPass));
    }
    
    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass("");
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdPass));
    }

    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }
    
    client.close();
    log.info("Complete.\r\n");    
  }
  
  public void testHELP() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testHELP()");
    
    log.info("Test...");
    
    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    CmdHelp cmdHelp = new CmdHelp();
    assertEquals(FtpConst.Replyes.REPLY_214, client.executeCommand(cmdHelp));    
    
    client.close();
    log.info("Complete.\r\n");
  }
  
  public void testNOOP() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testNOOP()");
    
    log.info("Test...");
    
    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    CmdNoop cmdNoop = new CmdNoop();
    assertEquals(FtpConst.Replyes.REPLY_200, client.executeCommand(cmdNoop));
    
    client.close();
    log.info("Complete.\r\n");
  }
  
  public void test_QUIT() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.test_QUIT()");
    
    log.info("Test...");
    
    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    CmdQuit cmdQuit = new CmdQuit();
    assertEquals(FtpConst.Replyes.REPLY_221, client.executeCommand(cmdQuit));    
    
    client.close();
    log.info("Complete.\r\n");    
  }
  
  public void testMODE() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testMODE()");
    
    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }

    // param required
    {
      CmdMode cmdMode = new CmdMode("");
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdMode));
    }

    // Mode set to S
    
    {
      CmdMode cmdMode = new CmdMode("s");
      assertEquals(FtpConst.Replyes.REPLY_200, client.executeCommand(cmdMode));
    }
    
    // unsupported modes - c, b

    {
      CmdMode cmdMode = new CmdMode("c");
      assertEquals(FtpConst.Replyes.REPLY_504, client.executeCommand(cmdMode));
    }

    {
      CmdMode cmdMode = new CmdMode("b");
      assertEquals(FtpConst.Replyes.REPLY_504, client.executeCommand(cmdMode));
    } 
    
    // unrecognized modes

    {
      CmdMode cmdMode = new CmdMode("a");
      assertEquals(FtpConst.Replyes.REPLY_501, client.executeCommand(cmdMode));
    }
        
    client.close();
    log.info("Complete.\r\n");    
  }
  
  public void testTYPE() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testTYPE()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    {
      CmdType cmdType = new CmdType("");
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdType));
    }

    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }
    
    {
      CmdType cmdType = new CmdType("a");      
      assertEquals(FtpConst.Replyes.REPLY_200, client.executeCommand(cmdType));
    }

    {
      CmdType cmdType = new CmdType("i");      
      assertEquals(FtpConst.Replyes.REPLY_200, client.executeCommand(cmdType));
    }
    
    {
      CmdType cmdType = new CmdType("any");      
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdType));
    }

    {
      CmdType cmdType = new CmdType("");      
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdType));
    }    
    
    client.close();
    log.info("Complete.\r\n");    
  }
  
  public void testSYST() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testSYST()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdSyst cmdSyst = new CmdSyst();
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdSyst));
    }

    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }

    {
      CmdSyst cmdSyst = new CmdSyst();
      assertEquals(FtpConst.Replyes.REPLY_215, client.executeCommand(cmdSyst));
    }    
    
    client.close();
    log.info("Complete.\r\n");        
  }  
  
  public void testSTRU() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testSTRU()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdStru cmdStru = new CmdStru("");
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdStru));
    }    
    
    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }
    
    {
      CmdStru cmdStru = new CmdStru("");
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdStru));
    }

    {
      CmdStru cmdStru = new CmdStru("f");
      assertEquals(FtpConst.Replyes.REPLY_200, client.executeCommand(cmdStru));
    }

    {
      CmdStru cmdStru = new CmdStru("any");
      assertEquals(FtpConst.Replyes.REPLY_501, client.executeCommand(cmdStru));
    }
    
    client.close();
    log.info("Complete.\r\n");        
  }
  
  public void testSTAT() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testSTAT()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdStat cmdStat = new CmdStat();
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdStat));
    }
    
    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }
    
    {
      CmdStat cmdStat = new CmdStat();
      assertEquals(FtpConst.Replyes.REPLY_211, client.executeCommand(cmdStat));
    }
    
    client.close();
    log.info("Complete.\r\n");            
  }
  
  public void testPWD() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testPWD()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdPwd cmdPwd = new CmdPwd();
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdPwd));
    }

    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }

    {
      CmdPwd cmdPwd = new CmdPwd();
      assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(cmdPwd));
    }    
    
    client.close();
    log.info("Complete.\r\n");                
  }
  
  public void testCWD() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testCWD()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdCwd cmdCwd = new CmdCwd("");
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdCwd));
    }

    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }
    
    {
      CmdCwd cmdCwd = new CmdCwd("");
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdCwd));      
    }
    
    {
      CmdCwd cmdCwd = new CmdCwd("NotExistFolder");
      assertEquals(FtpConst.Replyes.REPLY_550, client.executeCommand(cmdCwd));      
    }
    
    {
      CmdCwd cmdCwd = new CmdCwd("production");
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdCwd));      
    }
    
    client.close();
    log.info("Complete.\r\n");                
  }

  public void testCDUP() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testCDUP()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdCdUp cmdCdUp = new CmdCdUp();
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdCdUp));
    }

    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }

    {
      CmdCdUp cmdCdUp = new CmdCdUp();
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdCdUp));
    }
    
    client.close();
    log.info("Complete.\r\n");                
  }

  public void testMKD() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testMKD()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdMkd cmdMkd = new CmdMkd("");
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdMkd));
    }

    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }

    {
      CmdMkd cmdMkd = new CmdMkd("");
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdMkd));
    }
    
    {
      CmdMkd cmdMkd = new CmdMkd("myfolder");
      assertEquals(FtpConst.Replyes.REPLY_550, client.executeCommand(cmdMkd));
    }
    
    {
      CmdCwd cmdCwd = new CmdCwd("production");
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdCwd));
      
      String folderName = "test_folder_" + System.currentTimeMillis();
      
      CmdMkd cmdMkd = new CmdMkd(folderName);
      assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(cmdMkd));
      
      CmdRmd cmdRmd = new CmdRmd(folderName);
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdRmd));
    }
    
    client.close();
    log.info("Complete.\r\n");                
  }

  public void testRMD() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testRMD()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdRmd cmdRmd = new CmdRmd("");
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdRmd));
    }
    
    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }

    {
      CmdRmd cmdRmd = new CmdRmd("");
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdRmd));
    }

    {
      CmdRmd cmdRmd = new CmdRmd("NotexistFolder");
      assertEquals(FtpConst.Replyes.REPLY_550, client.executeCommand(cmdRmd));
    }
    
    {      
      CmdCwd cmdCwd = new CmdCwd("production");
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdCwd));
      
      String folderName = "test_folder_" + System.currentTimeMillis();
      
      CmdMkd cmdMkd = new CmdMkd(folderName);
      assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(cmdMkd));
      
      CmdRmd cmdRmd = new CmdRmd(folderName);
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdRmd));
    }
    
    client.close();
    log.info("Complete.\r\n");                
  }

  public void testDELE() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testDELE()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    {
      CmdDele cmdDele = new CmdDele("");
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdDele));
    }
    
    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }

    {
      CmdDele cmdDele = new CmdDele("");
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdDele));
    }    
    
    {
      CmdDele cmdDele = new CmdDele("NotExistFolder");
      assertEquals(FtpConst.Replyes.REPLY_550, client.executeCommand(cmdDele));
    }

    {
      CmdCwd cmdCwd = new CmdCwd("production");
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdCwd));
    }
    
    String folderName = "testFolder_" + System.currentTimeMillis();
    
    {
      CmdMkd cmdMkd = new CmdMkd(folderName);
      assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(cmdMkd));
    }
    
    {
      CmdDele cmdDele = new CmdDele(folderName);
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdDele));      
    }
    

    client.close();
    log.info("Complete.\r\n");                
  }

  public void testPASV() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testPASV()");

    log.info("Test...");

    {
      FtpClientSession client = FtpTestConfig.getTestFtpClient();
      client.connect();

      {
        CmdPasv cmdPasv = new CmdPasv();
        assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdPasv));        
      }
      
      {
        CmdUser cmdUser = new CmdUser(USER_ID);
        assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
      }
      
      {
        CmdPass cmdPass = new CmdPass(USER_PASS);
        assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
      }

      {
        CmdPasv cmdPasv = new CmdPasv();
        assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(cmdPasv));        
      }
      
      log.info("Waiting for connection...");
      
      while (true) {
        if (client.getDataTransiver().isConnected()) {
          break;
        }
        Thread.sleep(1);
      }
      
      log.info("Connected.");
      
      client.close();
    }
    
//    // NOW try to get all available channels
//    // then server reply 421
//    {
//      
//      ArrayList<FtpClientSession> clients = new ArrayList<FtpClientSession>();
//
//      // configuration params
//      int FTP_MIN_PORT = 32000;
//      int FTP_MAX_PORT = 32100;
//      
//      int CLIENTS_COUNT = FTP_MAX_PORT - FTP_MIN_PORT + 2;
//      
//      log.info("Starting CRUSH test...");
//      log.info("CLIENTS COUNT - " + CLIENTS_COUNT);
//      Thread.sleep(2000);
//      log.info("Started...");
//      
//      for (int i = 0; i < CLIENTS_COUNT; i++) {
//        FtpClientSession curClient = FtpTestConfig.getTestFtpClient();
//        curClient.connect();
//        
//        clients.add(curClient);
//
//        {
//          CmdUser cmdUser = new CmdUser(USER_ID);
//          assertEquals(FtpConst.Replyes.REPLY_331, curClient.executeCommand(cmdUser));
//        }
//        
//        {
//          CmdPass cmdPass = new CmdPass(USER_PASS);
//          assertEquals(FtpConst.Replyes.REPLY_230, curClient.executeCommand(cmdPass));
//        }        
//        
//        CmdPasv cmdPasv = new CmdPasv();
//        
//        if (i == CLIENTS_COUNT - 1) {
//          assertEquals(FtpConst.Replyes.REPLY_421, curClient.executeCommand(cmdPasv));          
//        } else {
//          assertEquals(FtpConst.Replyes.REPLY_227, curClient.executeCommand(cmdPasv));
//        }
//        
//      }
//      
//      for (int i = 0; i < CLIENTS_COUNT; i++) {
//        FtpClientSession curClient = clients.get(i);
//        curClient.close();
//      }
//
//    }
    
    log.info("Complete.\r\n");                
  }
  
  public void testPORT() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testPORT()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdPort cmdPort = new CmdPort("127.0.0.1", 80);
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdPort));      
    }
    
    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }

    {
      CmdPort cmdPort = new CmdPort(null, 80);
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdPort));            
    }
    
    {
      CmdPort cmdPort = new CmdPort("invalidhost", 80);
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdPort));            
    }

    {
      CmdPort cmdPort = new CmdPort("127,0,0,1", 80);
      assertEquals(FtpConst.Replyes.REPLY_200, client.executeCommand(cmdPort));            
    }    
    
    client.close();
    
    log.info("Complete.\r\n");
  }

  public void testLIST() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testLIST()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    {
      CmdList cmdList = new CmdList();
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdList));
    }
    
    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }
    
    {
      CmdList cmdList = new CmdList();
      assertEquals(FtpConst.Replyes.REPLY_425, client.executeCommand(cmdList));      
    }

    {
      {
        CmdPasv cmdPasv = new CmdPasv();
        assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(cmdPasv));
      }

      {
        CmdList cmdList = new CmdList("NotExistFolder");
        assertEquals(FtpConst.Replyes.REPLY_450, client.executeCommand(cmdList));      
      }          
    }
    
    {
      {
        CmdPasv cmdPasv = new CmdPasv();
        assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(cmdPasv));
      }

      // Normal executing replies sequence 125..226
      // 125 used in List command inside
      {
        CmdList cmdList = new CmdList();
        assertEquals(FtpConst.Replyes.REPLY_226, client.executeCommand(cmdList));      
      }          
    }
    
    client.close();
    
    log.info("Complete.\r\n");
  }

  public void testNLST() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testNLST()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    {
      CmdNlst cmdNlst = new CmdNlst();
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdNlst));
    }
    
    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }
    
    {
      CmdNlst cmdNlst = new CmdNlst();
      assertEquals(FtpConst.Replyes.REPLY_425, client.executeCommand(cmdNlst));      
    }

    {
      {
        CmdPasv cmdPasv = new CmdPasv();
        assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(cmdPasv));
      }
      
      {
        CmdNlst cmdNlst = new CmdNlst("NotExistFolder");
        assertEquals(FtpConst.Replyes.REPLY_450, client.executeCommand(cmdNlst));      
      }    
    }
    
    {
      {
        CmdPasv cmdPasv = new CmdPasv();
        assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(cmdPasv));
      }
      
      // Normal executing replies sequence 125..226
      // 125 used in NLst command inside
      {
        CmdNlst cmdNlst = new CmdNlst();
        assertEquals(FtpConst.Replyes.REPLY_226, client.executeCommand(cmdNlst));      
      }    
    }
    
    client.close();
    
    log.info("Complete.\r\n");
  }
  
  public void testSIZE() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testSIZE()");

    log.info("Test...");
    
    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdSize cmdSize = new CmdSize(null);
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdSize));
    }
    
    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }
    
    {
      CmdSize cmdSize = new CmdSize(null);
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdSize));
    }
    
    {
      CmdSize cmdSize = new CmdSize("NoSuchFile");
      assertEquals(FtpConst.Replyes.REPLY_550, client.executeCommand(cmdSize));
    }
    
    String filePath = "/production/test_size_file.txt";
    String fileContent = "This test File for SIZE command.";
    int fileSize = fileContent.length();
    
    {
      CmdPasv cmdPasv = new CmdPasv();
      assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(cmdPasv));
    }
    
    {
      CmdStor cmdStor = new CmdStor(filePath);
      cmdStor.setFileContent(fileContent.getBytes());
      assertEquals(FtpConst.Replyes.REPLY_226, client.executeCommand(cmdStor));
    }
    
    {
      CmdSize cmdSize = new CmdSize(filePath);
      assertEquals(FtpConst.Replyes.REPLY_213, client.executeCommand(cmdSize));
      assertEquals(fileSize, cmdSize.getSize()); 
    }
    
    {
      CmdDele cmdDele = new CmdDele(filePath);
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdDele));
    }
    
    client.close();

    log.info("Complete.\r\n");
  }  

    public void testRNFR() throws Exception {
      Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testRNFR()");

      log.info("Test...");

      FtpClientSession client = FtpTestConfig.getTestFtpClient();
      client.connect();
  
      {
        CmdRnFr cmdRnFr = new CmdRnFr(null);
        assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdRnFr));
      }
      
      {
        CmdUser cmdUser = new CmdUser(USER_ID);
        assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
      }
      
      {
        CmdPass cmdPass = new CmdPass(USER_PASS);
        assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
      }
      
      {
        CmdRnFr cmdRnFr = new CmdRnFr(null);
        assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdRnFr));
      }
      
      {
        CmdRnFr cmdRnFr = new CmdRnFr("NotExistFolder");
        assertEquals(FtpConst.Replyes.REPLY_550, client.executeCommand(cmdRnFr));
      }
      
      String folderName = "/production/folder_to_rename";
      
      {
        CmdMkd cmdMkd = new CmdMkd(folderName);
        assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(cmdMkd));
      }
      
      {
        CmdRnFr cmdRnFr = new CmdRnFr(folderName);
        assertEquals(FtpConst.Replyes.REPLY_350, client.executeCommand(cmdRnFr));
      }
      
      {
        CmdDele cmdDele = new CmdDele(folderName);
        assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdDele));      
      }      
      
      client.close();

      log.info("Complete.\r\n");
    }  

  public void testRNTO() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testRNTO()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    {
      CmdRnTo cmdRnTo = new CmdRnTo(null);
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdRnTo));
    }
    
    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }

    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }
    
    {
      CmdRnTo cmdRnTo = new CmdRnTo(null);
      assertEquals(FtpConst.Replyes.REPLY_503, client.executeCommand(cmdRnTo));
    }
    
    {
      CmdCwd cmdCwd = new CmdCwd("production");
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdCwd));
    }
    
    String folder_from = "FOLDER_FROM_" + System.currentTimeMillis();
    String folder_to = "FOLDER_TO_" + System.currentTimeMillis();
    String folder_existed = "FOLDER_EXISTED_" + System.currentTimeMillis();
    
    {
      CmdMkd cmdMkd = new CmdMkd(folder_from);
      assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(cmdMkd));
    }
    
    {
      CmdRnFr cmdRnFr = new CmdRnFr(folder_from);
      assertEquals(FtpConst.Replyes.REPLY_350, client.executeCommand(cmdRnFr));
    }
    
    {
      CmdRnTo cmdRnTo = new CmdRnTo(null);
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdRnTo));
    }
    
    {
      CmdMkd cmdMkd = new CmdMkd(folder_existed);
      assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(cmdMkd));      
    }
    
    {
      CmdRnFr cmdRnFr = new CmdRnFr(folder_from);
      assertEquals(FtpConst.Replyes.REPLY_350, client.executeCommand(cmdRnFr));
    }
    
    {
      CmdRnTo cmdRnTo = new CmdRnTo(folder_existed);
      assertEquals(FtpConst.Replyes.REPLY_553, client.executeCommand(cmdRnTo));
    }
    
    {
      CmdRnFr cmdRnFr = new CmdRnFr(folder_from);
      assertEquals(FtpConst.Replyes.REPLY_350, client.executeCommand(cmdRnFr));
    }

    {
      CmdRnTo cmdRnTo = new CmdRnTo(folder_to);
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdRnTo));      
    }
    
    {
      CmdDele cmdDele = new CmdDele(folder_to);
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdDele));
    }
    
    {
      CmdDele cmdDele = new CmdDele(folder_existed);
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdDele));
    }    
    
    client.close();

    log.info("Complete.\r\n");
  }  

  public void testREST() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testREST()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    {
      CmdRest cmdRest = new CmdRest(-1);
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdRest));
    }
    
    {
      CmdUser cmdUser = new CmdUser(USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(USER_PASS);
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
    }
  
    {
      CmdRest cmdRest = new CmdRest(null);
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdRest));
    }
    
    {
      CmdRest cmdRest = new CmdRest("notoffset");
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdRest));
    }

    {
      CmdRest cmdRest = new CmdRest("100");
      assertEquals(FtpConst.Replyes.REPLY_350, client.executeCommand(cmdRest));      
    }

    {
      CmdRest cmdRest = new CmdRest(200);
      assertEquals(FtpConst.Replyes.REPLY_350, client.executeCommand(cmdRest));      
    }    
    
    client.close();
    log.info("Complete.\r\n");
  }  

  public void testSTOR() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testSTOR()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    byte []fileContent = "THIS FILE CONTENT".getBytes();
  
    // desired reply - 530 Please login with USER and PASS
    {
      CmdStor cmdStor = new CmdStor(null);
      cmdStor.setFileContent(fileContent);
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdStor));
    }

    // login
    {
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(new CmdUser(USER_ID)));
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(new CmdPass(USER_PASS)));
    }

    // desired reply - 425 Unable to build data connection
    {
      CmdStor cmdStor = new CmdStor(null);
      cmdStor.setFileContent(fileContent);
      assertEquals(FtpConst.Replyes.REPLY_425, client.executeCommand(cmdStor));
    }
    
    // desired reply - 500 STOR: command requires a parameter
    {
      assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(new CmdPasv()));
      
      CmdStor cmdStor = new CmdStor(null);
      cmdStor.setFileContent(fileContent);
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdStor));
    }
        
    String fileName = "test_stor_file_" + System.currentTimeMillis() + ".txt";
    
    // desired reply - 550 test_stor_file.txt: Permission denied
    {
      assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(new CmdPasv()));
      
      CmdStor cmdStor = new CmdStor(fileName);
      cmdStor.setFileContent(fileContent);
      assertEquals(FtpConst.Replyes.REPLY_550, client.executeCommand(cmdStor));
    }
    
    // desired reply - 125 Data connection already open; Transfer starting
    //                 226 Transfer complete
    {
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(new CmdCwd("production")));
      assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(new CmdPasv()));
      
      CmdStor cmdStor = new CmdStor(fileName);
      cmdStor.setFileContent(fileContent);
      assertEquals(FtpConst.Replyes.REPLY_226, client.executeCommand(cmdStor));
    }
    
    // desired reply - 550 Restore value invalid
    {
      assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(new CmdPasv()));
      assertEquals(FtpConst.Replyes.REPLY_350, client.executeCommand(new CmdRest(1000)));            
      
      CmdStor cmdStor = new CmdStor(fileName);
      cmdStor.setFileContent(fileContent);
      assertEquals(FtpConst.Replyes.REPLY_550, client.executeCommand(cmdStor));
    }
    
    {
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(new CmdDele(fileName)));
    }
    
    client.close();
    log.info("Complete.\r\n");
  }  

  public void testRETR() throws Exception {
    Log log = ExoLogger.getLogger("exo.ftpclient.eXoFtpDetailTest.testRETR()");

    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    // desired reply - 530 Please login with USER and PASS
    {
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(new CmdRetr(null)));
    }
    
    // login
    {
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(new CmdUser(USER_ID)));
      assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(new CmdPass(USER_PASS)));                
    }
    
    // desired reply - 425 Unable to build data connection
    {
      assertEquals(FtpConst.Replyes.REPLY_425, client.executeCommand(new CmdRetr(null)));
    }      

    // desired reply - 500 RETR: command requires a parameter
    {
      assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(new CmdPasv()));        
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(new CmdRetr(null)));
    }
    
    String fileName = "test_file_" + System.currentTimeMillis() + ".txt";
    byte []fileContent = "THIS FILE CONTENT".getBytes();
    
    // desired reply - 550 $: Permission denied      
    {
      assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(new CmdPasv()));
      assertEquals(FtpConst.Replyes.REPLY_550, client.executeCommand(new CmdRetr(fileName)));
    }
          
    {
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(new CmdCwd("production")));
      assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(new CmdPasv()));
      
      CmdStor cmdStor = new CmdStor(fileName);
      cmdStor.setFileContent(fileContent);
      assertEquals(FtpConst.Replyes.REPLY_226, client.executeCommand(cmdStor));
    }
    
    // desired reply - 125 Data connection already open; Transfer starting
    //                 226 Transfer complete
    {
      assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(new CmdPasv()));
      CmdRetr cmdRetr = new CmdRetr(fileName);
      assertEquals(FtpConst.Replyes.REPLY_226, client.executeCommand(cmdRetr));
    }
    
    // desired reply - 550 Restore value invalid
    {
      assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(new CmdPasv()));
      assertEquals(FtpConst.Replyes.REPLY_350, client.executeCommand(new CmdRest(100000)));
      
      CmdRetr cmdRetr = new CmdRetr(fileName);
      assertEquals(FtpConst.Replyes.REPLY_550, client.executeCommand(cmdRetr));
    }
    
    {
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(new CmdDele(fileName)));
    }
    
    client.close();
    log.info("Complete.\r\n");
  }  
  
}
