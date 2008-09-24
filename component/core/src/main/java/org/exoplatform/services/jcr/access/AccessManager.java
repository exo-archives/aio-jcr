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
import java.util.HashMap;
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
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: AccessManager.java 14518 2008-05-20 13:27:19Z ksm $
 */

public abstract class AccessManager {

  protected static Log                          log           = ExoLogger.getLogger("jcr.AccessManager");

  protected final Map<String, String>           parameters;

  private static ThreadLocal<InvocationContext> contextHolder = new ThreadLocal<InvocationContext>();

  protected AccessManager(RepositoryEntry config, WorkspaceEntry wsConfig) throws RepositoryException {

    this.parameters = new HashMap<String, String>();
    if (wsConfig != null && wsConfig.getAccessManager() != null) {
      List<SimpleParameterEntry> paramList = wsConfig.getAccessManager().getParameters();
      for (SimpleParameterEntry param : paramList)
        parameters.put(param.getName(), param.getValue());
    }
  }

  protected final InvocationContext context() {
    return contextHolder.get();
  }

  public final void setContext(InvocationContext context) {
    contextHolder.set(context);
  }

  /**
   * Has permission.
   * 
   * @param acl
   *          access control list
   * @param permission
   *          permission
   * @param user
   *          user Identity
   * @return boolean
   * @throws RepositoryException
   */
  public final boolean hasPermission(AccessControlList acl, String permission, Identity user) throws RepositoryException {
    return hasPermission(acl, parseStringPermissions(permission), user);
  }

  /**
   * Has permission.
   * 
   * @param acl
   *          access control list
   * @param permission
   *          permissions array
   * @param user
   *          user Identity
   * @return boolean
   */
  public boolean hasPermission(AccessControlList acl, String[] permission, Identity user) {

    String userId = user.getUserId();

    if (userId.equals(SystemIdentity.SYSTEM)) {
      // SYSTEM has permission everywhere
      return true;
    } else if (userId.equals(acl.getOwner())) {
      // Current user is owner of node so has all privileges
      return true;
    } else if (userId.equals(SystemIdentity.ANONIM)) {
      List<String> anyPermissions = acl.getPermissions(SystemIdentity.ANY);

      if (anyPermissions.size() < permission.length)
        return false;

      for (int i = 0; i < permission.length; i++) {
        if (!anyPermissions.contains(permission[i]))
          return false;
      }

      return true;
    } else {

      if (acl.getPermissionsSize() > 0 && permission.length > 0) {
        // check permission to perform all of the listed actions
        for (int i = 0; i < permission.length; i++) {
          // check specific actions
          if (!isPermissionMatch(acl.getPermissionsList(), permission[i], user))
            return false;
        }
        return true;
      }
      return false;

    }
  }

  private String[] parseStringPermissions(String str) throws RepositoryException {
    List<String> permissions = new ArrayList<String>();
    StringTokenizer parser = new StringTokenizer(str, ",");

    while (parser.hasMoreTokens()) {
      String token = parser.nextToken();
      if (PermissionType.READ.equals(token) || PermissionType.ADD_NODE.equals(token)
          || PermissionType.REMOVE.equals(token) || PermissionType.SET_PROPERTY.equals(token)) {

        permissions.add(token);
      } else {
        throw new RepositoryException("Unknown permission entry " + token);

      }
    }
    return permissions.toArray(new String[permissions.size()]);
  }

  private boolean isPermissionMatch(List<AccessControlEntry> existedPermission,
                                    String testPermission,
                                    Identity user) {
    for (AccessControlEntry ace : existedPermission) {
      // match action
      if (ace.getPermission().equals(testPermission)) {
        // match any
        if (ace.getIdentity().equals(SystemIdentity.ANY))
          return true;
        else if (ace.getIdentity().indexOf(":") == -1) {
          // just user
          if (ace.getIdentity().equals(user.getUserId()))
            return true;

        } else if (user.isMemberOf(MembershipEntry.parse(ace.getIdentity())))
          return true;
      }
    }
    return false;
  }

}
