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

public interface DeltaVConstants extends PropertyConstants {

  public static final QName CHECKEDIN      = new QName("DAV:", "checked-in");

  public static final QName CHECKEDOUT     = new QName("DAV:", "checked-out");

  public static final QName LABELNAMESET   = new QName("DAV:", "label-name-set");

  public static final QName PREDECESSORSET = new QName("DAV:", "predecessor-set");

  public static final QName SUCCESSORSET   = new QName("DAV:", "successor-set");

  public static final QName VERSIONHISTORY = new QName("DAV:", "version-history");

  public static final QName VERSIONNAME    = new QName("DAV:", "version-name");

}
