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
import org.exoplatform.frameworks.ftpclient.commands.CmdMkd;
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdRnFr;
import org.exoplatform.frameworks.ftpclient.commands.CmdRnTo;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RNTOTest extends TestCase implements TestConst {
  
  private static Log log = new Log("RNTOTest");
  
  public void testRNTO() throws Exception {
    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    {
      CmdRnTo cmdRnTo = new CmdRnTo(null);
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdRnTo));
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
      CmdRnTo cmdRnTo = new CmdRnTo(null);
      assertEquals(FtpConst.Replyes.REPLY_503, client.executeCommand(cmdRnTo));
    }
    
    {
      CmdCwd cmdCwd = new CmdCwd("production");
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdCwd));
    }
    
    String folder_from = "FOLDER_FROM_" + System.currentTimeMillis();
    String folder_to = "FOLDER_TO_" + System.currentTimeMillis();
    String folder_existed = "FOLDER_EXISTED_" + System.currentTimeMillis();
    
    {
      CmdMkd cmdMkd = new CmdMkd(folder_from);
      assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(cmdMkd));
    }
    
    {
      CmdRnFr cmdRnFr = new CmdRnFr(folder_from);
      assertEquals(FtpConst.Replyes.REPLY_350, client.executeCommand(cmdRnFr));
    }
    
    {
      CmdRnTo cmdRnTo = new CmdRnTo(null);
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdRnTo));
    }
    
    {
      CmdMkd cmdMkd = new CmdMkd(folder_existed);
      assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(cmdMkd));      
    }
    
    {
      CmdRnFr cmdRnFr = new CmdRnFr(folder_from);
      assertEquals(FtpConst.Replyes.REPLY_350, client.executeCommand(cmdRnFr));
    }
    
    {
      CmdRnTo cmdRnTo = new CmdRnTo(folder_existed);
      assertEquals(FtpConst.Replyes.REPLY_553, client.executeCommand(cmdRnTo));
    }
    
    {
      CmdRnFr cmdRnFr = new CmdRnFr(folder_from);
      assertEquals(FtpConst.Replyes.REPLY_350, client.executeCommand(cmdRnFr));
    }

    {
      CmdRnTo cmdRnTo = new CmdRnTo(folder_to);
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdRnTo));      
    }
    
    {
      CmdDele cmdDele = new CmdDele(folder_to);
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdDele));
    }
    
    {
      CmdDele cmdDele = new CmdDele(folder_existed);
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdDele));
    }    
    
    client.close();

    log.info("Complete.\r\n");
  }  

}
