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
package org.exoplatform.frameworks.ftpclient.commands;

import java.util.ArrayList;

import org.exoplatform.services.log.Log;
import org.exoplatform.frameworks.ftpclient.FtpConst;
import org.exoplatform.frameworks.ftpclient.data.FtpDataTransiver;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author Vitaly Guly
 * @version $Id: $
 */

public class CmdNlst extends FtpCommandImpl {

  private static Log          log   = ExoLogger.getLogger(FtpConst.FTP_PREFIX + "CmdNlst");

  protected String            path  = "";

  protected ArrayList<String> names = new ArrayList<String>();

  public CmdNlst() {
  }

  public CmdNlst(String path) {
    this.path = path;
  }

  public ArrayList<String> getNames() {
    return names;
  }

  public int execute() {
    try {
      String req = "";
      if (!"".equals(path)) {
        req = String.format("%s %s", FtpConst.Commands.CMD_NLST, path);
      } else {
        req = FtpConst.Commands.CMD_NLST;
      }
      sendCommand(req);

      int reply = getReply();

      if (reply == FtpConst.Replyes.REPLY_125) {
        FtpDataTransiver dataTransiver = clientSession.getDataTransiver();

        for (int i = 0; i < 15; i++) {
          if (!dataTransiver.isConnected()) {
            Thread.sleep(1000);
          }
        }

        byte[] data = dataTransiver.receive();
        dataTransiver.close();

        String dd = "";
        for (int i = 0; i < data.length; i++) {
          dd += (char) data[i];
        }

        String[] lines = dd.split("\r\n");
        for (int i = 0; i < lines.length; i++) {
          names.add(lines[i]);
        }

        reply = getReply();
      }

      return reply;
    } catch (Exception exc) {
      log.info(FtpConst.EXC_MSG + exc.getMessage(), exc);
    }

    return -1;
  }

}
