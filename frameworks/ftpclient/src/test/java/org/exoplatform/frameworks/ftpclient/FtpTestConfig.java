/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.ftpclient;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpTestConfig {

  public static final String FTP_HOST = "localhost";
  public static final int FTP_PORT = 21;

//  public static final String FTP_HOST = "localhost";
//  public static final int FTP_PORT = 2121;  
  
//  public static final String FTP_HOST = "192.168.0.10";
//  public static final int FTP_PORT = 2121;
  
  public static FtpClientSession getTestFtpClient() {
    return new FtpClientSessionImpl(FTP_HOST, FTP_PORT);
  }  
  
}
