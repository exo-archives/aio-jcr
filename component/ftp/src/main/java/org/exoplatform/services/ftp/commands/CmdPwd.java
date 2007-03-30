/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.commands;

import java.io.IOException;
import java.util.ArrayList;
import org.exoplatform.services.ftp.FtpConst;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CmdPwd extends FtpCommandImpl {

  public CmdPwd() {
    commandName = FtpConst.Commands.CMD_PWD; 
  }
  
  public void run(String []params) throws IOException {    
    ArrayList<String> curPath = clientSession().getPath();
    
    String path = "/";
    for (int i = 0; i < curPath.size(); i++) {
      path += curPath.get(i);
      if (i != (curPath.size() - 1)) {
        path += "/";
      }
    }
    
    reply(String.format(FtpConst.Replyes.REPLY_257, path));
  }
  
}
