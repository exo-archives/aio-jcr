/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.commands;

import java.io.IOException;

import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.ftp.FtpDataTransiver;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CmdPasv extends FtpCommandImpl {

  public CmdPasv() {
    commandName = FtpConst.Commands.CMD_PASV; 
  }
  
  public void run(String []params) throws IOException {
    FtpDataTransiver transiver = clientSession().getFtpServer().getDataChannelManager().getDataTransiver(clientSession());
    
    if (transiver == null) {
      reply(FtpConst.Replyes.REPLY_421_DATA);
      return;
    }
    clientSession().setDataTransiver(transiver);
    
    String serverLocation = clientSession().getServerIp();
    serverLocation = serverLocation.replace('.', ',');
          
    int dataPort = transiver.getDataPort();
    int high = dataPort / 256;
    int low = dataPort % 256;
    
    serverLocation += String.format(",%s,%s", high, low);
    reply(String.format(FtpConst.Replyes.REPLY_227, serverLocation));
  }
  
}
