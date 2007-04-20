/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.filters.jcr;

import java.util.Enumeration;

import javax.jcr.Repository;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.container.client.http.HttpClientInfo;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.security.SecurityService;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: Helper.java 13081 2007-03-01 15:32:25Z tuan08 $
 */

public class Helper {

  private static Log log = ExoLogger.getLogger("jcr.Helper");

  public static Repository requestRepository(HttpServletRequest httpRequest, String repoName,
      boolean useRmi) throws Exception {
    Repository repository = null;
    if (repoName == null)
      repository = (Repository) httpRequest.getSession().getAttribute("repo");
    if (repository == null) {
      /*
       * if (useRmi) { RepositoryFactory factory = new RepositoryFactory();
       * repository = factory.getRepository(repoName); } else {
       */
      InitialContext ctx = new InitialContext();
      try {
        Object obj = ctx.lookup(repoName);
        if (obj instanceof Repository) {
          repository = (Repository) obj;
        } else {
          obj = ctx.lookup("java:comp/env/" + repoName);
          if (obj instanceof Repository) {
            repository = (Repository) obj;
          } else {
            log.info("Can't cast object " + obj + " as Repository class object");
          }
        }
      } catch (NamingException e) {
        System.err.println("Repository not bound in JNDI with name '" + repoName + "', "
            + e.getMessage());
        repository = (Repository) ctx.lookup("java:comp/env/jcr/" + repoName);
      }
    }
    httpRequest.getSession().setAttribute("repo", repository);
    // System.out.println(" -- repository: " + repository);
    return repository;
  }

  static public void removeAttributes(HttpSession session) {
    Enumeration e = session.getAttributeNames();
    while (e.hasMoreElements()) {
      String key = (String) e.nextElement();
      session.removeAttribute(key);
    }
  }

  //private static void newSessionContainer(HttpServletRequest request_) throws Exception {
  private static void setUser(HttpServletRequest request_) throws Exception {
    StandaloneContainer container_ = StandaloneContainer.getInstance();
    HttpSession session = request_.getSession();
    // removeAttributes(session);
    SecurityService securityService = (SecurityService) container_
        .getComponentInstanceOfType(SecurityService.class);
    securityService.setCurrentUser(request_.getRemoteUser());
    OrganizationService orgService = (OrganizationService) container_
        .getComponentInstanceOfType(OrganizationService.class);
    UserProfile userProfile = orgService.getUserProfileHandler().findUserProfileByName(
        request_.getRemoteUser());
    if (userProfile == null) {
      userProfile = orgService.getUserProfileHandler().createUserProfileInstance();
    }

    /*container_.removeSessionContainer(session.getId());
    SessionContainer scontainer_ = container_.createSessionContainer(session.getId());
    scontainer_.setClientInfo(new HttpClientInfo(request_));
    scontainer_.setUniqueId(session.getId());
    OrganizationService orgService = (OrganizationService) container_
        .getComponentInstanceOfType(OrganizationService.class);
    UserProfile userProfile = orgService.getUserProfileHandler().findUserProfileByName(
        scontainer_.getOwner());
    if (userProfile == null) {
      userProfile = orgService.getUserProfileHandler().createUserProfileInstance();
    }
    scontainer_.registerComponentInstance(UserProfile.class, userProfile);*/
  }

  public static void tuneRequest(HttpServletRequest request_) {
    try {
      //newSessionContainer(request_);
      setUser(request_);
    } catch (Exception e) {
    }
  }

}
