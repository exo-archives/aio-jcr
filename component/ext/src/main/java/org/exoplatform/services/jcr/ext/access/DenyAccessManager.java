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
package org.exoplatform.services.jcr.ext.access;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.core.ItemImpl;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.services.organization.OrganizationService;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class DenyAccessManager extends AccessManager {
  private String denyName = "";
  public DenyAccessManager(RepositoryEntry config, WorkspaceEntry wsConfig,
      OrganizationService orgService) throws RepositoryException, RepositoryConfigurationException {
    super(config, wsConfig, orgService);
    this.denyName = wsConfig.getAccessManager().getParameterValue("name");
    log.info("DenyAccessManager created");
  }
  @Override
  public boolean hasPermission(AccessControlList acl, String[] permission, String userId) {
    if (super.hasPermission(acl, permission, userId)) {
      if (userId != "admin" && userId != SystemIdentity.SYSTEM) {
        if (context() != null) {
          int ivent = ((Integer) context().get("event")).intValue();
          if (ivent == ExtendedEvent.READ) {
            ItemImpl curItem = (ItemImpl) context().get("currentItem");

            // String name = path.getName().getAsString();
            if (curItem != null && curItem.getInternalName().getAsString().indexOf(denyName)>-1) {
              log.debug("DenyAccessManager permission deny by rool name='"+denyName+"'");
              return false;
            }
          }
        } else {
          log.warn("Context = null");
        }
      }
      return true;
    }
    return false;
  }
}
