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
import org.exoplatform.frameworks.ftpclient.commands.CmdMode;
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MODETest extends TestCase implements TestConst {

  private static Log log = new Log("MODETest");
  
  public void testMODE() throws Exception {
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
  
  
}

