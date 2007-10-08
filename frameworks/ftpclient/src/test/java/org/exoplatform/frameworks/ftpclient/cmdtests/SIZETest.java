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
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdPasv;
import org.exoplatform.frameworks.ftpclient.commands.CmdSize;
import org.exoplatform.frameworks.ftpclient.commands.CmdStor;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SIZETest extends TestCase {
  
  private static Log log = new Log("SIZETest");

  public void testSIZE() throws Exception {
    log.info("Test...");
    
    FtpClientSession client = FtpTestConfig.getTestFtpClient();
    client.connect();

    {
      CmdSize cmdSize = new CmdSize(null);
      assertEquals(FtpConst.Replyes.REPLY_530, client.executeCommand(cmdSize));
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
      CmdSize cmdSize = new CmdSize(null);
      assertEquals(FtpConst.Replyes.REPLY_500, client.executeCommand(cmdSize));
    }
    
    {
      CmdSize cmdSize = new CmdSize("NoSuchFile");
      assertEquals(FtpConst.Replyes.REPLY_550, client.executeCommand(cmdSize));
    }
    
    String filePath = "/production/test_size_file.txt";
    String fileContent = "This test File for SIZE command.";
    int fileSize = fileContent.length();
    
    {
      CmdPasv cmdPasv = new CmdPasv();
      assertEquals(FtpConst.Replyes.REPLY_227, client.executeCommand(cmdPasv));
    }
    
    {
      CmdStor cmdStor = new CmdStor(filePath);
      cmdStor.setFileContent(fileContent.getBytes());
      assertEquals(FtpConst.Replyes.REPLY_226, client.executeCommand(cmdStor));
    }
    
    {
      CmdSize cmdSize = new CmdSize(filePath);
      assertEquals(FtpConst.Replyes.REPLY_213, client.executeCommand(cmdSize));
      assertEquals(fileSize, cmdSize.getSize()); 
    }
    
    {
      CmdDele cmdDele = new CmdDele(filePath);
      assertEquals(FtpConst.Replyes.REPLY_250, client.executeCommand(cmdDele));
    }
    
    client.close();

    log.info("Complete.\r\n");
  }    
  
}

