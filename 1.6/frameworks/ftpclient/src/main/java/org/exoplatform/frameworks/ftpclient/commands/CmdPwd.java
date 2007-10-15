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

public class CmdPwd extends FtpCommandImpl {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdPwd");
  
  public int execute() {
    try {
      sendCommand(FtpConst.Commands.CMD_PWD);
      return getReply();
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }
    return -1;
  }
  
  public String getCurrentPath() {    
    String pVal =  getDescription();
    
    if (pVal.indexOf(" \"") != 0) {
      pVal = pVal.substring(pVal.indexOf(" \"") + 2, pVal.lastIndexOf("\" "));
    }

    return pVal;
  }
  
}
