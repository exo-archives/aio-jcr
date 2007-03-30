/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.ftpclient;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.ftpclient.FtpConst.Replyes;
import org.exoplatform.frameworks.ftpclient.commands.CmdList;
import org.exoplatform.frameworks.ftpclient.commands.CmdPass;
import org.exoplatform.frameworks.ftpclient.commands.CmdPasv;
import org.exoplatform.frameworks.ftpclient.commands.CmdPwd;
import org.exoplatform.frameworks.ftpclient.commands.CmdSyst;
import org.exoplatform.frameworks.ftpclient.commands.CmdType;
import org.exoplatform.frameworks.ftpclient.commands.CmdUser;
import org.exoplatform.services.log.ExoLogger;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FtpGrab extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.ftpgrabber");
  
  
  public void testFtpGrab() throws Exception {
  }
  
}
