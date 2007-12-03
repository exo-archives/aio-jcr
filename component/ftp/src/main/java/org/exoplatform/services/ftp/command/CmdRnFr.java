/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
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
 * Created by The eXo Platform SAS
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
