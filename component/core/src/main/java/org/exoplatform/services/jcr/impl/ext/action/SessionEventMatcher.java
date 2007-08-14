/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.ext.action;

import javax.jcr.nodetype.NodeType;

import org.exoplatform.services.command.action.ActionMatcher;
import org.exoplatform.services.command.action.Condition;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeType;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: SessionEventMatcher.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class SessionEventMatcher implements ActionMatcher {

  public static final String EVENTTYPE_KEY        = "types";

  public static final String WORKSPACE_KEY        = "workspaces";

  public static final String PATH_KEY             = "paths";

  public static final String NODETYPE_KEY         = "nodeType";

  public static final String PARENT_NODETYPES_KEY = "parentNodeTypes";

  public static final String NODETYPES_KEY        = "nodeTypes";

  private int                eventTypes;

  private String[]           workspaces;

  private QPath[]            paths;

  private boolean            isDeep;

  @Deprecated
  private InternalQName[]    parentNodeTypeNames;

  @Deprecated
  private InternalQName[]    nodeTypeName;

  private InternalQName[]    nodeTypeNames;

  public SessionEventMatcher(int eventTypes,
      QPath[] paths,
      boolean isDeep,
      InternalQName[] nodeTypeName,
      InternalQName[] parentNodeTypeNames,
      String[] workspaces,
      InternalQName[] nodeTypeNames) {
    super();
    this.eventTypes = eventTypes;
    this.paths = paths;
    this.isDeep = isDeep;
    this.nodeTypeName = nodeTypeName;
    this.parentNodeTypeNames = parentNodeTypeNames;
    this.nodeTypeNames = nodeTypeNames;
    this.workspaces = workspaces;
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
    } else {
      if (!isParentNodeTypesMatch((NodeType[]) conditions.get(PARENT_NODETYPES_KEY))) {
        return false;
      }

      if (!isNodeTypeMatch((InternalQName) conditions.get(NODETYPE_KEY))) {
        return false;
      }
    }
    if (!isWorkspaceMatch((String) conditions.get(WORKSPACE_KEY))) {
      return false;
    }

    return internalMatch(conditions);
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

    if (parentNodeTypeNames != null) {
      str += "Parent Node Types:\n";
      for (InternalQName n : parentNodeTypeNames) {
        str += n.getAsString() + "\n";
      }
    }

    return str;
  }

  protected boolean internalMatch(Condition conditions) {
    return true;
  }

  private boolean isEventTypeMatch(int type) {
    return (eventTypes & type) > 0;
  }

  private boolean isPathMatch(QPath itemPath) {
    if (this.paths == null || itemPath == null)
      return true;

    for (QPath p : paths) {
      if (itemPath.equals(p) || itemPath.isDescendantOf(p, !isDeep))
        return true;
    }

    return false;
  }

  @Deprecated
  private boolean isParentNodeTypesMatch(NodeType[] nodeType) {
    if (this.parentNodeTypeNames == null || nodeType == null)
      return true;
    for (InternalQName nt : parentNodeTypeNames) {
      for (NodeType searchNt : nodeType) {
        if (((ExtendedNodeType) searchNt).getQName().equals(nt))
          return true;
      }
    }

    return false;
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

  @Deprecated
  private boolean isNodeTypeMatch(InternalQName nodeType) {
    if (this.nodeTypeName == null || nodeType == null)
      return true;

    for (InternalQName nt : nodeTypeName) {
      if (nt.equals(nodeType))
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
}
