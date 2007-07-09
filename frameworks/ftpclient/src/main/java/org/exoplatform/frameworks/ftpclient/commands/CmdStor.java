/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient.commands;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.frameworks.ftpclient.data.FtpDataTransiver;
import org.exoplatform.services.log.ExoLogger;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public class CmdStor extends FtpCommandImpl {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdStor");
  
  protected String path;
  protected byte []fileContent = null;
  
  public CmdStor(String path) {
    this.path = path;
  }
  
  public void setFileContent(byte []fileContent) {
    this.fileContent = fileContent;
  }
  
  public int execute() {
    if (fileContent == null) {
      return -1;
    }
    
    try {      
      FtpDataTransiver dataTransiver = clientSession.getDataTransiver();
      
      if (dataTransiver != null) {
        for (int i = 0; i < 150; i++) {
          if (!dataTransiver.isConnected()) {
            Thread.sleep(100);
          }
        }        
      }
      
      if (path == null) {
        sendCommand(FtpConst.Commands.CMD_STOR);
        return getReply();
      }
      
      sendCommand(String.format("%s %s", FtpConst.Commands.CMD_STOR, path));
      
      int reply = getReply();
      if (reply == FtpConst.Replyes.REPLY_125) {
        dataTransiver.send(fileContent);
        reply = getReply();
      }
      return reply;
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }    
    return -1;
  }
  
}
