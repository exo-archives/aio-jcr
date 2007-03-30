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

public class CmdUser extends FtpCommandImpl {
  
  public CmdUser() {
    commandName = FtpConst.Commands.CMD_USER; 
    isNeedLogin = false;
  }
  
  public void run(String []params) throws IOException {    
    if (params.length < 2) {
      reply(String.format(FtpConst.Replyes.REPLY_500_PARAMREQUIRED, FtpConst.Commands.CMD_USER));
      return;
    }
    
    String userName = params[1];
    clientSession().setUserName(userName);
    reply(String.format(FtpConst.Replyes.REPLY_331, userName));
  }
  
}
