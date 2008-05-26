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

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * @author Vitaliy Obmanyuk
 */

public class SessionInstantiationTest extends JCRTestBase {
  /*
   * This test calculates the time (ms or tps) of adding of one nodes of type
   * nt:file (including addNode(), setProperty(), addMixin(), save() methods).
   */

  private RepositoryImpl repositoryImpl = null;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {    
    repositoryImpl = (RepositoryImpl)context.getSession().getRepository();
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    repositoryImpl.getSystemSession();
  }

}
