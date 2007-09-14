/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class WebDavPortalSessionProviderFilter extends SessionProviderFilter {

  @Override
  protected ExoContainer getContainer() {
    return ExoContainerContext.getContainerByName("portal");
  }
  
}
