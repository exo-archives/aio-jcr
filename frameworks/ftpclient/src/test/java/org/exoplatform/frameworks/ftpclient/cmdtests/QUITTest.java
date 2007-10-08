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
import org.exoplatform.frameworks.ftpclient.commands.CmdQuit;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class QUITTest extends TestCase {
  
  private static Log log = new Log("QUITTest");

  public void test_QUIT() throws Exception {
    log.info("Test...");
    
    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    CmdQuit cmdQuit = new CmdQuit();
    assertEquals(FtpConst.Replyes.REPLY_221, client.executeCommand(cmdQuit));    
    
    client.close();
    log.info("Complete.\r\n");    
  }
  
}
