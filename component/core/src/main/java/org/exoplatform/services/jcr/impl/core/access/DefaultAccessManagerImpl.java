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
package org.exoplatform.services.jcr.impl.core.access;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SAS.
 * @author Gennady Azarenkov
 * @version $Id: DefaultAccessManagerImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class DefaultAccessManagerImpl extends AccessManager {

  public DefaultAccessManagerImpl(RepositoryEntry config, WorkspaceEntry wsConfig
      ,OrganizationService orgService) throws RepositoryException {
    super(config, wsConfig, orgService);
  }
  
  public DefaultAccessManagerImpl(RepositoryEntry config, 
      OrganizationService orgService) throws RepositoryException {
    super(config, null, orgService);
  }

}
