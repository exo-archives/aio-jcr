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
package org.exoplatform.services.jcr.impl.ext.action;

import org.exoplatform.services.command.action.ActionMatcher;
import org.exoplatform.services.command.action.Condition;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class SessionEventMatcher implements ActionMatcher {

  /**
   * Key describe an Event name to be listened to.
   */
  public static final String    EVENTTYPE_KEY        = "types";

  /**
   * Key describe a workspace
   */
  public static final String    WORKSPACE_KEY        = "workspaces";

  /**
   * Key describe an Item absolute paths
   */
  public static final String    PATH_KEY             = "paths";

  @Deprecated
  public static final String    NODETYPE_KEY         = "nodeType";

  @Deprecated
  public static final String    PARENT_NODETYPES_KEY = "parentNodeTypes";

  /**
   * Key describe an InternalQName[] array of current node NodeType names.
   */
  public static final String    NODETYPES_KEY        = "nodeTypes";

  private final int             eventTypes;

  private final String[]        workspaces;

  private final QPath[]         paths;

  private boolean               isDeep;

  private final InternalQName[] nodeTypeNames;

  public SessionEventMatcher(int eventTypes,
                             QPath[] paths,
                             boolean isDeep,
                             String[] workspaces,
                             InternalQName[] nodeTypeNames) {
    super();
    this.eventTypes = eventTypes;
    this.paths = paths;
    this.isDeep = isDeep;

    this.nodeTypeNames = nodeTypeNames;
    this.workspaces = workspaces;
  }

  public String dump() {
    String str = "SessionEventMatcher: " + eventTypes + "\n";

    if (paths != null) {
      str += "Paths (isDeep=" + isDeep + "):\n";
      for (QPath p : paths) {
        str += p.getAsString() + "\n";
      }
    }

    if (nodeTypeNames != null) {
      str += "Node Types:\n";
      for (InternalQName n : nodeTypeNames) {
        str += n.getAsString() + "\n";
      }
    }

    return str;
  }

  public final boolean match(Condition conditions) {

    if (conditions.get(EVENTTYPE_KEY) == null
        || !isEventTypeMatch((Integer) conditions.get(EVENTTYPE_KEY))) {
      return false;
    }

    if (!isPathMatch((QPath) conditions.get(PATH_KEY))) {
      return false;
    }

    if (nodeTypeNames != null) {
      if (!isNodeTypesMatch((InternalQName[]) conditions.get(NODETYPES_KEY))) {
        return false;
      }
    }

    if (!isWorkspaceMatch((String) conditions.get(WORKSPACE_KEY))) {
      return false;
    }

    return internalMatch(conditions);
  }

  private boolean isEventTypeMatch(int type) {
    return (eventTypes & type) > 0;
  }

  private boolean isNodeTypesMatch(InternalQName[] nodeTypes) {
    if (this.nodeTypeNames == null || nodeTypes == null)
      return true;
    for (InternalQName nt : nodeTypeNames) {
      for (InternalQName searchNt : nodeTypes) {
        if (nt.equals(searchNt))
          return true;
      }
    }
    return false;
  }

  private boolean isPathMatch(QPath itemPath) {
    if (this.paths == null || itemPath == null)
      return true;

    for (QPath p : paths) {
      if (itemPath.equals(p) || itemPath.isDescendantOf(p, !isDeep))// TODO is Child
        return true;
    }

    return false;
  }

  private boolean isWorkspaceMatch(String workspace) {
    if (this.workspaces == null || workspace == null)
      return true;
    for (String ws : workspaces) {
      if (ws.equals(workspace))
        return true;
    }

    return false;
  }

  protected boolean internalMatch(Condition conditions) {
    return true;
  }
}
