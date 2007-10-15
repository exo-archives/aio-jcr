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
import org.exoplatform.frameworks.ftpclient.commands.CmdList;
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdPasv;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class LISTTest extends TestCase implements TestConst {
  
  private static Log log = new Log("LISTTest");
  
  public void testLIST() throws Exception {
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

}

