/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.multi;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.TestUtils;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MultiThreadTest extends TestCase {

  public static final int THREADS_COUNT = 100;
  
  private static boolean startEnabled = false;
  
  public static boolean isStartEnabled() {
    return startEnabled;
  }
  
  public void testMultiMulti() throws Exception {
    Log.info("testMultiMulti...");

    AnimeThread thread = new AnimeThread(System.currentTimeMillis());
    thread.start();    
    
    SimpleTestAgent []clients = new SimpleTestAgent[THREADS_COUNT];
    
    String rootFolderName = "/production/test multi threads " + System.currentTimeMillis();
    TestUtils.createCollection(rootFolderName);
    
    for (int i = 0; i < clients.length; i++) {
      String subFolderName = rootFolderName + "/test agent " + i; 
      SimpleTestAgent testAgent = new SimpleTestAgent(subFolderName);
      clients[i] = testAgent;
      testAgent.start();
    }
    
    startEnabled = true;

    while (!isAllDone(clients)) {
      Thread.sleep(500);
    }

    int failures = 0;
    for (int i = 0; i < clients.length; i++) {
      if (!clients[i].isSuccess()) {
        failures++;
      }
    }
    
    Log.info("done");
    Log.info("All clients: " + clients.length);
    Log.info("Successed: " + (clients.length - failures));
    Log.info("Failures: " + failures);
    
    Log.info("\r\nClearing............");
    TestUtils.removeResource(rootFolderName);
    
    thread.interrupt();
    while (thread.isAlive()) {
      Thread.sleep(10);
    }
  }
  
  private boolean isAllDone(SimpleTestAgent []agents) {
    for (int i = 0; i < agents.length; i++) {
      SimpleTestAgent agent = agents[i];
      if (agent.isAlive()) {
        return false;
      }      
    }
    
    return true;
  }
  
  private class AnimeThread extends Thread {
    
    private long startTime; 
    
    private AnimeThread(long startTime) {
      this.startTime = startTime;
    }
    
    public void run() {
      Log.info("Started!!");
      long endTime = System.currentTimeMillis();
      try {
        while (true) {
          endTime = System.currentTimeMillis();
          System.out.println("Process (sec): " + (endTime - startTime) / 1000);
          Thread.sleep(1000);
        }        
      } catch (InterruptedException exc) {
        Log.info("Stopped!!");
      }
      
      endTime = System.currentTimeMillis();
      Log.info("ProcessAll (sec): " + (endTime - startTime) / 1000);
    }
  }
  

}

