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

public class CmdNLst extends FtpCommandImpl {

  public CmdNLst() {
    commandName = FtpConst.Commands.CMD_NLST; 
  }
  
  public void run(String []params) throws IOException {
    SendFileList(params);
  }
  
}
