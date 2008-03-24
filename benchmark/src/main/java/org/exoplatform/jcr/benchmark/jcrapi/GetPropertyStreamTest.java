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

import javax.jcr.Node;

import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Vitaliy Obmanyuk
 * 
 * @version $Id: SetPropertyTest.java 11582 2008-03-04 16:49:40Z pnedonosko $
 */

public class GetPropertyStreamTest extends AbstractGetItemNameTest {

  @Override
  protected void createContent(Node parent, TestCase tc, JCRTestContext context) throws Exception {
    String pname = context.generateUniqueName("property");
    InputStream value = new FileInputStream("../resources/benchmark.pdf");
    try {
      parent.setProperty(pname, value);
      addName(parent.getName() + "/" + pname);
    } finally {
      value.close();
    }
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    rootNode.getProperty(nextName()).getStream();
  }
}
