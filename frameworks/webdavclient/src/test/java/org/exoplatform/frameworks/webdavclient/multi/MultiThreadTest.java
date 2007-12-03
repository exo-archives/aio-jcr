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

package org.exoplatform.frameworks.webdavclient.multi;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
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

