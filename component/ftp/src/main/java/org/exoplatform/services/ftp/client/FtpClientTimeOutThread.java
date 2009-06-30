/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.ftp.client;

import java.io.IOException;

import org.exoplatform.services.log.Log;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * 
 * @version $Id: $
 */

public class FtpClientTimeOutThread extends Thread {

  private static Log       log   = ExoLogger.getLogger("jcr.FtpClientTimeOutThread");

  private FtpClientSession clientSession;

  private int              timeOutValue;

  private int              clock = 0;

  public FtpClientTimeOutThread(FtpClientSession clientSession) {
    this.clientSession = clientSession;
    timeOutValue = clientSession.getFtpServer().getConfiguration().getTimeOut();
  }

  public void refreshTimeOut() {
    clock = 0;
  }

  public void run() {
    while (true) {
      try {
        Thread.sleep(1000);
        clock++;
        if (clock >= timeOutValue) {
          break;
        }
      } catch (InterruptedException iexc) {
        return;
      }
    }

    try {
      clientSession.reply(String.format(FtpConst.Replyes.REPLY_421, timeOutValue));
    } catch (IOException ioexc) {
      log.info("Unhandled exception. " + ioexc.getMessage(), ioexc);
    }
    clientSession.logout();
  }

}
