/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import javax.jcr.Session;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface FtpClientSession {

  FtpServer getFtpServer();
  
  Socket getClientSocket();
  
  void reply(String replyString) throws IOException;  
  
  String getServerIp();
  
  boolean isLogged();
  
  void logout();
  
  String getUserName();
  
  String getUserPassword();
  
  void setUserName(String userName);
  
  void setPassword(String userPass);
  
  FtpDataTransiver getDataTransiver();
  
  void setDataTransiver(FtpDataTransiver newTransiver);
  
  void closeDataTransiver();
  
  String getPrevCommand();
  
  String getPrevParams();
  
  String getPrevParamsEx();

  void setPrevCommand(String prevCommand);
  
  void setPrevParams(String prevParams);
  
  void setPrevParamsEx(String prevParams);  
  
  String changePath(String resPath);
  
  ArrayList<String> getPath();
  
  ArrayList<String> getFullPath(String resPath);
  
  String getRepoPath(ArrayList<String> repoPath);
  
  Session getSession(String workspaceName) throws Exception;
  
  void refreshTimeOut();
  
}
