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
import org.exoplatform.frameworks.ftpclient.commands.CmdDele;
import org.exoplatform.frameworks.ftpclient.commands.CmdMkd;
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdRnFr;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RNFRTest extends TestCase {
  
  private static Log log = new Log("RNFRTest");
  
  public void testRNFR() throws Exception {
    log.info("Test...");

    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdRnFr cmdRnFr = new CmdRnFr(null);
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdRnFr));
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
      CmdRnFr cmdRnFr = new CmdRnFr(null);
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdRnFr));
    }
    
    {
      CmdRnFr cmdRnFr = new CmdRnFr("NotExistFolder");
      assertEquals(FtpConst.Replyes.REPLY_550, client.executeCommand(cmdRnFr));
    }
    
    String folderName = "/production/folder_to_rename";
    
    {
      CmdMkd cmdMkd = new CmdMkd(folderName);
      assertEquals(FtpConst.Replyes.REPLY_257, client.executeCommand(cmdMkd));
    }
    
    {
      CmdRnFr cmdRnFr = new CmdRnFr(folderName);
      assertEquals(FtpConst.Replyes.REPLY_350, client.executeCommand(cmdRnFr));
    }
    
    {
      CmdDele cmdDele = new CmdDele(folderName);
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdDele));      
    }      
    
    client.close();

    log.info("Complete.\r\n");
  }    

}

