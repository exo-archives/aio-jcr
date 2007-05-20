/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.access;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: AccessManager.java 12004 2007-01-17 12:03:57Z geaz $
 */

public abstract class AccessManager {

  protected static Log log = ExoLogger.getLogger("jcr.AccessManager");

  protected final Map <String, String> parameters;
  
  private final OrganizationService orgService;
  
  private static ThreadLocal <InvocationContext> contextHolder = new ThreadLocal<InvocationContext>(); 
  
  protected AccessManager(RepositoryEntry config, WorkspaceEntry wsConfig
      ,OrganizationService orgService)
      throws RepositoryException {
//    this.accessControlPolicy = config.getAccessControl();
    this.parameters = new HashMap <String, String>();
//    AccessManagerEntry amConfig = wsConfig.getAccessManager();
    if(wsConfig != null && wsConfig.getAccessManager() != null) {
      List <SimpleParameterEntry> paramList = wsConfig.
      getAccessManager().getParameters();
      for(SimpleParameterEntry param : paramList) 
        parameters.put(param.getName(), param.getValue());
    }
    this.orgService = orgService;
  }
  

  
  protected final InvocationContext context() {
    return contextHolder.get();
  }
  
  public final void setContext(InvocationContext context) {
    contextHolder.set(context);
  }
  
  /**
   * @param acl
   * @param permission
   * @param userId
   * @return
   * @throws RepositoryException
   */
  public final boolean hasPermission(AccessControlList acl, String permission,
      String userId) throws RepositoryException {
    return hasPermission(acl, parseStringPermissions(permission), userId);
  }

  /**
   * @param acl
   * @param permission
   * @param userId
   * @return
   */
  public boolean hasPermission(AccessControlList acl, String[] permission,
      String userId) {
    
    if (userId.equals(SystemIdentity.SYSTEM)) {
      // SYSTEM has permission everywhere
      return true;
    } else if (userId.equals(acl.getOwner())) {      
      //Current user is owner of node so has all privileges
      return true;
    } else if (userId.equals(SystemIdentity.ANONIM) 
        && (permission.length > 1 || !permission[0].equals(PermissionType.READ)) ) {
      //Anonim does not have WRITE permission even for ANY
      //System.out.println(">>>userId "+userId+" "+permission[0]);
      return false;
    } else {
      // Check Other with Org service
      for(AccessControlEntry ace: acl.getPermissionEntries()) {
        if (isUserMatch(ace.getIdentity(), userId) && 
            isPermissionMatch(ace.getPermission(), permission))
          return true;
      }
    }
    //log.debug("Has Permission == false for "+userId);
    return false;
  }

  protected boolean isUserMatch(String identity, String userId) {
    if (identity.equals(SystemIdentity.ANY)) //any
      return true;
    if (identity.indexOf(":") == -1) // || identity.equals(SystemIdentity.ANONIM)) 
      // just user
      return identity.equals(userId);
    else {// group
      Iterator groups;
      String membershipName = identity.substring(0, identity.indexOf(":"));
      String groupName = identity.substring(identity.indexOf(":") + 1);
      try {  
        groups = orgService.getGroupHandler().findGroupsOfUser(userId).iterator();
      } catch (Exception e) {
        log.error("AccessManager.isUserMatch() failed " + e);
        return false;
      }
      if ("*".equals(membershipName)) {
        while (groups.hasNext()) {
          Group group = (Group) groups.next();
          if(log.isDebugEnabled()) {
            log.debug("Check of user "+userId+" membership. Test if "+
              groupName+" == "+group.getId()+" "+groupName.equals(group.getId()));
          }
          if (groupName.equals(group.getId())) 
            return true;
        }
      } else {
        while (groups.hasNext()) {
          Group group = (Group) groups.next();
          try {
            Iterator memberships = 
              orgService.getMembershipHandler().findMembershipsByUserAndGroup(userId, group.getId()).iterator();
            while (memberships.hasNext()) {
              Membership membership = (Membership) memberships.next();
              if (membership.getMembershipType().equals(membershipName))
                return true;
            }
          } catch (Exception e) {
            log.error("AccessManager.isUserMatch() failed " + e);
            return false;
          }
        }
      }
      return false;
    }
  }


  private static String[] parseStringPermissions(String str) {
    ArrayList permissions = new ArrayList();
    StringTokenizer parser = new StringTokenizer(str, ",");
    while (parser.hasMoreTokens()) {
      String token = parser.nextToken();
      permissions.add(token);
    }
    String[] perms = new String[permissions.size()];
    for (int i = 0; i < perms.length; i++)
      perms[i] = (String) permissions.get(i);
    return perms;
  }

  protected final boolean isPermissionMatch(String existedPermission,
      String[] testPermissions) {
    try {
      //Value[] values = permProperty.getValues();
      for (int i = 0; i < testPermissions.length; i++) {
        if (existedPermission.equals(testPermissions[i]))
          return true;
      }
    } catch (Exception e) {
      log.error("AccessManager.isPermissionMatch() exception " + e);
    }
    return false;
  }

}