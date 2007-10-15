/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.acl;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class Privileges {

  public static final String READ = "read";
  public static final String WRITE = "write";
  
  public static final String WRITE_PROPERTIES = "write-properties";
  
  public static final String WRITE_CONTENT = "write-content";
  
  public static final String UNLOCK = "unlock";
  
  public static final String READ_ACL = "read-acl";
  public static final String WRITE_ACL = "write-acl";
    
  public static final String BIND = "bind";
  public static final String UNBIND = "unbind";
  
  public static final String ALL = "all";
  
}
