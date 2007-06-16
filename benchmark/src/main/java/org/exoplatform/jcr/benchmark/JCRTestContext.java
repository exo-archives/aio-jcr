/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark;

import java.util.HashMap;

import javax.jcr.Session;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public final class JCRTestContext extends HashMap<String, Object> {

  public static final String SESSION       = "session";

  private int counter = 0;
  
  private String name;
  
  public JCRTestContext(String contextName) {
    this.name = contextName;
  }

  public void setSession(Session session) {
    put(SESSION, session);
  }

  public Session getSession() {
    return (Session) get(SESSION);
  }

  public String generateUniqueName(String prefix) {
    String uname = prefix+"-"+name+"-"+(counter++);
    return uname;
  }
  
  
}
