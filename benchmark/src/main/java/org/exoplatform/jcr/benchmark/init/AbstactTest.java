/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.init;

import javax.jcr.Session;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class AbstactTest {
  
  public abstract void doPrepare(final TestCase tc, Session session);
  public abstract void doRun(final TestCase tc, Session session);
  
}
