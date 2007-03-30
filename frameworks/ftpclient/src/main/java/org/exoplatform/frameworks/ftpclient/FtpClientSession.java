/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient;

import java.net.Socket;
import org.exoplatform.frameworks.ftpclient.commands.FtpCommand;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public interface FtpClientSession {

  Socket getClientSocket();
  
  boolean connect() throws Exception;
  boolean connect(int attemptsCount) throws Exception;
  
  void close();
  int executeCommand(FtpCommand command) throws Exception;
  int executeCommand(FtpCommand command, int expectReply, int attemptsCount) throws Exception;
  
  void setSystemType(String systemType);
  String getSystemType();
  
  void setDataTransiver(FtpDataTransiver dataTransiver);
  FtpDataTransiver getDataTransiver();
  
}
