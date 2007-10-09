/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav;

import org.exoplatform.common.http.HTTPMethods;

/**
 * Created by The eXo Platform SARL Author : Vitaly Guly
 * <gavrik-vetal@ukr.net/mail.ru>
 * 
 * @version $Id: $
 */

public interface WebDavMethod extends HTTPMethods {

  public static final String PROPFIND       = "PROPFIND";

  public static final String REPORT         = "REPORT";

  public static final String COPY           = "COPY";

  public static final String OPTIONS        = "OPTIONS";

  public static final String MKCOL          = "MKCOL";

  public static final String DELETE         = "DELETE";

  public static final String HEAD           = "HEAD";

  public static final String MOVE           = "MOVE";

  public static final String PUT            = "PUT";

  public static final String GET            = "GET";

  public static final String LOCK           = "LOCK";

  public static final String UNLOCK         = "UNLOCK";

  public static final String PROPPATCH      = "PROPPATCH";

  public static final String POST           = "POST";

  public static final String ORDERPATCH     = "ORDERPATCH";

  public static final String VERSIONCONTROL = "VERSION-CONTROL";

  public static final String CHECKIN        = "CHECKIN";

  public static final String CHECKOUT       = "CHECKOUT";

  public static final String UNCHECKOUT     = "UNCHECKOUT";

  public static final String RESTORE        = "RESTORE";

  public static final String SEARCH         = "SEARCH";

}
