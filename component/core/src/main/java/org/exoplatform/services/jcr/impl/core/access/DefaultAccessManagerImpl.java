/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core.access;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SARL        .
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
