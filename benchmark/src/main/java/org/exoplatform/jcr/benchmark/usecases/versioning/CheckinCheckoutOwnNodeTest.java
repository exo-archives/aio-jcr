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
package org.exoplatform.jcr.benchmark.usecases.versioning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.services.log.ExoLogger;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class CheckinCheckoutOwnNodeTest extends JCRTestBase {
  /*
   * This test measures performance of versioning mechanism using checkin() and
   * checkout() methods, each thread has own subnode
   */

  public static Log log      = ExoLogger.getLogger("jcr.benchmark");

  private Node      rootNode = null;

  private String    name     = "";

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    name = context.generateUniqueName("rootNode");
    rootNode = context.getSession().getRootNode().addNode(name);
    rootNode.addMixin("mix:versionable");
    context.getSession().save();
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    log.info(1);
    rootNode.checkin();
    log.info(2);
    rootNode.checkout();
    log.info(3);
    rootNode.addNode(context.generateUniqueName("child"));
    log.info(4);
    context.getSession().save();
    log.info(5);
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    rootNode.remove();
    context.getSession().save();
  }

}
