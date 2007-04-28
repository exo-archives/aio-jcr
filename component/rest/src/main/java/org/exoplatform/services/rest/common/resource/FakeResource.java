/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.rest.common.resource;

import org.exoplatform.services.rest.RestCommandContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class FakeResource extends AbstractRestResource {
  
  public FakeResource(RestCommandContext commandContext, String localHref) {
    super(commandContext, localHref);
  }

}

