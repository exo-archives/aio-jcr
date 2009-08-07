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

package org.exoplatform.services.jcr.webdav.util;

import javax.xml.namespace.QName;

/**
 * Created by The eXo Platform SAS Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id$
 */

public interface PropertyConstants {

  public static final QName  CHILDCOUNT           = new QName("DAV:", "childcount");

  public static final QName  CREATIONDATE         = new QName("DAV:", "creationdate");

  public static final QName  DISPLAYNAME          = new QName("DAV:", "displayname");

  public static final QName  GETCONTENTLANGUAGE   = new QName("DAV:", "getcontentlanguage");

  public static final QName  GETCONTENTLENGTH     = new QName("DAV:", "getcontentlength");

  public static final QName  GETCONTENTTYPE       = new QName("DAV:", "getcontenttype");

  public static final QName  GETLASTMODIFIED      = new QName("DAV:", "getlastmodified");

  public static final QName  HASCHILDREN          = new QName("DAV:", "haschildren");

  public static final QName  ISCOLLECTION         = new QName("DAV:", "iscollection");

  public static final QName  ISFOLDER             = new QName("DAV:", "isfolder");

  public static final QName  ISROOT               = new QName("DAV:", "isroot");

  public static final QName  ISVERSIONED          = new QName("DAV:", "isversioned");

  public static final QName  PARENTNAME           = new QName("DAV:", "parentname");

  public static final QName  RESOURCETYPE         = new QName("DAV:", "resourcetype");

  public static final QName  SUPPORTEDLOCK        = new QName("DAV:", "supportedlock");

  public static final QName  LOCKDISCOVERY        = new QName("DAV:", "lockdiscovery");

  public static final QName  SUPPORTEDMETHODSET   = new QName("DAV:", "supported-method-set");

  public static final QName  LOCKSCOPE            = new QName("DAV:", "lockscope");

  public static final QName  LOCKTYPE             = new QName("DAV:", "locktype");

  public static final QName  OWNER                = new QName("DAV:", "owner");

  public static final QName  EXCLUSIVE            = new QName("DAV:", "exclusive");

  public static final QName  WRITE                = new QName("DAV:", "write");

  public static final QName  ORDERING_TYPE        = new QName("DAV:", "ordering-type");

  // date and time patterns for DAV:creationdate and DAV:getlastmodified

  public static final String CREATION_PATTERN     = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  public static final String MODIFICATION_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

}
