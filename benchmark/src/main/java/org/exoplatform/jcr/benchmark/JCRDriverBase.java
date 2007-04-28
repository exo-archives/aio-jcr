/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark;

import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.exoplatform.jcr.benchmark.init.JCRInitializer;
import org.exoplatform.jcr.benchmark.jcrapi.AbstactTest;

import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class JCRDriverBase extends JapexDriverBase {

  protected Repository repository;

  protected Session    session;
  
  protected int    myNodeIndex;
  
  private final String packageName  = "org.exoplatform.jcr.benchmark.jcrapi.";

  private AbstactTest  test         = null;

  @Override
  public void initializeDriver() {
    super.initializeDriver();
    String initializerName = getParam("initializer");
    try {
      JCRInitializer initializer = (JCRInitializer) Class.forName(initializerName).newInstance();
      initializer.initialize();
      repository = initializer.getRepository();
      session = initializer.getSession();
      myNodeIndex = initializer.getMyNodeIndex();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  /*public void prepare(final TestCase tc) {
    ...
  }*/
  
  /*public void run(final TestCase tc) {
    ...    
  }*/

}
