/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient.commands;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.frameworks.ftpclient.FtpConst.Commands;
import org.exoplatform.services.log.ExoLogger;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public class CmdSize extends FtpCommandImpl {

  public static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdSize");
  
  protected String path;
  protected int size = 0;
  
  public CmdSize(String path) {
    this.path = path;
  }
  
  public int getSize() {
    return size;
  }
  
  public int execute() {
    try {
      // for tests only
      if (path == null) {
        sendCommand(FtpConst.Commands.CMD_SIZE);
        return getReply();
      }
      
      sendCommand(String.format("%s %s", FtpConst.Commands.CMD_SIZE, path));

      int reply = getReply();
      
      if (reply == FtpConst.Replyes.REPLY_213) {
        String descr = getDescription(); 
        String sizeVal = descr.substring(descr.indexOf(" ") + 1);
        size = new Integer(sizeVal);
      }
      return reply;
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }
    return -1;
  }
  
}
