/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Node;
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

  protected static int     threadCounter = 0;

  protected Repository     repository;

  protected Session        oneSession;

  protected Credentials    credentials;

  protected String         workspace;

  protected JCRTestContext context;

  private JCRTestBase      test;

  @Override
  public void initializeDriver() {
    super.initializeDriver();
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
      context.put(JCRTestContext.THREAD_NUMBER, ++threadCounter);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void prepare(final TestCase tc) {
    super.prepare(tc);
    try {
      // context = new JCRTestContext();
      initContext(tc, context);
      // testInstance(tc).doPrepare(tc, context);
      // context.put(JCRTestContext.THREAD_NUMBER, threadCounter++);
      //context.put(JCRTestContext.COUNTER, 0);
      test = testInstance(tc);
      test.doPrepare(tc, context);
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
      throw new RuntimeException(e);
    }
  }

  @Override
  public void finish(final TestCase tc) {
    // testInstance(tc).doFinish(tc, context);
    // super.finish(tc);
//     System.out.println("------------------- FINISH --------------"
//     //+tc.getParam("japex.resultTime")+" "
//     +tc.getParam("japex.actualRunTime")+" "
//     //+tc.getParam("japex.actualRunIterations")+" "
//     //+tc.getParam("japex.warmupTime")+" "
//     +tc.getParam("japex.runIterations")+" "
//     //+tc.getParam("japex.runTime")+" "
//     +getParam("japex.numberOfThreads")+" "
//     +tc.getParam("japex.resultUnit")+" "
//     //+tc.getParam("japex.resultValue")+" "
//    
//     );
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
    context.setSession(oneSession);

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
