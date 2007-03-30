/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.commands.FtpCommand;
import org.exoplatform.services.log.ExoLogger;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public class FtpClientSessionImpl implements FtpClientSession {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpClientSessionImpl");
  
  protected String host;
  protected int port;
  protected Socket clientSocket = null;

  protected String systemType;
  protected FtpDataTransiver dataTransiver = null;
  
  public FtpClientSessionImpl(String host, int port) {
    //log.info("Starting client...");
    this.host = host;
    this.port = port;
  }
  
  public boolean connect() throws Exception {
    return connect(1);
  }
  
  public boolean connect(int attemptsCount) throws Exception {
    Exception prevExc = null;
    for (int i = 0; i < attemptsCount; i++) {
      try {
        clientSocket = new Socket();
        SocketAddress sockAddr = new InetSocketAddress(host, port);
        clientSocket.connect(sockAddr);   
        //log.info("Connected - " + clientSocket.isConnected());
        
        boolean connected = false; 
        for (int wi = 0; wi < 200; wi++) {
          if (clientSocket.isConnected()) {
            connected = true;
            break;
          }
          Thread.sleep(1);
        }
        
        if (!connected) {
          throw new Exception(); 
        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        
        String reply = "";
        while (!reply.startsWith("220 ")) {
          reply = br.readLine();
          //log.info("REPLY - " + reply);
        }
        
        return true;
      } catch (Exception exc) {
        prevExc = exc;        
      }
      Thread.sleep(3000);
      //log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> sleeping.........");
    }
    
    log.info("unhandled exception. " + prevExc.getMessage(), prevExc);
    
    return false;
  }

  public void close() {
    try {      
      if (dataTransiver != null) {
        dataTransiver.close();
      }
      
      if (clientSocket == null) {
        return;
      }
      if (clientSocket.isConnected()) {
        clientSocket.close();
      }
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }
  }
  
  public Socket getClientSocket() {
    return clientSocket;
  }
  
  public int executeCommand(FtpCommand command) throws Exception {
    if (clientSocket == null) {
      return -1;
    }
    return command.run(this);
  }
  
  public int executeCommand(FtpCommand command, int expectReply, int attemptsCount) throws Exception {
    int reply = -1;
    for (int i = 0; i < attemptsCount; i++) {
       reply = command.run(this);
       if (reply == expectReply) {
         return reply;
       }
       Thread.sleep(100);
    }
    return reply;
  }
  
  public void setSystemType(String systemType) {
    this.systemType = systemType;
  }
  
  public String getSystemType() {
    return systemType;
  }
  
  public void setDataTransiver(FtpDataTransiver dataTransiver) {
    this.dataTransiver = dataTransiver;
  }

  public FtpDataTransiver getDataTransiver() {
    return dataTransiver;
  }
  
}
