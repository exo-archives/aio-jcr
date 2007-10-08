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
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdType;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class TYPETest extends TestCase {
  
  private static Log log = new Log("TYPETest");

  public void testTYPE() throws Exception {
    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    {
      CmdType cmdType = new CmdType("");
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdType));
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
  
}

