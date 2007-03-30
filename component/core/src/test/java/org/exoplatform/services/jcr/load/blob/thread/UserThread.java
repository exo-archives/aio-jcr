/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.load.blob.thread;

import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 24.10.2006
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: UserThread.java 12841 2007-02-16 08:58:38Z peterit $
 */

public abstract class UserThread extends Thread {
  
  final Session threadSession;
  
  final Log threadLog;
  
  boolean process = false;    
  
  public UserThread(Session threadSession) {
    super();
    int inx = getName().indexOf("-");
    setName(getClass().getSimpleName() + "-" + getName().substring(inx >= 0 ? inx + 1 : 0));
    threadLog = ExoLogger.getLogger("jcr." + getName());
    this.threadSession = threadSession;
    process = true;
  }

  public void testStop() {
    process = false;
  }
  
  public void run() {
    testAction();
  }
  
  public abstract void testAction(); 
}
