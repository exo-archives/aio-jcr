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

public class AddNodeDriver extends JCRDriverBase {
  
  protected final String SAVE_SESSION         = "saveSession";

  @Override
  public void run(final TestCase tc) {
    try {
      System.out.println("\n===AddNodeDriver.java, run");
      session.getRootNode().addNode("testNode" + String.valueOf(Math.random()), "nt:unstructured");
      if (tc.getBooleanParam(SAVE_SESSION)) {
        session.save();
      }
      System.out.println("\n===run(), repository: " + (repository));
      System.out.println("\n===run(), session   : " + (session));
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    } finally {
      session.logout();
    }
  }

}
