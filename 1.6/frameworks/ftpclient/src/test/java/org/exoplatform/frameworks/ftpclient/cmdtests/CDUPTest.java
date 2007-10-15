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
import org.exoplatform.frameworks.ftpclient.commands.CmdCdUp;
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CDUPTest extends TestCase implements TestConst {
  
  private static Log log = new Log("CDUPTest");
  
  public void testCDUP() throws Exception {
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

}

