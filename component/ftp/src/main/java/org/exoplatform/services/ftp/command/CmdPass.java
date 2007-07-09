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

public class CmdPass extends FtpCommandImpl {
  
  public CmdPass() {
    commandName = FtpConst.Commands.CMD_PASS; 
    isNeedLogin = false;
  }

  public void run(String []params) throws IOException {
    if ((!FtpConst.Commands.CMD_USER.equals(clientSession().getPrevCommand())) || 
        (null == clientSession().getPrevParams())) {
      reply(String.format(FtpConst.Replyes.REPLY_503_PASS));
      return;
    }
    
    if (params.length < 2) {
      reply(String.format(FtpConst.Replyes.REPLY_500_PARAMREQUIRED, FtpConst.Commands.CMD_PASS));
      return;
    }
    
    String pass = params[1];
    clientSession().setPassword(pass);
    
    reply(String.format(FtpConst.Replyes.REPLY_230, clientSession().getUserName()));
  }
  
}
