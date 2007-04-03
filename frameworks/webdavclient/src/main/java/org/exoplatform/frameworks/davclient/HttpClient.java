/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class HttpClient {

  private static Log log = ExoLogger.getLogger("jcr.HttpClient");
  
  private boolean enableTrace = false;
  public void EnableTrace() {
      enableTrace = true;
  }
  
  private String server = "";
  private int port = 0;
  
  private Socket clientSocket = null;
  
  public void a() {
        
  }
  
  private PrintStream outPrintStream = null;
  private OutputStream outStream = null;
  private InputStream inputStream = null;

  // Request
  private String httpCommand = "GET";
  private String httpRequestStr = "";
  private ArrayList requestHeaders = new ArrayList();
  
  private String httpRequestBodyStr;
  private byte []httpRequestBodyBytes;
  private InputStream httpRequestBodyStream;
  
  // Response
  private String mainHeader = "";
  private ArrayList responseHeaders = new ArrayList();

  private byte []contentBytes = null;
  
  public HttpClient(String server, int port) {
      this.server = server;
      this.port = port;
  }
  
  public void conect() throws Exception {
      clientSocket = new Socket(server, port);
      outStream = clientSocket.getOutputStream();
      outPrintStream = new PrintStream(clientSocket.getOutputStream());
      inputStream = clientSocket.getInputStream();     
  }
  
  public void setHttpCommand(String httpCommand) {
      this.httpCommand = httpCommand;
  }
  
  public void setRequestPath(String httpRequestStr) {
      this.httpRequestStr = httpRequestStr;
  }
  
  public void setRequestHeader(String headerName, String headerValue) throws Exception {    
      //log.info("Try to set request header: [" + headerName + " :" + headerValue + "]");
      
      int existedIndex = -1;
      for (int i = 0; i < requestHeaders.size(); i++) {
          String curHeader = (String)requestHeaders.get(i);
          
          //log.info("cur header: >> [" + curHeader + "]");
          
          String []curHeaderValues = curHeader.split(Const.Http.HEADER_DELIM);
          
          //log.info("Pathes: " + curHeaderValues.length);
//          for (int pi = 0; pi < curHeaderValues.length; pi++) {
//            log.info("   >>> path: [" + curHeaderValues[pi] + "]");
//          }
          
//          log.info("COMP 1: [" + curHeaderValues[0].toUpperCase() + "]");
//          log.info("COMP 2: [" + headerName.toUpperCase() + "]");
          
          if (curHeaderValues[0].toUpperCase().equals(headerName.toUpperCase())) {
//            log.info("EXISTED!!!!!!!!!!!!!");
              existedIndex = i;
              break;
          }            
      }
      
      if (existedIndex >= 0) {
//        log.info("Removing: " + existedIndex);
        requestHeaders.remove(existedIndex);
      }        
      
      String newHeader = headerName + Const.Http.HEADER_DELIM + headerValue;
      log.info("Set header: [" + newHeader + "]");
      requestHeaders.add(newHeader);
  }
  
  public void setRequestBody(String httpRequestBodyStr) {
      this.httpRequestBodyStr = httpRequestBodyStr;
  }
  
  public void setRequestBody(byte []httpRequestBodyBytes) {
    this.httpRequestBodyBytes = httpRequestBodyBytes;
  }
  
  public void setRequestStream(InputStream httpRequestBodyStream) {
    this.httpRequestBodyStream = httpRequestBodyStream;
  }
  
  public void zeroRequestBody() {
      this.httpRequestBodyStr = null;
  }
  
  public void sendRequest(String request) {
      outPrintStream.print(request);
  }    

  public String getMainHeader() {
      return mainHeader;
  }
  
  public int getContentLength() throws Exception {
      for (int i = 0; i < responseHeaders.size(); i++) {
          String curHeader = (String)responseHeaders.get(i);
          if (curHeader.startsWith(Const.HttpHeaders.CONTENTLENGTH)) {
              String []params = curHeader.split(":");
              String lenValue = params[1];
              lenValue = lenValue.trim();                    
              return new Integer(lenValue);
          }
      }

      return 0;
//      log.info("try read content length..........");
//      String contentLength = readLine();
//      log.info("READED: " + contentLength);
//      
//      while (contentLength.length() < 4) {
//        contentLength = "0" + contentLength;
//      }
//
//      char []c = contentLength.toCharArray();
//      byte []decVal = Hex.decodeHex(c);
//      
//      log.info("DECVAL LEN: " + decVal.length);
//      
//      int contLen = 0;
//      
//      for (int i = 0; i < decVal.length; i++) {
//        int a = 0 + decVal[i];
//        if (a < 0) {
//          a = 256 + a;
//        }
//        contLen = (contLen << 8) + a;
//      }
//      
//      log.info("RESULT LEN: " + contLen);
//      
//      return contLen;      
  }
  
  public ArrayList<String> getResponseHeadersNames() {
      ArrayList<String> result = new ArrayList<String>();
      for (int i = 0; i < responseHeaders.size(); i++) {
          String curHeader = (String)responseHeaders.get(i);
          result.add(curHeader.split(":")[0]);
      }
      return result;
  }
  
  public String getResponseHeader(String headerName) {
      for (int i = 0; i < responseHeaders.size(); i++) {
          String curHeader = (String)responseHeaders.get(i);          
          if (curHeader.startsWith(headerName + ": ")) {
            return curHeader.substring(curHeader.indexOf(": ") + 2);
          }
      }
      return "";
  }

  public int getReplyCode() throws Exception {
      int replyCode = 0;
      String []mPathes = mainHeader.split(" ");
      replyCode = new Integer(mPathes[1]);
      return replyCode;
  }

  public String getResponseBody() {
    String contentString = "";
    for (int i = 0; i < contentBytes.length; i++) {
      contentString += (char)contentBytes[i];
    }
    return contentString;
  }

  public byte []getResponseBytes() {
    return contentBytes;
  }
  
  public InputStream getResponseStream() {
      return new ByteArrayInputStream(contentBytes);
  }

  public int execute() throws Exception {
    String escapedHttpPath = TextUtils.Escape(httpRequestStr, '%', true);
    String httpLine = httpCommand + " " + escapedHttpPath + " " + Const.Http.VERSION;

    outPrintStream.println(httpLine);
    
    if (enableTrace) {
      log.info(httpLine);
    }
    
    long reqContLength = 0;
    
    if (httpRequestBodyStream == null) {
      if (httpRequestBodyStr != null) {
        reqContLength = httpRequestBodyStr.length();
      } else if (httpRequestBodyBytes != null) {
        reqContLength = httpRequestBodyBytes.length;
      }
      
      setRequestHeader(Const.HttpHeaders.CONTENTLENGTH, "" + reqContLength);
    }
    
//    if (!requestHeaders.contains(Const.HttpHeaders.CONTENTLENGTH)) {
//      log.info(">>> set request header: " + Const.HttpHeaders.CONTENTLENGTH);      
//    } else {
//      log.info(">>> skipping: " + Const.HttpHeaders.CONTENTLENGTH);
//    }
        
    setRequestHeader(Const.HttpHeaders.HOST, String.format("%s:%s", server, port));
    setRequestHeader(Const.HttpHeaders.USERAGENT, Const.Http.CLIENTDESCR);
    
    for (int i = 0; i < requestHeaders.size(); i++) {
        String curHeader = (String)requestHeaders.get(i);
        outPrintStream.println(curHeader);
        if (enableTrace) {
          log.info(curHeader);
        }
    }
    
    outPrintStream.println();
    
    if (httpRequestBodyStream != null) {
      byte []buff = new byte[4096];
      long readData = 0;
      while (true) {
        int readed = httpRequestBodyStream.read(buff);
        readData += readed;
        if (readed < 0) {
          log.info("OOOOOOO: " + readData);
          break;
        }
        outStream.write(buff, 0, readed);
      }
    } else {
      if (reqContLength != 0) {
        if (httpRequestBodyStr != null) {
          outPrintStream.print(httpRequestBodyStr);
        } else {
          outStream.write(httpRequestBodyBytes);
        }
      }      
    }
    
    // RESPONSE
    
    mainHeader = readLine();
    while (true) {        
        String nextHeader = readLine();
        if (nextHeader.equals("")) {
            break;
        } else {
            responseHeaders.add(nextHeader);
        }
    }

    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    try {
      int contentLength = getContentLength();
      
      if (contentLength != 0 && !Const.DavCommand.HEAD.equals(httpCommand)) {
        byte []buffer = new byte[16 * 1024];
        int received = 0;
        
        while (received < contentLength) {          
          int needToRead = buffer.length;
          if (needToRead > (contentLength - received)) {
            needToRead = contentLength - received;
          }
          
          int readed = inputStream.read(buffer, 0, needToRead);
          
          if (readed < 0) {
            break;
          }
          
          if (readed == 0) {
            Thread.sleep(100);
          }
          
          outStream.write(buffer, 0, readed);            
          received += readed;          
        }
      }
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
    contentBytes = outStream.toByteArray();
    
    try {
        clientSocket.close();
    } catch (Exception exc) {}        

    return getReplyCode();
  }  
  
  protected String readLine() throws Exception {
    byte []buffer = new byte[4*1024];
    int bufPos = 0;
    byte prevByte = 0;

    while (true) {
      int received = inputStream.read();
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
