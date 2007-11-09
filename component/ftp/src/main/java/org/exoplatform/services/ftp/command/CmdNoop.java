/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.command;

import java.io.IOException;

import org.exoplatform.services.ftp.FtpConst;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CmdNoop extends FtpCommandImpl {
  
  public CmdNoop() {
    commandName = FtpConst.Commands.CMD_NOOP; 
    isNeedLogin = false;
  }

  public void run(String []params) throws IOException {
    reply(String.format(FtpConst.Replyes.REPLY_200, FtpConst.Commands.CMD_NOOP + " command successful"));
  }
  
}
