/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.jcrapi;

import org.exoplatform.jcr.benchmark.JCRDriverBase;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class TestDriver extends JCRDriverBase {

  @Override
  public void run(final TestCase tc) {
    try {
      System.out.println("\n===TestDriver.java, run");
      session.getRootNode().addNode("test");
      session.save();
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    } finally {
      session.logout();
    }
  }

}
