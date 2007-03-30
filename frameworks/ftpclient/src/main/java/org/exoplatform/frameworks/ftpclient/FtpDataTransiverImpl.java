/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.ftpclient;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
* Created by The eXo Platform SARL        .
* @author Vitaly Guly
* @version $Id: $
*/

public class FtpDataTransiverImpl implements FtpDataTransiver {

  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "FtpDataTransiverImpl");
  
  protected Socket dataSocket = null;
  
  protected Thread connectionThread;
  
  public FtpDataTransiverImpl() {
  }
    
  public void OpenPassive(String host, int port) {
    connectionThread = new PassiveThread(host, port);
    connectionThread.start();
  }
  
  public boolean OpenActive(int port) {
    try {
      connectionThread = new ActiveThread(port);
      connectionThread.start();
      return true;
    } catch (Exception exc) {
      log.info("Can't open active mode. PORT is busy. " + exc.getMessage(), exc);
    }
    return false;
  }
  
  public boolean isConnected() {
    if (dataSocket == null) {
      return false;
    }
    return dataSocket.isConnected();
  }
  
  public void close() {
    try {
      if (connectionThread != null) {
        connectionThread.stop();
      }      
      if (dataSocket != null && dataSocket.isConnected()) {
        dataSocket.close();
      }
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }
  }
  
  public byte []receive() {
    if (dataSocket == null) {
      return null;
    }
  
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
  
    try {
      byte []buffer = new byte[4096];
      while (dataSocket.isConnected()) {
        int readed = dataSocket.getInputStream().read(buffer);
        if (readed < 0) {
          break;
        }
        outStream.write(buffer, 0, readed);
        Thread.sleep(10);
      }    
    } catch (SocketException exc) {
      //..
    } catch (Exception exc) {
      exc.printStackTrace();
    }
  
    try {
      if (dataSocket.isConnected()) {
        dataSocket.close();
      }
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
  
    return outStream.toByteArray();
  }
  
  public boolean send(byte []data) {
    if (dataSocket != null) {
      try {
        dataSocket.getOutputStream().write(data);
        dataSocket.close();
        return true;
      } catch (Exception exc) {
        log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
      }
    }
    return false;
  }
  
  protected class PassiveThread extends Thread {
   
    private Log passiveLog = ExoLogger.getLogger("jcr.FtpDataTransiverImpl__PassiveThread");
    
    protected String host;
    protected int port;
    
    public PassiveThread(String host, int port) {
      this.host = host;
      this.port = port;
    }
    
    public void run() {
      dataSocket = new Socket();
      SocketAddress sockAddr = new InetSocketAddress(host, port);
      
      try {
        dataSocket.connect(sockAddr);
      } catch (Exception exc) {
        passiveLog.info("Can't open PASSIVE mode. " + exc.getMessage(), exc);
      }
      
    }
    
  }
  
  protected class ActiveThread extends Thread {
    
    private Log activeLog = ExoLogger.getLogger("jcr.FtpDataTransiverImpl__ActiveThread");
    
    protected int port;
    protected ServerSocket serverSocket;
   
    public ActiveThread(int port) throws Exception {
      this.port = port;
      serverSocket = new ServerSocket(port); 
    }
    
    public void run() {
      try {
        dataSocket = serverSocket.accept();
        serverSocket.close();
      } catch (Exception exc) {
        activeLog.info("Can't open ACTIVE mode. " + exc.getMessage(), exc);
      }
      
    }
    
  }
  
}
