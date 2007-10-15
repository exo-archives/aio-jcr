/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient;

import java.net.ServerSocket;
import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public class FtpUtils {

  protected static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpUtils"); 
  
  public static int getReplyCode(String reply) {    
    if (reply.charAt(3) != ' ') {
      return -1;
    }    
    String replyCodeVal = reply.substring(0, 3);
    return new Integer(replyCodeVal);
  }
  
  public static boolean isPortFree(int port) {    
    try {
      ServerSocket serverSocket = new ServerSocket(port);
      serverSocket.close();
      return true;
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }    
    return false;
  }
  
}
