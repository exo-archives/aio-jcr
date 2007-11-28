/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.exoplatform.jcr.benchmark.init.JCRInitializer;

import com.sun.japex.JapexDriverBase;
import com.sun.japex.Params;
import com.sun.japex.ParamsImpl;
import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 *
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class JCRDriver extends JapexDriverBase {

  protected Repository     repository;

  protected Session        oneSession;

  protected Credentials    credentials;

  protected String         workspace;

  protected JCRTestContext context;

  private JCRTestBase      test;

  @Override
  public void initializeDriver() {
    if (!hasParam("jcr.initializer"))
      throw new RuntimeException("<jcr.initializer> parameter required");
    if (!hasParam("jcr.user"))
      throw new RuntimeException("<jcr.user> parameter required");
    if (!hasParam("jcr.password"))
      throw new RuntimeException("<jcr.password> parameter required");
    if (!hasParam("jcr.workspace"))
      throw new RuntimeException("<jcr.workspace> parameter required");

    String initializerName = getParam("jcr.initializer");
    String user = getParam("jcr.user");
    String password = getParam("jcr.password");
    Params params = new ParamsImpl();
    params.setParam("exo.jaasConf", getParam("exo.jaasConf"));
    params.setParam("exo.containerConf", getParam("exo.containerConf"));
    try {
      JCRInitializer initializer = (JCRInitializer) Class.forName(initializerName).newInstance();
      initializer.initialize(params);
      repository = initializer.getRepository();
      workspace = getParam("jcr.workspace");
      credentials = new SimpleCredentials(user, password.toCharArray());
      oneSession = repository.login(credentials, workspace);
      context = new JCRTestContext();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void prepare(final TestCase tc) {
    try {
      //System.out.println("Start...");
      initContext(tc, context);
      //System.out.println("initContext done...");
      test = testInstance(tc);
      //System.out.println("testInstance done...");
      test.doPrepare(tc, context);
      //System.out.println("doPrepare done...");
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void run(final TestCase tc) {
    try {
      test.doRun(tc, context);
    } catch (Exception e) {
      e.printStackTrace();
      try {
        Session s = context.getSession();
        if(s != null)
          s.refresh(false);
      } catch (RepositoryException e1) {
        System.err.println("Can not refresh test session. Reason: "+e1.getMessage());
      }
    }
  }

  @Override
  public void finish(final TestCase tc) {
    try {
      test.doFinish(tc, context);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private synchronized JCRTestBase testInstance(TestCase tc) {

    if (!tc.hasParam("exo.testClass"))
      throw new RuntimeException("<exo.testClass> parameter required");

    try {
      String testCaseName = tc.getParam("exo.testClass");
      return (JCRTestBase) Class.forName(testCaseName).newInstance();

    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

  private synchronized JCRTestContext initContext(TestCase tc, JCRTestContext context) {
    //context.setSession(oneSession);

    if (!hasParam("jcr.sessionPolicy"))
      throw new RuntimeException("<jcr.sessionPolicy> parameter required");
    String sessionPolicy = getParam("jcr.sessionPolicy");
    if (sessionPolicy.equalsIgnoreCase("single"))
      context.setSession(oneSession);
    else if (sessionPolicy.equalsIgnoreCase("multiple"))
      try {
        context.setSession(repository.login(credentials, workspace));
      } catch (LoginException e) {
        throw new RuntimeException(e);
      } catch (RepositoryException e) {
        throw new RuntimeException(e);
      }
    else
      throw new RuntimeException(
          "<sessionPolicy> parameter expects 'single' or 'multiple' values. Found " + sessionPolicy);

    return context;
  }
}
