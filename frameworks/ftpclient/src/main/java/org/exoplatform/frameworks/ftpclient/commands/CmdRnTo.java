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

public class CmdRnTo extends FtpCommandImpl {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdRnTo"); 
  
  protected String path; 
  
  public CmdRnTo(String path) {
    this.path = path;
  }
  
  public int execute() {
    try {
      // for tests only
      if (path == null) {
        sendCommand(FtpConst.Commands.CMD_RNTO);
        return getReply();
      }
      
      sendCommand(String.format("%s %s", FtpConst.Commands.CMD_RNTO, path));
      return getReply();
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }
    return -1;
  }
  
}
