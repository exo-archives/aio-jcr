/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice;

import org.exoplatform.services.orgservice.organization.template.user.UserTemplate;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RequestTemplates {

  public static final String [][]TEMPLATES = {
    {UserTemplate.TEMPLATENAME, UserTemplate.class.getCanonicalName()}
  };
  
}

