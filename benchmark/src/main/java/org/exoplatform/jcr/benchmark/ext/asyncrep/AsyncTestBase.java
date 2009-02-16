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
package org.exoplatform.jcr.benchmark.ext.asyncrep;

import org.apache.commons.logging.Log;
import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.services.log.ExoLogger;

import com.sun.japex.TestCase;


/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: AsynctestBase.java 111 2008-11-11 11:11:11Z serg $
 */
public abstract class AsyncTestBase {
  protected static Log log = ExoLogger.getLogger("jcr.benchmark.async");

  /**
   * @param tc
   * @param context
   * @throws Exception
   */
  public void doPrepare(final TestCase tc, AsyncTestContext context) throws Exception {
  }

  /**
   * @param tc
   * @param context
   * @throws Exception
   */
  public void doFinish(final TestCase tc, AsyncTestContext context) throws Exception {
  }

  /**
   * @param tc
   * @param context
   * @throws Exception
   */
  public abstract void doRun(final TestCase tc, AsyncTestContext context) throws Exception;

}
