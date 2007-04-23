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

import com.sun.japex.JapexDriverBase;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public abstract class JCRDriverBase extends JapexDriverBase {

  protected Repository repository;

  //protected Session session;

  @Override
  public void initializeDriver() {
    super.initializeDriver();
    String initializerName = getParam("initializer");
    try {
      JCRInitializer initializer = (JCRInitializer)
        Class.forName(initializerName).newInstance();
      initializer.initialize();
      repository = initializer.getRepository();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

  }

//  @Override
//  public void prepare() {
//    super.prepare();
//    try {
//      session = repository.login();
//    } catch (LoginException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    } catch (RepositoryException e) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//
//    System.out.println(">>>>>>>>>>>>>>>>>> JCRDriverBase.prepare() <<<<<<<<<<<<<<<<<<<<<");
//
//  }

}

