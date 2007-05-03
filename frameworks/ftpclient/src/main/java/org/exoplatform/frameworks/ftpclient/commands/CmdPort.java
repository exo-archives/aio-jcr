/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient.commands;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.frameworks.ftpclient.FtpConst.Commands;
import org.exoplatform.services.log.ExoLogger;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public class CmdPort extends FtpCommandImpl {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdPort");
  
  protected String host;
  protected int port;
  
  public CmdPort(String host, int port) {
    this.host = host;
    this.port = port;
  }
  
  public int execute() {
    try {

      //this "IF" for tests only. try to get reply 500
      if (host == null) {
        sendCommand(FtpConst.Commands.CMD_PORT);
        return getReply();
      }      
      
      sendCommand(String.format("%s %s,%d,%d", FtpConst.Commands.CMD_PORT, host, port / 256, port % 256).replace('.', ','));
      return getReply();
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }
    return -1;
  }
  
}
