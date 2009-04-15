/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.load;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.usecases.BaseUsecasesTest;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 15.04.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TestSameUserLogin extends BaseUsecasesTest {

  class LoginThread extends Thread {
    
    private final Log          TLOG  = ExoLogger.getLogger("jcr.LoginThread");

    private final Credentials user;
    
    private final Object lock;
    
    private Session session;

    LoginThread(int number, Credentials user, Object runLock) {
      super("LoginThread-" + number);
      this.user = user;
      this.lock = runLock;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      
      try {
        synchronized (lock) {
          TLOG.info("On wait");
          lock.wait();  
        }
        session = repository.login(user, "ws1");
        TLOG.info("Login ok " + System.currentTimeMillis());
      } catch (LoginException e) {
        e.printStackTrace();
      } catch (NoSuchWorkspaceException e) {
        e.printStackTrace();
      } catch (RepositoryException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }      
    }
    
    void done() {
      if (session != null)
        session.logout();
    }
  }

  public void testMultiThreadLogin() {
   
    LoginThread[] queue = new LoginThread[250];
    
    Object runLock = new Object();
    
    for (int i = 0; i<queue.length; i++) {
      queue[i] = new LoginThread(i, new CredentialsImpl("root", "exo".toCharArray()), runLock);
      queue[i].start();
    }
    
    // try start all together
    synchronized (runLock) {
      runLock.notifyAll();  
    }
    
    for (LoginThread lt : queue) {
      lt.done();
    }
  }

}
