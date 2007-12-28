/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.ftp.command;

import java.io.IOException;

import org.exoplatform.services.ftp.FtpConst;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CmdStru extends FtpCommandImpl {
  
  public CmdStru() {
    commandName = FtpConst.Commands.CMD_STRU;
  }
  
  public void run(String []params) throws IOException {
    if (params.length < 2) {
      reply(String.format(FtpConst.Replyes.REPLY_500_PARAMREQUIRED, FtpConst.Commands.CMD_STRU));
      return;
    }

    if ("F".equals(params[1].toUpperCase())) {
      reply(String.format(FtpConst.Replyes.REPLY_200, "Structure set to F"));
      return;
    }

    reply(String.format(FtpConst.Replyes.REPLY_501_STRU, FtpConst.Commands.CMD_STRU + " " + params[1].toUpperCase()));
  }

}
