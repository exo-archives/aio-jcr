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
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.Serializable;

import org.exoplatform.services.jcr.datamodel.QPath;

/**
 * Created by The eXo Platform SAS. <br/>
 * 
 * Base class for WorkspaceCache keys.<br/>
 * 
 * Date: 10.06.2008<br/>
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public abstract class CacheKey implements Serializable {

  /**
   * Tell if this key is descendant of given path. I.e. is ItemData cached with this key belong to
   * ItemData hierarchy cached with the path.
   * 
   * @param path
   *          of possible descentand
   * @return true if yes
   */
  abstract boolean isDescendantOf(QPath path);
}
