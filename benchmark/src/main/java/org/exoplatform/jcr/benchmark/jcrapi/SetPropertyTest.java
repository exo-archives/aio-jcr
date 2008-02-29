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
package org.exoplatform.jcr.benchmark.jcrapi;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:vitaliy.obmanyuk@exoplatform.com.ua">Vitaliy Obmanyuk</a>
 * @version $Id: SetPropertyTest.java 111 2008-11-11 11:11:11Z vetalok $
 */

public class SetPropertyTest extends JCRTestBase {

  private Node rootNode = null;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    Session session = context.getSession();
    rootNode = session.getRootNode().addNode(context.generateUniqueName("rootNode"));
    session.save();
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    try {
      rootNode.setProperty(context.generateUniqueName("property"), context.generateUniqueName("value"));
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    Session session = context.getSession();
    rootNode.remove();
    session.save();
  }

}
