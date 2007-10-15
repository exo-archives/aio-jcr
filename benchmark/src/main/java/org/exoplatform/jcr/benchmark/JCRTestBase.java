/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class JCRTestBase {

  /**
   * @param tc
   * @param context
   * @throws Exception
   */
  public void doPrepare(final TestCase tc, JCRTestContext context) throws Exception {
  }

  /**
   * @param tc
   * @param context
   * @throws Exception
   */
  public void doFinish(final TestCase tc, JCRTestContext context) throws Exception {
  }

  /**
   * @param tc
   * @param context
   * @throws Exception
   */
  public abstract void doRun(final TestCase tc, JCRTestContext context) throws Exception;

}
