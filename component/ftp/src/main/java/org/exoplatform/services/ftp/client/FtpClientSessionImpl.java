/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.jcr.JCRAppSessionFactory;
import org.exoplatform.frameworks.jcr.SingleRepositorySessionFactory;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.ftp.FtpServer;
import org.exoplatform.services.ftp.data.FtpDataTransiver;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpClientSessionImpl implements FtpClientSession {
  
  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpClientSessionImpl");
  
  private FtpServer ftpServer;
  
  FtpClientCommandThread commandThread;
  FtpClientTimeOutThread timeOutThread;
  
  private FtpDataTransiver transiver = null;
  
  private Socket clientSocket;  
  //private PrintStream outPrintStream;

  private JCRAppSessionFactory sessionFactory;
  
  private ArrayList<String> path = new ArrayList<String>();
  
  private String serverIp;  
  private String userName = "";
  private String userPass = "";
  private boolean logged = false;
  
  private String prevCommand = "";
  private String prevParams = "";
  private String prevParamsEx = "";
   
  public FtpClientSessionImpl(FtpServer ftpServer, Socket clientSocket) throws Exception {    
    this.ftpServer = ftpServer;
    this.clientSocket = clientSocket;
    //outPrintStream = new PrintStream(clientSocket.getOutputStream());
    
    SocketAddress addr = clientSocket.getLocalSocketAddress();
    String serverAddr = addr.toString();    
    if (serverAddr.startsWith("/")) {
      serverAddr = serverAddr.substring(1);
    }
    String []serverLocations = serverAddr.split(":");
    serverIp = serverLocations[0];
    
    welcomeClient();

    commandThread = new FtpClientCommandThread(this);
    commandThread.start();
    
    if (getFtpServer().getConfiguration().isNeedTimeOut()) {
      timeOutThread = new FtpClientTimeOutThread(this);
      timeOutThread.start();
    }
  }
  
  public Socket getClientSocket() {
    return clientSocket;
  }
  
  public void reply(String replyString) throws IOException {
    String encodingType = ftpServer.getConfiguration().getClientSideEncoding();
    try {
      byte []data = replyString.getBytes(encodingType);
      //outPrintStream.println(new String(, encodingType));
      clientSocket.getOutputStream().write(data);
    } catch (UnsupportedEncodingException eexc) {
      log.info("Unsupported encoding exception. See for CLIENT-SIDE-ENCODING parameter. " + eexc.getMessage(), eexc);
      byte []data = replyString.getBytes();
      clientSocket.getOutputStream().write(data);
      //outPrintStream.println(replyString);
    }    
    clientSocket.getOutputStream().write("\r\n".getBytes());
  }
  
  public FtpServer getFtpServer() {
    return ftpServer;
  }
  
  protected void welcomeClient() throws IOException {
    for (int i = 0; i < FtpConst.eXoLogo.length; i++) {
      reply(FtpConst.eXoLogo[i]);
    }
  }
  
  private boolean isLoggedOut = false;
  
  public void logout() {
    if (isLoggedOut) {
      return;
    }
    isLoggedOut = true;
    
    commandThread.interrupt();    
    if (timeOutThread != null) {
      timeOutThread.interrupt();
    }    
    
    closeDataTransiver();
    
    try {
      clientSocket.close();
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }

    getFtpServer().unRegisterClient(this);
    
    if (sessionFactory != null) {
      sessionFactory.close();
    }    
  }
  
  public boolean isLogged() {
    return logged;
  }
  
  public void setUserName(String userName) {
    this.userName = userName;
    logged = false;
  }
  
  public void setPassword(String userPass) {
    this.userPass = userPass;
    
    if (sessionFactory != null) {      
      sessionFactory.close();
    }
    
    Credentials credentials = new SimpleCredentials(userName, userPass.toCharArray());
    sessionFactory = new SingleRepositorySessionFactory(ftpServer.getRepository(), credentials);
    logged = true;
  }
  
  public String getUserName() {
    return userName;
  }
  
  public String getUserPassword() {
    return userPass;
  }

  public String getServerIp() {
    return serverIp;
  }
  
  public void setDataTransiver(FtpDataTransiver newTransiver) {
    if (transiver != null) {
      transiver.close(); 
    }
    transiver = newTransiver;
  }
  
  public void closeDataTransiver() {
    if (transiver != null) {
      transiver.close();
      transiver = null;
    }
  }
  
  public FtpDataTransiver getDataTransiver() {
    return transiver;
  }
  
  public void setPrevCommand(String prevCommand) {
    this.prevCommand = prevCommand;
  }
  
  public void setPrevParams(String prevParams) {
    this.prevParams = prevParams;
  }
  
  public void setPrevParamsEx(String prevParamsEx) {
    this.prevParamsEx = prevParamsEx;
  }
  
  public String getPrevCommand() {
    return prevCommand;
  }
  
  public String getPrevParams() {
    return prevParams;
  }
  
  public String getPrevParamsEx() {
    return prevParamsEx;
  }

  public ArrayList<String> getFullPath(String resPath) {
    ArrayList<String> curPath = getPath();
    if (resPath.startsWith("/")) {
      curPath.clear();
    }
    
    String []pathes = resPath.split("/");
    for (int i = 0; i < pathes.length; i++) {
      if (!"".equals(pathes[i])) {
        if ("..".equals(pathes[i])) {
          if (curPath.size() != 0) {
            curPath.remove(curPath.size() - 1);
          }
        } else {
          curPath.add(pathes[i]);
        }
      }
    }    
    return curPath; 
  }  
  
  public String getRepoPath(ArrayList<String> repoPath) {
    String curPath = "/";
    for (int i = 1; i < repoPath.size(); i++) {
      curPath += repoPath.get(i);
      if (i < (repoPath.size() - 1)) {
        curPath += "/";
      }
    }
    return curPath;
  }
  
  public Session getSession(String workspaceName) throws Exception {
    Session curSession = sessionFactory.getSession(workspaceName);
    curSession.refresh(false);
    return curSession;
  }

  public String changePath(String resPath) {
    ArrayList<String> newPath = getFullPath(resPath); 
    
    if (newPath.size() == 0) {
      path = new ArrayList<String>();
      return FtpConst.Replyes.REPLY_250;
    }
    
    String repoWorkspace = newPath.get(0);
    String repoPath = getRepoPath(newPath);

    try {
      Session curSession = getSession(repoWorkspace);
      
      Node curNode = (Node)curSession.getItem(repoPath);
      if (curNode.isNodeType(FtpConst.NodeTypes.NT_FILE)) {
        return FtpConst.Replyes.REPLY_550;
      }        
     
      path = (ArrayList<String>)newPath.clone();
      return FtpConst.Replyes.REPLY_250;
    } catch (RepositoryException exc) {
      return FtpConst.Replyes.REPLY_550;
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
    return FtpConst.Replyes.REPLY_550;
  }
  
  public ArrayList<String> getPath() {
    return (ArrayList<String>)path.clone();
  }  
  
  public void refreshTimeOut() {
    if (timeOutThread != null) {
      timeOutThread.refreshTimeOut();
    }
  }
  
}
