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
import org.exoplatform.frameworks.ftpclient.commands.CmdRest;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RESTTest extends TestCase implements TestConst {
  
  private static Log log = new Log("RESTTest");
  
  public void testREST() throws Exception {
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

}
