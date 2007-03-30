/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.commands;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.ftp.FtpDataTransiver;
import org.exoplatform.services.ftp.FtpDataTransiverImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CmdPort extends FtpCommandImpl {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdPort");
  
  public CmdPort() {
    commandName = FtpConst.Commands.CMD_PORT; 
  }
  
  public void run(String []params) throws IOException {
    if (params.length < 2) {
      reply(String.format(FtpConst.Replyes.REPLY_500_PARAMREQUIRED, FtpConst.Commands.CMD_PORT));
      return;
    }
    
    String host = "";
    int port = 0;
    
    try {
      String []ports = params[1].split(",");      
      for (int i = 0; i < 3; i++) {
        host += ports[i] + ".";
      }
      host += ports[3];    
      port = new Integer(ports[4]) * 256 + new Integer(ports[5]);
    } catch (Exception exc) {
      reply(String.format(FtpConst.Replyes.REPLY_500_ILLEGAL, "PORT"));
      return;
    }
    
    try {
      FtpDataTransiver dataTransiver = new FtpDataTransiverImpl(host, port,
            clientSession().getFtpServer().getConfiguration(), clientSession());
      
      clientSession().setDataTransiver(dataTransiver);
      reply(String.format(FtpConst.Replyes.REPLY_200, "Port command success"));
      return;
    } catch (Exception exc) {
      log.info("Unhandlede exception. " + exc.getMessage(), exc);
    }
    
    reply(String.format(FtpConst.Replyes.REPLY_500_ILLEGAL, "PORT"));
  }
  
}
