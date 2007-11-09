/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient.commands;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.frameworks.ftpclient.data.FtpDataTransiver;
import org.exoplatform.frameworks.ftpclient.data.FtpDataTransiverImpl;
import org.exoplatform.services.log.ExoLogger;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public class CmdPasv extends FtpCommandImpl {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdPasv");
  
  protected String host = "";
  protected int port = 0;
  
  public int execute() {
    try {
      sendCommand(FtpConst.Commands.CMD_PASV);
      
      int reply = getReply();
      
      if (FtpConst.Replyes.REPLY_227 != reply) {        
        return reply;
      }
      
      String descrVal = getDescription();
      descrVal = descrVal.substring(descrVal.indexOf("(") + 1, descrVal.indexOf(")"));
      
      String []addrValues = descrVal.split(",");

      host = "";
      for (int i = 0; i < 3; i++) {
        host += addrValues[i] + ".";
      }
      host += addrValues[3];
      
      port = new Integer(addrValues[4]) * 256 + new Integer(addrValues[5]);

      if (FtpConst.Replyes.REPLY_227 == reply) {
        FtpDataTransiver dataTransiver = new FtpDataTransiverImpl();
        dataTransiver.OpenPassive(host, port);
        clientSession.setDataTransiver(dataTransiver);
      }
      
      return reply;
    } catch (Exception exc) {
      log.info("unhandled ecxeption. " + exc.getMessage(), exc);
    }
    log.info("SOME ERRORS");
    return -1;
  }
 
  public String getHost() {
    return host;
  }
  
  public int getPort() {
    return port;
  }
  
}
