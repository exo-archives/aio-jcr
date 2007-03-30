/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.commands;

import java.io.IOException;

import org.exoplatform.services.ftp.FtpConst;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CmdSyst extends FtpCommandImpl {

  public CmdSyst() {
    commandName = FtpConst.Commands.CMD_SYST; 
  }
  
  public void run(String []params) throws IOException {
    String systemType = clientSession().getFtpServer().getConfiguration().getSystemType();
    reply(String.format(FtpConst.Replyes.REPLY_215, systemType));
  }
  
}
