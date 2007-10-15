/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.ftpclient.cmdtests;

import junit.framework.TestCase;

import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.frameworks.ftpclient.FtpTestConfig;
import org.exoplatform.frameworks.ftpclient.Log;
import org.exoplatform.frameworks.ftpclient.client.FtpClientSession;
import org.exoplatform.frameworks.ftpclient.commands.CmdCwd;
import org.exoplatform.frameworks.ftpclient.commands.CmdDele;
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdPasv;
import org.exoplatform.frameworks.ftpclient.commands.CmdRest;
import org.exoplatform.frameworks.ftpclient.commands.CmdRetr;
import org.exoplatform.frameworks.ftpclient.commands.CmdStor;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RETRTest extends TestCase implements TestConst {
  
  private static Log log = new Log("RETRTest");
  
  public void testRETR() throws Exception {
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

