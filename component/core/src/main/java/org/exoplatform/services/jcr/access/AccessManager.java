/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.access;

import java.util.ArrayList;
import java.util.Collection;
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
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: AccessManager.java 12004 2007-01-17 12:03:57Z geaz $
 */

public abstract class AccessManager {

  protected static Log                          log           = ExoLogger
                                                                  .getLogger("jcr.AccessManager");

  protected final Map<String, String>           parameters;

  private final OrganizationService             orgService;

  private static ThreadLocal<InvocationContext> contextHolder = new ThreadLocal<InvocationContext>();

  protected AccessManager(RepositoryEntry config,
      WorkspaceEntry wsConfig,
      OrganizationService orgService) throws RepositoryException {
    this.parameters = new HashMap<String, String>();
    if (wsConfig != null && wsConfig.getAccessManager() != null) {
      List<SimpleParameterEntry> paramList = wsConfig.getAccessManager().getParameters();
      for (SimpleParameterEntry param : paramList)
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
  public final boolean hasPermission(AccessControlList acl, String permission, String userId) throws RepositoryException {
    return hasPermission(acl, parseStringPermissions(permission), userId);
  }

  /**
   * @param acl
   * @param permission
   * @param userId
   * @return
   */
  public boolean hasPermission(AccessControlList acl, String[] permission, String userId) {

    if (userId.equals(SystemIdentity.SYSTEM)) {
      // SYSTEM has permission everywhere
      return true;
    } else if (userId.equals(acl.getOwner())) {
      // Current user is owner of node so has all privileges
      return true;
    } else if (userId.equals(SystemIdentity.ANONIM)
        && (permission.length > 1 || !permission[0].equals(PermissionType.READ))) {
      // Anonim does not have WRITE permission even for ANY
      return false;
    } else {
      // Check Other with Org service
      for (AccessControlEntry ace : acl.getPermissionEntries()) {
        if (isUserMatch(ace.getIdentity(), userId)
            && isPermissionMatch(ace.getPermission(), permission))
          return true;
      }
    }
    return false;
  }

  protected boolean isUserMatch(String identity, String userId) {
    if (identity.equals(SystemIdentity.ANY)) // any
      return true;
    if (identity.indexOf(":") == -1) 
      // just user
      return identity.equals(userId);
    // group
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
        log.debug("Check of user " + userId + " membership. Test if " + groupName + " == "
            + group.getId() + " " + groupName.equals(group.getId()));
        if (groupName.equals(group.getId()))
          return true;
      }
      
    } else {
      try {
        Collection memberships = orgService.getMembershipHandler()
            .findMembershipsByUserAndGroup(userId, groupName);
        for (Object obj : memberships) {
          Membership membership = (Membership) obj;
          if (membership.getMembershipType().equals(membershipName))
            return true;
        }
      } catch (Exception e) {
        log.error("AccessManager.isUserMatch() failed " + e);
        return false;
      }
    }
    return false;
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

  protected final boolean isPermissionMatch(String existedPermission, String[] testPermissions) {
    try {
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