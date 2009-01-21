/**
 * 
 */
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
package org.exoplatform.jcr.benchmark.sessionregistry.sessioncleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.jcr.benchmark.sessionregistry.AbstractSessionRegistryTest;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.SessionRegistry;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: SessionLoginLogoutMultiThreadTest.java 111 2008-11-11 11:11:11Z $
 */
public class SessionLoginLogoutSimultaneouslyMultiThreadTest extends AbstractSessionRegistryTest {

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    super.doPrepare(tc, context);
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {

    class AgentLogin extends Thread {

      Random      random         = new Random();

      SessionImpl workSession;

      boolean     sessionStarted = false;

      public AgentLogin() {
      }

      @Override
      public void run() {
        try {
          Thread.sleep(SessionRegistry.DEFAULT_CLEANER_TIMEOUT - random.nextInt(200) + 200);

          workSession = (SessionImpl) repository.login(credentials, "system");
          sessionStarted = true;

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    class AgentLogout extends Thread {
      AgentLogin agentLogin;

      public AgentLogout(AgentLogin agentLogin) {
        this.agentLogin = agentLogin;
      }

      @Override
      public void run() {
        try {
          while (!agentLogin.sessionStarted) {
            Thread.sleep(50);
          }

          if (agentLogin.workSession.isLive()) {
            agentLogin.workSession.logout();
          }

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    // start
    List<Object> agents = new ArrayList<Object>();

    for (int i = 0; i < AGENT_COUNT; i++) {
      AgentLogin agentLogin = new AgentLogin();
      agents.add(agentLogin);
      agentLogin.start();

      AgentLogout agentLogout = new AgentLogout(agentLogin);
      agents.add(agentLogout);
      agentLogout.start();
    }

    // wait to stop all threads
    boolean isNeedWait = true;
    while (isNeedWait) {
      isNeedWait = false;
      for (int i = 0; i < AGENT_COUNT * 2; i++) {
        Thread agent = (Thread) agents.get(i);
        if (agent.isAlive()) {
          isNeedWait = true;
          break;
        }
      }
      Thread.sleep(1000);
    }
  }
}
