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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Vitaliy Obmanyuk
 */

public class GetPropertyStreamTest extends JCRTestBase {

  private Node         rootNode      = null;

  private int          RUNITERATIONS = 0;

  private List<String> names         = new ArrayList<String>();

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    RUNITERATIONS = tc.getIntParam("japex.runIterations");
    Session session = context.getSession();
    rootNode = session.getRootNode().addNode(context.generateUniqueName("rootNode"));
    session.save();
    for (int i = 0; i < RUNITERATIONS; i++) {
      String name = context.generateUniqueName("property");
      InputStream value = new FileInputStream("../resources/benchmark.pdf");
      rootNode.setProperty(name, value);
      names.add(name);
    }
    session.save();
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    rootNode.getProperty(names.remove(0)).getStream();
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    Session session = context.getSession();
    rootNode.remove();
    session.save();
  }

}
