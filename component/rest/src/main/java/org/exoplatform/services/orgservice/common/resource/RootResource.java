/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.common.resource;

import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.organization.resource.OrganizationResource;


/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RootResource extends AbstractRestResource {
  
  public RootResource(RestCommandContext commandContext, String localHref) {
    super(commandContext, localHref);
  }

  @Override
  protected RestResource getChildByName(String childName) {
    if (OrganizationResource.MAP_NAME.equals(childName)) {
      return new OrganizationResource(commandContext, getLocalHref());
    }
    
    return new FakeResource(commandContext, getLocalHref());
  }

}
