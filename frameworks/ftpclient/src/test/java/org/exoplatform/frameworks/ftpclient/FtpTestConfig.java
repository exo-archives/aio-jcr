/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.ftpclient;

import org.exoplatform.frameworks.ftpclient.client.FtpClientSession;
import org.exoplatform.frameworks.ftpclient.client.FtpClientSessionImpl;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpTestConfig {

  public static final String FTP_HOST = "localhost";
  public static final int FTP_PORT = 2121;

  public static FtpClientSession getTestFtpClient() {
    return new FtpClientSessionImpl(FTP_HOST, FTP_PORT);
  }  
  
}
