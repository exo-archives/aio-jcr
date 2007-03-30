/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.commands;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CmdCwd extends FtpCommandImpl {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdCwd");
  
  public CmdCwd() {
    commandName = FtpConst.Commands.CMD_CWD; 
  }
  
  public void run(String []params) throws IOException {
    if (params.length < 2) {
      reply(String.format(FtpConst.Replyes.REPLY_500_PARAMREQUIRED, FtpConst.Commands.CMD_CWD));
      return;
    }
    
    try {
      reply(String.format(clientSession().changePath(params[1]), FtpConst.Commands.CMD_CWD));
      return;
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    reply(String.format(FtpConst.Replyes.REPLY_550, FtpConst.Commands.CMD_CWD));
  }
  
}
