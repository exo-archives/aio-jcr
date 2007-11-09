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

public class CmdRest extends FtpCommandImpl {

  public CmdRest() {
    commandName = FtpConst.Commands.CMD_REST; 
  }
  
  public void run(String []params) throws IOException {
    if (params.length < 2) {
      reply(String.format(FtpConst.Replyes.REPLY_500_PARAMREQUIRED, FtpConst.Commands.CMD_REST));
      return;
    }

    try {
      String position = params[1];
      new Integer(position);
      reply(String.format(FtpConst.Replyes.REPLY_350_REST, position));
      return;
    } catch (Exception exc) {
    }
    reply(String.format(FtpConst.Replyes.REPLY_500, FtpConst.Commands.CMD_REST));
  }
  
}
