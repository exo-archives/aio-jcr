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
import org.exoplatform.frameworks.ftpclient.commands.CmdHelp;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class HELPTest extends TestCase {
  
  private static Log log = new Log("HELPTest");
  
  public void testHELP() throws Exception {
    log.info("Test...");
    
    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();
    
    CmdHelp cmdHelp = new CmdHelp();
    assertEquals(FtpConst.Replyes.REPLY_214, client.executeCommand(cmdHelp));    
    
    client.close();
    log.info("Complete.\r\n");
  }  

}
