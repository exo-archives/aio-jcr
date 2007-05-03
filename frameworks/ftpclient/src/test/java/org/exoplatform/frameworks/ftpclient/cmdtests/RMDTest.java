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
import org.exoplatform.frameworks.ftpclient.commands.CmdMkd;
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdRmd;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RMDTest extends TestCase implements TestConst {
  
  private static Log log = new Log("RMDTest");
  
  public void testRMD() throws Exception {
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
  

}

