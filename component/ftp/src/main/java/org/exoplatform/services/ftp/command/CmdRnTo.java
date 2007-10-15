/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.ftp.command;

import java.io.IOException;
import java.util.ArrayList;

import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ftp.FtpConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CmdRnTo extends FtpCommandImpl {
  
  private static Log log = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdRnTo");
  
  public CmdRnTo() {
    commandName = FtpConst.Commands.CMD_RNTO; 
  }
  
  public void run(String []params) throws IOException {
    if ((!FtpConst.Commands.CMD_RNFR.equals(clientSession().getPrevCommand())) ||
        (clientSession().getPrevParamsEx() == null)) {
      reply(FtpConst.Replyes.REPLY_503);
      return;
    }
    
    if (params.length < 2) {
      reply(String.format(FtpConst.Replyes.REPLY_500_PARAMREQUIRED, FtpConst.Commands.CMD_RNTO));
      return;
    }
    
    String pathName = params[1];
    
    try {
      ArrayList<String> newPath = clientSession().getFullPath(pathName);
      Session curSession = clientSession().getSession(newPath.get(0));
      
      String repoPath = clientSession().getRepoPath(newPath);

      if (curSession.itemExists(repoPath)) {
        reply(String.format(FtpConst.Replyes.REPLY_553, clientSession().getPrevParamsEx()));
        return;
      }      
      
      curSession.move(clientSession().getPrevParamsEx(), repoPath);
      curSession.save();
      
      reply(String.format(FtpConst.Replyes.REPLY_250, FtpConst.Commands.CMD_RNTO));
      return;
    } catch (PathNotFoundException pexc) {
    } catch (NoSuchWorkspaceException wexc) {
    } catch (Exception exc) {
      log.info("Unhandled exceprion. " + exc.getMessage(), exc);
    }
    
    reply(String.format(FtpConst.Replyes.REPLY_550, pathName));
  }

}
