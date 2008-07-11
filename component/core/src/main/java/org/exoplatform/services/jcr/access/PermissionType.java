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
package org.exoplatform.services.jcr.access;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: PermissionType.java 14515 2008-05-20 11:45:21Z ksm $
 */
public interface PermissionType {
  public static final String   READ              = "read";

  public static final String   ADD_NODE          = "add_node";

  public static final String   SET_PROPERTY      = "set_property";

  public static final String   REMOVE            = "remove";

  public static final String[] ALL               = new String[] { READ, ADD_NODE, SET_PROPERTY,
      REMOVE                                    };

  public static final String[] DEFAULT_AC        = new String[] { READ };

  public static final String   CHANGE_PERMISSION = ADD_NODE + "," + SET_PROPERTY + "," + REMOVE;
}
