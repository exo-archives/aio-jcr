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
 * @version $Id: PermissionType.java 12843 2007-02-16 09:11:18Z peterit $
 */
public interface PermissionType {
    public final static String READ = "read";
    public final static String ADD_NODE = "add_node";
    public final static String SET_PROPERTY = "set_property";
    public final static String REMOVE = "remove";
    public final static String[] ALL = new String[] {READ, ADD_NODE, SET_PROPERTY, REMOVE};
    public final static String[] DEFAULT_AC = new String[] {READ};
    public final static String CHANGE_PERMISSION = ADD_NODE+","+SET_PROPERTY+","+REMOVE;
}
