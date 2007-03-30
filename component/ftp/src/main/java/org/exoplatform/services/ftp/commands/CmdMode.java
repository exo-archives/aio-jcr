/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.ftp.commands;

import java.io.IOException;

import org.exoplatform.services.ftp.FtpConst;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CmdMode extends FtpCommandImpl {
  
  public CmdMode() {
    commandName = FtpConst.Commands.CMD_MODE;
  }
  
  public void run(String []params) throws IOException {    
    if (params.length < 2) {
      reply(String.format(FtpConst.Replyes.REPLY_500_PARAMREQUIRED, FtpConst.Commands.CMD_MODE));
      return;
    }

    if ("S".equals(params[1].toUpperCase())) {
      reply(String.format(FtpConst.Replyes.REPLY_200, "Mode set to S"));
      return;
    }
    
    if ("C".equals(params[1].toUpperCase()) || "B".equals(params[1].toUpperCase())) {
      reply(String.format(FtpConst.Replyes.REPLY_504, FtpConst.Commands.CMD_MODE + " " + params[1].toUpperCase()));
      return;
    }

    reply(String.format(FtpConst.Replyes.REPLY_501_MODE, FtpConst.Commands.CMD_MODE + " " + params[1].toUpperCase()));    
  }
  
}
