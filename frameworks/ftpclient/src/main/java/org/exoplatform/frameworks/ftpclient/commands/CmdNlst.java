/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient.commands;

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.frameworks.ftpclient.FtpDataTransiver;
import org.exoplatform.services.log.ExoLogger;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public class CmdNlst extends FtpCommandImpl {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdNlst");
  
  protected String path = "";
  
  protected ArrayList<String> names = new ArrayList<String>(); 
  
  public CmdNlst() {
  }
  
  public CmdNlst(String path) {
    this.path = path;
  }
  
  public ArrayList<String> getNames() {
    return names; 
  }
  
  public int execute() {    
    try {
      String req = "";
      if (!"".equals(path)) {
        req = String.format("%s %s", FtpConst.Commands.CMD_NLST, path);
      } else {
        req = FtpConst.Commands.CMD_NLST;
      }
      sendCommand(req);
      
      int reply = getReply();
      
      if (reply == FtpConst.Replyes.REPLY_125) {
        FtpDataTransiver dataTransiver = clientSession.getDataTransiver();
        
        for (int i = 0; i < 15; i++) {
          if (!dataTransiver.isConnected()) {
            Thread.sleep(1000);
          }
        }        
        
        byte []data = dataTransiver.receive();
        dataTransiver.close();
        
        String dd = "";
        for (int i = 0; i < data.length; i++) {
          dd += (char)data[i];
        }
        
        String []lines = dd.split("\r\n");
        for (int i = 0; i < lines.length; i++) {
          names.add(lines[i]);          
        }
        
        reply = getReply();
      }

      return reply;
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }
    
    return -1;
  }
  
}
