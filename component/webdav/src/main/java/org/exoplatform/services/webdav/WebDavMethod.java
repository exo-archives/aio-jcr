/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.webdav;

import org.exoplatform.common.http.HTTPMethods;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
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
  
  public static final String LABEL          = "LABEL";

}
