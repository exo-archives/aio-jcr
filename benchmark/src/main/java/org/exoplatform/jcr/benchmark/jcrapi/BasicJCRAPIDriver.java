/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.jcrapi;

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.exoplatform.jcr.benchmark.JCRDriverBase;
import org.exoplatform.jcr.benchmark.init.AbstactTest;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class BasicJCRAPIDriver extends JCRDriverBase {

  private final String SAVE_SESSION = "saveSession";

  private final String packageName  = "org.exoplatform.jcr.benchmark.jcrapi.";

  @Override
  public void run(final TestCase tc) {
    Session session = null;
    try {
      session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()), "ws");
      System.out.println("===BasicJCRAPIDriver.java, run, repository : " + repository);
      System.out.println("===BasicJCRAPIDriver.java, run, session    : " + session);      
      String testCaseName = packageName + tc.getName();
      AbstactTest test = (AbstactTest) Class.forName(testCaseName).newInstance();
      test.execute(tc, session);
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    } finally {
      session.logout();
    }
  }

}
