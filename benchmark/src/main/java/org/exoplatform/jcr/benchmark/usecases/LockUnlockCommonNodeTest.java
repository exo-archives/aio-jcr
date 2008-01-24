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
package org.exoplatform.jcr.benchmark.usecases;

import javax.jcr.Credentials;
import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.lock.LockException;

import org.apache.commons.logging.Log;
import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class LockUnlockCommonNodeTest extends JCRTestBase {
  /*
   * This test measures performance of lock mechanism, each thread has common
   * node
   */

  public static Log     log             = ExoLogger.getLogger("jcr.benchmark");

  public static boolean rootNodeCreated = false;

  public static boolean rootNodeDeleted = false;

  public static String  rootNodeName    = "rootNode";

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    createRootNode(context);
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    Node rootNode = context.getSession().getRootNode().getNode(rootNodeName);
    try {
      rootNode.addNode(context.generateUniqueName("node"), "nt:unstructured");
      context.getSession().save();
      throw new RuntimeException("LockException must be here");
    } catch (LockException e) {
      // as expected
    }
    try {
      rootNode.setProperty(context.generateUniqueName("property"), context
          .generateUniqueName("value"));
      context.getSession().save();
      throw new RuntimeException("LockException must be here");
    } catch (LockException e) {
      // as expected
    }
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    deleteRootNodeForAllThreads(context);
  }

  private synchronized void createRootNode(JCRTestContext context) {
    try {
      if (!rootNodeCreated) {
        Credentials tmpCredentials = ((SessionImpl) context.getSession()).getCredentials();
        Session tmpSession = context.getSession().getRepository().login(tmpCredentials);
        Node rootNode = tmpSession.getRootNode().addNode(rootNodeName);
        tmpSession.save();
        rootNode.addMixin("mix:lockable");
        tmpSession.save();
        context.getSession().save();
        rootNode.lock(true, true);
        rootNodeCreated = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private synchronized void deleteRootNodeForAllThreads(JCRTestContext context) {
    try {
      if (!rootNodeDeleted) {
        Credentials tmpCredentials = ((SessionImpl) context.getSession()).getCredentials();
        Session tmpSession = context.getSession().getRepository().login(tmpCredentials);
        Node rootNode = tmpSession.getRootNode().getNode(rootNodeName);
        rootNode.remove();
        tmpSession.save();
        rootNodeDeleted = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
