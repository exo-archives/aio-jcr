/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.proccess;
/**
 * Created by The eXo Platform SARL        .
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
        e.printStackTrace();
      }
    }
  }
  
  public void halt() {
    stopped = true;
  }
  
  protected abstract void callPeriodically() throws Exception;

}
