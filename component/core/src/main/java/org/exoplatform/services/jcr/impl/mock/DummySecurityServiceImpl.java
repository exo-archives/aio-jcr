/*
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail. 
 */

package org.exoplatform.services.jcr.impl.mock;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.SecurityService;
import org.exoplatform.services.security.SubjectEventListener;
import org.exoplatform.services.security.sso.SSOAuthenticationConfig;


/**
 * Created y the eXo platform team
 * User: Benjamin Mestrallet
 * Date: 28 avr. 2004
 */
public class DummySecurityServiceImpl implements SecurityService {
  
  protected static Log log = ExoLogger.getLogger("jcr.DummySecurityServiceImpl");

  private Map subjects;

  public DummySecurityServiceImpl() {
    subjects = new HashMap();
  }

  public boolean authenticate(String login, String password) throws Exception {
    if (password == null || "".equals(password)) {
      log.debug("password must not be null or empty");
      throw new Exception("password must not be null or empty");
    }
    return true;
  }

  public void setUpAndCacheSubject(String userName, Subject value) /*throws Exception*/ {
    subjects.put(userName, value);
  }

  public Subject getSubject(String userName) {
    log.debug("get subject for user " + userName);
    return (Subject) subjects.get(userName);
  }

  public void removeSubject(String userName) {
    log.debug("remove subject for user " + userName);
    subjects.remove(userName);
  }
  
  public Log getLog() { return log ;  }

  public boolean isUserInRole(String userName, String role) {
    return false;
  }

  public String getSSOAuthentication() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isSSOAuthentication() {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean isStandaloneAuthentication() {
    // TODO Auto-generated method stub
    return false;
  }

  public String getProxyTicket(String userName, String urlOfTargetService) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean hasMembershipInGroup(String user, String roleExpression) {
    // TODO Auto-generated method stub
    return false;
  }

  public boolean hasMembershipInGroup(String userId, String membershipName, String groupName) {
    return true;
  }

  public void addSubjectEventListener(SubjectEventListener subjectEventListener) {
    // TODO Auto-generated method stub
    
  }

  public void addSubjectEvenetListener(SubjectEventListener arg0) {
    // TODO Auto-generated method stub
    
  }

  public SSOAuthenticationConfig getSSOAuthenticationConfig() {
    // TODO Auto-generated method stub
    return null;
  }
}