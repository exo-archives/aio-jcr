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

public class CmdStat extends FtpCommandImpl {
  
  public CmdStat() {
    commandName = FtpConst.Commands.CMD_STAT;
  }
  
  public static final String []eXoStatInfo = {
    "211-",
    "",
    "     _/_/_/  _/_/_/  _/_/_/      _/_/_/  _/_/_/  _/_/   _/_/_/",
    "    _/        _/    _/   _/     _/        _/   _/   _/   _/   ",
    "   _/_/      _/    _/_/_/        _/      _/   _/ _/_/   _/    ",
    "  _/        _/    _/         _/_/_/     _/   _/   _/   _/     ",
    "  ____________________________________________________________",
    "  Connected from: [127.0.0.1]",
    "  Logged in as: [admin]",
    "  TYPE: ASCII",
    "  STRUcture: File",
    "  MODE: Stream",
    "  SYSTEM: Unix L8",
    "  CLIENT-SIDE-ENCODING: WINDOWS-1251",
    "  ____________________________________  http://eXoPlatform.org",
    "",
    "211 -"
  };
  
  public void run(String []params) throws IOException {
    for (int i = 0; i < eXoStatInfo.length; i++) {
      reply(eXoStatInfo[i]);
    }
  }
  
}
