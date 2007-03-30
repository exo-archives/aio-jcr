/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient.commands;

import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.FtpClientSession;
import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.frameworks.ftpclient.FtpUtils;
import org.exoplatform.services.log.ExoLogger;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public abstract class FtpCommandImpl implements FtpCommand {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpCommandImpl");
  
  protected FtpClientSession clientSession;
  
  //protected PrintStream outPrintStream;
  
  protected int replyCode;
  protected String descript = "";
  
  public int run(FtpClientSession clientSession) {
    this.clientSession = clientSession;
    try {
      //outPrintStream = new PrintStream(clientSession.getClientSocket().getOutputStream());
      int status = execute();
      return status;
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }    
    return -1;
  }

  public abstract int execute();
  
  public void sendCommand(String command) {
    log.info(">>> " + command);

    try {
      byte []data = command.getBytes();
      OutputStream outStream = clientSession.getClientSocket().getOutputStream();
      outStream.write(data);
      outStream.write("\r\n".getBytes());      
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
  }
  
  private boolean isReplyString(String replyString) {
    if (replyString.length() < 4) {
      return false;
    }
    
    if (replyString.charAt(0) >= '0' && replyString.charAt(0) <= '9' &&
        replyString.charAt(1) >= '0' && replyString.charAt(1) <= '9' &&
        replyString.charAt(2) >= '0' && replyString.charAt(2) <= '9' &&
        replyString.charAt(3) == ' ') {
      return true;
    }
    return false;
  }
  
  public int getReply() throws Exception {
    log.info("try get reply..........");
    String reply = "";
    String curReply = "";
    
    while (true) {
      curReply = readLine();
      
      if ("".equals(curReply)) {
        reply += "\r\n";
      } else {
        reply += curReply;
        if (isReplyString(curReply)) {
          break;
        } else {
          reply += "\r\n";
        }
      }
      
    }

    descript = reply;
    
    replyCode = FtpUtils.getReplyCode(curReply);
    log.info("<<< " + descript);
    return replyCode;
  }
  
  public String getDescription() {
    return descript;
  }
  
//  public String readLine() throws Exception {
//    BufferedReader br = new BufferedReader(new InputStreamReader(clientSession.getClientSocket().getInputStream()));
//    return br.readLine();
//  }
  
  public String readLine() throws Exception {
    byte []buffer = new byte[4*1024];
    int bufPos = 0;
    byte prevByte = 0;

    while (true) {
      int received = clientSession.getClientSocket().getInputStream().read();
      if (received < 0) {
        return null;
      }
      
      buffer[bufPos] = (byte)received;
      bufPos++;
      
      if (prevByte == '\r' && received == '\n') {
        String resultLine = "";
        for (int i = 0; i < bufPos - 2; i++) {
          resultLine += (char)buffer[i];
        }
        return resultLine;
      }
      
      prevByte = (byte)received;
    }
  }
  
}
