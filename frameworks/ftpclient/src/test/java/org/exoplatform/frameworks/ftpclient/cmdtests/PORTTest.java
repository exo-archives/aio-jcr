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
import org.exoplatform.frameworks.ftpclient.commands.CmdPort;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PORTTest extends TestCase {
  
  private static Log log = new Log("PORTTest");
  
  public void testPORT() throws Exception {
    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdPort cmdPort = new CmdPort("127.0.0.1", 80);
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdPort));      
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

}
