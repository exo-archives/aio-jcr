/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.orgservice.command.RestCommand;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface RestService {
  
  RestCommand getCommand(String commandName);
  
  OrganizationService getOrganizationService();

}
