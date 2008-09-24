/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.ftpclient.commands;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * 
 * @version $Id: $
 */

public class CmdQuit extends FtpCommandImpl {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdQuit");

  public int execute() {
    try {
      sendCommand(FtpConst.Commands.CMD_QUIT);
      return getReply();
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }
    return -1;
  }

}
