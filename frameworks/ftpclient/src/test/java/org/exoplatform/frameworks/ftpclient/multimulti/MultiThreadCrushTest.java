/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.ftpclient.multimulti;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.FtpClientSession;
import org.exoplatform.frameworks.ftpclient.FtpClientSessionImpl;
import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.frameworks.ftpclient.commands.CmdCwd;
import org.exoplatform.frameworks.ftpclient.commands.CmdDele;
import org.exoplatform.frameworks.ftpclient.commands.CmdMkd;
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdSyst;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;
import org.exoplatform.services.log.ExoLogger;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MultiThreadCrushTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.MultiThreadCrushTest");
  
  public static final int CLIENTS_COUNT = 500;
  public static final int CLIENT_DEPTH = 2;  
  
  public static final String HOST = "192.168.0.15";
  public static final int PORT = 21;  
  public static final String USER_ID = "admin";
  public static final String USER_PASS = "admin";  
  public static final String TEST_FOLDER = "/production/crash_test2";
  
  public static boolean IsNeedWaitAll = true;
  
  public void testSingleThread() throws Exception {
    log.info("testSingleThread...");
    
    {
      FtpClientSession client = new FtpClientSessionImpl(MultiThreadCrushTest.HOST, MultiThreadCrushTest.PORT);
      client.connect();

      {        
        CmdUser cmdUser = new CmdUser(MultiThreadCrushTest.USER_ID);
        assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
      }
      
      {
        CmdPass cmdPass = new CmdPass(MultiThreadCrushTest.USER_PASS);
        assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
      }
      
      {
        CmdSyst cmdSyst = new CmdSyst();
        assertEquals(FtpConst.Replyes.REPLY_215, client.executeCommand(cmdSyst));
      }

      {
        CmdCwd cmdCwd = new CmdCwd(TEST_FOLDER);
        if (FtpConst.Replyes.REPLY_550 == client.executeCommand(cmdCwd)) {
          CmdMkd cmdMkd = new CmdMkd(TEST_FOLDER);
          assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(cmdMkd));          

          cmdCwd = new CmdCwd(TEST_FOLDER);
          assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdCwd));
        }
        
        for (int i1 = 0; i1 < 10; i1++) {

          String folderName = TEST_FOLDER + "/" + i1;          
          assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(new CmdMkd(folderName)));          
          
          for (int i2 = 0; i2 < 10; i2++) {
            String testSubFolder = folderName + "/" + i2;
            assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(new CmdMkd(testSubFolder)));          
          }
          
        }
      }
      
      client.close();
    }
    
    HashMap<Integer, TestAgent> clients = new HashMap<Integer, TestAgent>();
    int count = 0;
    
    Random random = new Random();
    
    while (count < CLIENTS_COUNT) {
      int nextId = random.nextInt(Integer.MAX_VALUE);
      log.info("NEXT ID: [" + nextId + "]");
      
      if (nextId < 10000) {
        continue;
      }
      
      Integer curInteger = new Integer(nextId);
      if (clients.containsKey(curInteger)) {
        continue;
      }
         
      TestAgent testAgent = new TestAgent(curInteger, CLIENT_DEPTH);
      clients.put(curInteger, testAgent);
      count++;
    }
    
    log.info("CLIENTS: [" + clients.size() + "]");
    
    Thread.sleep(3000);
    log.info("START ALL!!!!!!!!!!!!!!!!!!!!!!!!");
    IsNeedWaitAll = false;
    Thread.sleep(3000);
    
    {
      boolean alive = true;
      
      while (alive) {
        alive = false;
        Thread.sleep(2000);
        
        int live = 0;
        
        Iterator<Integer> keyIter = clients.keySet().iterator();
        while (keyIter.hasNext()) {          
          Integer agentKey = keyIter.next();
          TestAgent agent = clients.get(agentKey);
          if (agent.isAlive()) {
            alive = true;
            live++;
          }
        }
        
        log.info(">>>>>>>>>>>>> LIVE: [" + live + "]");
        
      }      
    }  

    {
      {
        FtpClientSession client = new FtpClientSessionImpl(MultiThreadCrushTest.HOST, MultiThreadCrushTest.PORT);
        client.connect();

        {        
          CmdUser cmdUser = new CmdUser(MultiThreadCrushTest.USER_ID);
          assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
        }
        
        {
          CmdPass cmdPass = new CmdPass(MultiThreadCrushTest.USER_PASS);
          assertEquals(FtpConst.Replyes.REPLY_230, client.executeCommand(cmdPass));
        }
        
        {
          CmdSyst cmdSyst = new CmdSyst();
          assertEquals(FtpConst.Replyes.REPLY_215, client.executeCommand(cmdSyst));
        }
        
        assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(new CmdDele(TEST_FOLDER)));
      }
    }
    
    {
      int successed = 0;
      Iterator<Integer> keyIter = clients.keySet().iterator();
      while (keyIter.hasNext()) {
        Integer agentKey = keyIter.next();
        TestAgent agent = clients.get(agentKey);
        
        if (agent.isSuccessed()) {
          successed++;
        }
        
      }
      
      log.info("SUCCESSED: [" + successed + "]");
      log.info("FAILURES: [" + (CLIENTS_COUNT - successed) + "]");
      Thread.sleep(2000);
    }
    
    
    log.info("done.");
  }
  
}
