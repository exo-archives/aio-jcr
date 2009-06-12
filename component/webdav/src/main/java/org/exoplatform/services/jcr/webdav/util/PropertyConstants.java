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
 * Created by The eXo Platform SAS Author : <a
 * href="gavrikvetal@gmail.com">Vitaly Guly</a>.
 * 
 * @version $Id: $
 */

public interface PropertyConstants {

  /**
   * WebDAV childcount property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  CHILDCOUNT           = new QName("DAV:", "childcount");

  /**
   * WebDAV creationdate property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  CREATIONDATE         = new QName("DAV:", "creationdate");

  /**
   * WebDAV displayname property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  DISPLAYNAME          = new QName("DAV:", "displayname");

  /**
   * WebDAV getcontentlanguage property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  GETCONTENTLANGUAGE   = new QName("DAV:", "getcontentlanguage");

  /**
   * WebDAV getcontentlength property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  GETCONTENTLENGTH     = new QName("DAV:", "getcontentlength");

  /**
   * WebDAV getcontenttype property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  GETCONTENTTYPE       = new QName("DAV:", "getcontenttype");

  /**
   * WebDAV property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  GETLASTMODIFIED      = new QName("DAV:", "getlastmodified");

  /**
   * WebDAV getlastmodified property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  HASCHILDREN          = new QName("DAV:", "haschildren");

  /**
   * WebDAV iscollection property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  ISCOLLECTION         = new QName("DAV:", "iscollection");

  /**
   * WebDAV isfolder property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  ISFOLDER             = new QName("DAV:", "isfolder");

  /**
   * WebDAV isroot property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  ISROOT               = new QName("DAV:", "isroot");

  /**
   * WebDAV isversioned property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  ISVERSIONED          = new QName("DAV:", "isversioned");

  /**
   * WebDAV parentname property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  PARENTNAME           = new QName("DAV:", "parentname");

  /**
   * WebDAV resourcetype property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  RESOURCETYPE         = new QName("DAV:", "resourcetype");

  /**
   * WebDAV supportedlock property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  SUPPORTEDLOCK        = new QName("DAV:", "supportedlock");

  /**
   * WebDAV lockdiscovery property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  LOCKDISCOVERY        = new QName("DAV:", "lockdiscovery");

  /**
   * WebDAV supported-method-set property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  SUPPORTEDMETHODSET   = new QName("DAV:", "supported-method-set");

  /**
   * WebDAV lockscope property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  LOCKSCOPE            = new QName("DAV:", "lockscope");

  /**
   * WebDAV locktype property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  LOCKTYPE             = new QName("DAV:", "locktype");

  /**
   * WebDAV owner property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  OWNER                = new QName("DAV:", "owner");

  /**
   * WebDAV exclusive property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  EXCLUSIVE            = new QName("DAV:", "exclusive");

  /**
   * WebDAV write property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  WRITE                = new QName("DAV:", "write");

  /**
   * WebDAV ordering-type property. See <a
   * href='http://www.ietf.org/rfc/rfc2518.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName  ORDERING_TYPE        = new QName("DAV:", "ordering-type");

  /**
   * jcr:data property.
   */
  QName  JCR_DATA             = new QName("jcr:", "data");

  /**
   * jcr:content property.
   */
  QName  JCR_CONTENT          = new QName("jcr:", "content");

  /**
   * Creation date pattern.
   */
  String CREATION_PATTERN     = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /**
   * Last-Modified date pattern.
   */
  String MODIFICATION_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

}
