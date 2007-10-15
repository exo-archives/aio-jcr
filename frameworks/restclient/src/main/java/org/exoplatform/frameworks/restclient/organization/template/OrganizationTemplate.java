/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.restclient.organization.template;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.restclient.common.template.AbstractTemplate;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class OrganizationTemplate extends AbstractTemplate {
  
  public static final String TEMPLATE_USER = "user";
  
  public static final String TEMPLATE_GROUP = "group";
  
  public static final String TEMPLATE_MEMBERSHIP = "membership";
  
  public static final String TEMPLATE_MEMBERSHIPTYPE = "membership-type";
  
  
  public static final String XML_USER = "user"; 
  
  public static final String XML_USERID = "user-id";
  
  public static final String XML_USERPASSWORD = "user-password";

  public static final String XML_EMAIL = "email";
  
  public static final String XML_FIRSTNAME = "first-name";
  
  public static final String XML_LASTNAME = "last-name";
  
  public static final String XML_PROFILE = "profile";
  
  public static final String XML_MEMBERSHIP = "membership";
  
  public static final String XML_GROUPID = "group-id";
  
  public static final String XML_GROUP = "group";
  
  public static final String XML_LABEL = "label";
  
  public static final String XML_DESCRIPTION = "description";
  
  public static final String XML_MEMBERSHIPTYPEID = "membership-type-id";
  
  public static final String XML_MEMBERSHIPTYPE = "membership-type";
  
  public static final String XML_MEMBERSHIPID = "membership-id";
  
  
  public OrganizationTemplate(String xmlName) {
    super(xmlName);
    Log.info("XMLNAME: " + xmlName);
  }
  
}

