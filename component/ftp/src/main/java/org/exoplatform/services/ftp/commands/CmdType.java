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

public class CmdType extends FtpCommandImpl {

  public CmdType() {
    commandName = FtpConst.Commands.CMD_TYPE; 
  }
  
  public void run(String []params) throws IOException {
    if (params.length < 2) {
      reply(String.format(FtpConst.Replyes.REPLY_500_PARAMREQUIRED, FtpConst.Commands.CMD_TYPE));
      return;
    }
    
    String typeVal = params[1];
    if ("A".equals(typeVal.toUpperCase()) || "I".equals(typeVal.toUpperCase())) {
      reply(String.format(FtpConst.Replyes.REPLY_200, "Type set to " + typeVal.toUpperCase()));
      return;
    }    

    reply(String.format(FtpConst.Replyes.REPLY_500, "'" + FtpConst.Commands.CMD_TYPE + " " + params[1].toUpperCase() + "'"));
  }
  
}
