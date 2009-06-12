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

public interface DeltaVConstants extends PropertyConstants {

  /**
   * WebDAV DeltaV checked-in property. See <a
   * href='http://www.ietf.org/rfc/rfc3253.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName CHECKEDIN      = new QName("DAV:", "checked-in");

  /**
   * WebDAV DeltaV checked-out property. See <a
   * href='http://www.ietf.org/rfc/rfc3253.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName CHECKEDOUT     = new QName("DAV:", "checked-out");

  /**
   * WebDAV DeltaV label-name-set property. See <a
   * href='http://www.ietf.org/rfc/rfc3253.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName LABELNAMESET   = new QName("DAV:", "label-name-set");

  /**
   * WebDAV DeltaV predecessor-set property. See <a
   * href='http://www.ietf.org/rfc/rfc3253.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName PREDECESSORSET = new QName("DAV:", "predecessor-set");

  /**
   * WebDAV DeltaV successor-set property. See <a
   * href='http://www.ietf.org/rfc/rfc3253.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName SUCCESSORSET   = new QName("DAV:", "successor-set");

  /**
   * WebDAV DeltaV version-history property. See <a
   * href='http://www.ietf.org/rfc/rfc3253.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName VERSIONHISTORY = new QName("DAV:", "version-history");

  /**
   * WebDAV DeltaV version-name property. See <a
   * href='http://www.ietf.org/rfc/rfc3253.txt'>Versioning Extensions to
   * WebDAV</a> for more information.
   */
  QName VERSIONNAME    = new QName("DAV:", "version-name");

}
