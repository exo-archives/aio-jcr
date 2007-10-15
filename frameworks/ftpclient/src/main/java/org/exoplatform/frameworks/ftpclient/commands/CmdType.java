/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient.commands;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.services.log.ExoLogger;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public class CmdType extends FtpCommandImpl {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdType");
  
  protected String type;
  
  public CmdType(String type) {
    this.type = type;
  }
  
  public int execute() {
    try {
      sendCommand(String.format("%s %s", FtpConst.Commands.CMD_TYPE, type));
      return getReply();
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }    
    return -1;
  }
  
}
