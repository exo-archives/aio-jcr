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

package org.exoplatform.services.jcr.impl.proccess;
/**
 * Created by The eXo Platform SAS.
 * @author Gennady Azarenkov
 * @version $Id: WorkerThread.java 12841 2007-02-16 08:58:38Z peterit $
 */

public abstract class WorkerThread extends Thread {
  
  protected boolean stopped = false;
  
  protected long timeout;

  public WorkerThread(String name, long timeout) {
    super(name);
    this.timeout = timeout;
  }
  
  public WorkerThread(long timeout) {
    super();
    this.timeout = timeout;
  }


  @Override
  public void run() {
    while (!stopped) {
      try {
        callPeriodically();
        sleep(timeout);
      } catch (Exception e) {
        if(!(e instanceof InterruptedException))
          e.printStackTrace();
        
      }
    }
  }
  
  public void halt() {
    stopped = true;
  }
  
  protected abstract void callPeriodically() throws Exception;

}
