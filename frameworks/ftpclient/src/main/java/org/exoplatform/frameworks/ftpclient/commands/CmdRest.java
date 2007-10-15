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

public class CmdRest extends FtpCommandImpl {
  
  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdRest");
  
  protected String offset; 
  
  public CmdRest(int offset) {
    this.offset = String.format("%d", offset);
  }

  public CmdRest(String offset) {
    this.offset = offset;
  }
  
  public int execute() {
    try {      
      // for tests only
      if (offset == null) {
        sendCommand(FtpConst.Commands.CMD_REST);
        return getReply();
      }
      
      sendCommand(String.format("%s %s", FtpConst.Commands.CMD_REST, offset));
      return getReply();
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }
    return -1;
  }

}
