/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.command;

import java.io.IOException;
import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CmdRnFr extends FtpCommandImpl {
  
  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdRnFr");
  
  public CmdRnFr() {
    commandName = FtpConst.Commands.CMD_RNFR; 
  }

  public void run(String []params) throws IOException {
    if (params.length < 2) {
      reply(String.format(FtpConst.Replyes.REPLY_500_PARAMREQUIRED, FtpConst.Commands.CMD_RNFR));
      return;
    }
    
    String resName = params[1];
    clientSession().setPrevParamsEx(null);

    try {
      ArrayList<String> newPath = clientSession().getFullPath(resName);
      
      Session curSession = clientSession().getSession(newPath.get(0));
      
      String repoPath = clientSession().getRepoPath(newPath);
      
      curSession.getItem(repoPath);
      
      clientSession().setPrevParamsEx(repoPath);
      
      reply(FtpConst.Replyes.REPLY_350);
      return;
    } catch (RepositoryException rexc) {
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    reply(String.format(FtpConst.Replyes.REPLY_550, resName));
  }
  
}
