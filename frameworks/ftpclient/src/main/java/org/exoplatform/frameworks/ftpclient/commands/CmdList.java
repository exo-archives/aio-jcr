/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient.commands;

import java.util.ArrayList;
import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.frameworks.ftpclient.FtpDataTransiver;
import org.exoplatform.frameworks.ftpclient.FtpFileInfo;
import org.exoplatform.frameworks.ftpclient.FtpFileInfoImpl;
import org.exoplatform.services.log.ExoLogger;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public class CmdList extends FtpCommandImpl {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdList");
  
  protected String path = "";
  
  protected byte []fileData;
  protected ArrayList<FtpFileInfo> files = new ArrayList<FtpFileInfo>();

  public CmdList() {
  }
  
  public CmdList(String path) {
    this.path = path;
  }

  public byte []getFileData() {
    return fileData;
  }
  
  public ArrayList<FtpFileInfo> getFiles() {
    return files;
  }
  
  public int execute() {
    try {
      if (clientSession.getSystemType() == null) {
        clientSession.executeCommand(new CmdSyst());
      }

      String req;
      
      if ("".equals(path)) {
        req = FtpConst.Commands.CMD_LIST;
      } else {
        req = String.format("%s %s", FtpConst.Commands.CMD_LIST, path);
      }
      sendCommand(req);
      
      int reply = getReply();
      
      if (reply == FtpConst.Replyes.REPLY_125 || reply == FtpConst.Replyes.REPLY_150) {
        FtpDataTransiver dataTransiver = clientSession.getDataTransiver();
        
        fileData = dataTransiver.receive();
        
        dataTransiver.close();
        
        String dd = new String(fileData, "windows-1251");
        
        String []lines = dd.split("\r\n");
        
        String systemType = clientSession.getSystemType();
        systemType = systemType.substring(systemType.indexOf(" ") + 1);
        
        for (int i = 0; i < lines.length; i++) {
          try {
            FtpFileInfo fileInfo = new FtpFileInfoImpl();
            if (!"".equals(lines[i])) {
              fileInfo.parseDir(lines[i], systemType);
              files.add(fileInfo);            
            }            
          } catch (Exception exc) {
            log.info("CAN'T PARSE FILE LINE: [" + lines[i] + "]");
          }
        }
        reply = getReply();
      }      
      return reply;
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    return -1;
  }
  
}
