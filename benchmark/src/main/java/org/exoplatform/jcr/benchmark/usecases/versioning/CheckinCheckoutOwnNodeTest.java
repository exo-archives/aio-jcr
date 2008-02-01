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
   * Each thread makes a lot of versions of his own node using chekin-checkout
   * methods (between them should be some node operations like adding) many
   * times.
   */

  public static Log log      = ExoLogger.getLogger("jcr.benchmark");

  private Node      rootNode = null;

  private String    name     = "";

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    log.info("start");
    name = context.generateUniqueName("rootNode");
    rootNode = context.getSession().getRootNode().addNode(name);
    rootNode.addMixin("mix:versionable");
    context.getSession().save();
    log.info("end");
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    log.info("start");
    rootNode.checkin();
    rootNode.checkout();
    rootNode.addNode(context.generateUniqueName("child"));
    context.getSession().save();
    rootNode.checkin();
    rootNode.checkout();
    log.info("end");
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    log.info("start");
    rootNode.remove();
    context.getSession().save();
    log.info("end");
  }

}
