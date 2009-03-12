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
 * @version $Id: $
 */

public interface PropertyConstants {

  QName  CHILDCOUNT           = new QName("DAV:", "childcount");

  QName  CREATIONDATE         = new QName("DAV:", "creationdate");

  QName  DISPLAYNAME          = new QName("DAV:", "displayname");

  QName  GETCONTENTLANGUAGE   = new QName("DAV:", "getcontentlanguage");

  QName  GETCONTENTLENGTH     = new QName("DAV:", "getcontentlength");

  QName  GETCONTENTTYPE       = new QName("DAV:", "getcontenttype");

  QName  GETLASTMODIFIED      = new QName("DAV:", "getlastmodified");

  QName  HASCHILDREN          = new QName("DAV:", "haschildren");

  QName  ISCOLLECTION         = new QName("DAV:", "iscollection");

  QName  ISFOLDER             = new QName("DAV:", "isfolder");

  QName  ISROOT               = new QName("DAV:", "isroot");

  QName  ISVERSIONED          = new QName("DAV:", "isversioned");

  QName  PARENTNAME           = new QName("DAV:", "parentname");

  QName  RESOURCETYPE         = new QName("DAV:", "resourcetype");

  QName  SUPPORTEDLOCK        = new QName("DAV:", "supportedlock");

  QName  LOCKDISCOVERY        = new QName("DAV:", "lockdiscovery");

  QName  SUPPORTEDMETHODSET   = new QName("DAV:", "supported-method-set");

  QName  LOCKSCOPE            = new QName("DAV:", "lockscope");

  QName  LOCKTYPE             = new QName("DAV:", "locktype");

  QName  OWNER                = new QName("DAV:", "owner");

  QName  EXCLUSIVE            = new QName("DAV:", "exclusive");

  QName  WRITE                = new QName("DAV:", "write");

  QName  ORDERING_TYPE        = new QName("DAV:", "ordering-type");

  // date and time patterns for DAV:creationdate and DAV:getlastmodified

  String CREATION_PATTERN     = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  String MODIFICATION_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

}
