/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.recovery;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 24.03.2008
 */
public class WaitConfirmation extends Thread {
  protected static Log log = ExoLogger.getLogger("ext.WaitConfirmation");

  long                 timeOut;

  RecoveryManager      recoveryManager;

  String               identifier;

  WaitConfirmation(long timeOut, RecoveryManager recoveryManager, String identifier) {
    super();
    this.timeOut = timeOut;
    this.recoveryManager = recoveryManager;
    this.identifier = identifier;

    if (log.isDebugEnabled())
      log.debug("init : " + identifier);
  }

  public void run() {
    try {
      if (log.isDebugEnabled())
        log.debug("Before : getParticipantsClusterList().size():" + recoveryManager
          .getPendingConfirmationChengesLogById(identifier).getConfirmationList().size());
      
      Thread.sleep(timeOut);

      PendingConfirmationChengesLog confirmationChengesLog = recoveryManager
          .getPendingConfirmationChengesLogById(identifier);
      
      List<String> notConfirmationList = new ArrayList<String>(recoveryManager.getParticipantsClusterList());
      notConfirmationList.removeAll(confirmationChengesLog.getConfirmationList());
      
      if (notConfirmationList.size() > 0) {
        confirmationChengesLog.setNotConfirmationList(notConfirmationList);
        recoveryManager.save(identifier);
        
        if (log.isDebugEnabled())
          log.debug("save : " + identifier);
      }

      if (log.isDebugEnabled())
        log.debug("After : getParticipantsClusterList().size():" + confirmationChengesLog.getConfirmationList().size());
      
      recoveryManager.remove(identifier);
      
      if (log.isDebugEnabled())
        log.debug("remove : " + identifier);
    } catch (InterruptedException e) {
      log.error("Can't save ChangesLog", e);
    } catch (FileNotFoundException e) {
      log.error("Can't save ChangesLog", e);
    } catch (IOException e) {
      log.error("Can't save ChangesLog", e);
    } catch (Exception e) {
      log.error("Can't save ChangesLog", e);
    }
  }
}
