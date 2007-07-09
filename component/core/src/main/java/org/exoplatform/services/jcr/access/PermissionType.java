/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.access;
/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: PermissionType.java 12843 2007-02-16 09:11:18Z peterit $
 */
public interface PermissionType {
    public static String READ = "read";
    public static String ADD_NODE = "add_node";
    public static String SET_PROPERTY = "set_property";
    public static String REMOVE = "remove";
    public static String[] ALL = new String[] {READ, ADD_NODE, SET_PROPERTY, REMOVE};
    public static String[] DEFAULT_AC = new String[] {READ};
    public static String CHANGE_PERMISSION = ADD_NODE+","+SET_PROPERTY+","+REMOVE;
}
