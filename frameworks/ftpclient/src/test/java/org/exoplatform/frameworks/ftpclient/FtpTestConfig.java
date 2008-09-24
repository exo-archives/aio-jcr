/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.ftpclient;

import org.exoplatform.frameworks.ftpclient.client.FtpClientSession;
import org.exoplatform.frameworks.ftpclient.client.FtpClientSessionImpl;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * 
 * @version $Id: $
 */

public class FtpTestConfig {

  /*
   * Server location
   */

  public static final String FTP_HOST       = "localhost";

  public static final int    FTP_PORT       = 2121;

  /*
   * Credentials and workspaceName
   */

  public static final String USER_ID        = "admin";

  public static final String USER_PASS      = "admin";

  public static final String TEST_WORKSPACE = "production";

  /*
   * MultiThread
   */

  public static final int    CLIENTS_COUNT  = 20;

  public static final int    CLIENT_DEPTH   = 2;

  public static final String TEST_FOLDER    = "/" + TEST_WORKSPACE + "/crash_test2";

  public static FtpClientSession getTestFtpClient() {
    return new FtpClientSessionImpl(FTP_HOST, FTP_PORT);
  }

}
