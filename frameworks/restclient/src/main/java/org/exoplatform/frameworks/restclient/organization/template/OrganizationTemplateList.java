/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.restclient.organization.template;

import org.exoplatform.frameworks.restclient.common.template.AbstractTemplateList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class OrganizationTemplateList extends AbstractTemplateList {
  
  public static final String LIST_USER = "user-list";
  
  public static final String LIST_GROUP = "";
  
  public static final String LIST_MEMBERSHIP = "";
  
  public static final String LIST_MEMBERSHIPTYPE = "";

  public OrganizationTemplateList(String xmlName) {
    super(xmlName);
  }
  
}

