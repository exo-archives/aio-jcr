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
import org.exoplatform.frameworks.ftpclient.commands.CmdNlst;
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdPasv;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class NLSTTest extends TestCase {
  
  private static Log log = new Log("NLSTTest");
  
  public void testNLST() throws Exception {
    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    {
      CmdNlst cmdNlst = new CmdNlst();
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdNlst));
    }
    
    {
      CmdUser cmdUser = new CmdUser(FtpTestConfig.USER_ID);
      assertEquals(FtpConst.Replyes.REPLY_331, client.executeCommand(cmdUser));
    }
    
    {
      CmdPass cmdPass = new CmdPass(FtpTestConfig.USER_PASS);
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

}

