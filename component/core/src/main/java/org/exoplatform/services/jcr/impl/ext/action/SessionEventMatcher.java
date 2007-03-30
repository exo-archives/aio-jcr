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
import org.exoplatform.services.jcr.datamodel.InternalQPath;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: SessionEventMatcher.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class SessionEventMatcher implements ActionMatcher {

  public static final String EVENTTYPE_KEY = "types";
  public static final String WORKSPACE_KEY = "workspaces";
  public static final String PATH_KEY = "paths";
  public static final String NODETYPE_KEY = "nodeTypes";
  public static final String PARENT_NODETYPES_KEY = "parentNodeTypes";

  private int eventTypes;
  private String[] workspaces;
  private InternalQPath[] paths;
  private boolean isDeep;
  private InternalQName[] parentNodeTypeNames; 
  private InternalQName[] nodeTypeNames;


  public SessionEventMatcher(int eventTypes, InternalQPath[] paths, 
      boolean isDeep, InternalQName[] nodeTypeNames,
      InternalQName[] parentNodeTypeNames, String[] workspaces) {
    super();
    this.eventTypes = eventTypes;
    this.paths = paths;
    this.isDeep = isDeep;
    this.nodeTypeNames = nodeTypeNames;
    this.parentNodeTypeNames = parentNodeTypeNames;
    this.workspaces = workspaces;
  }

  public final boolean match(Condition conditions) {
    
    if(conditions.get(EVENTTYPE_KEY) == null ||
       !isEventTypeMatch((Integer)conditions.get(EVENTTYPE_KEY))) {
      return false;
    }
    

    if(!isPathMatch((InternalQPath)conditions.get(PATH_KEY))) {
       return false;
     }
    
    if(!isParentNodeTypesMatch((NodeType[])conditions.get(PARENT_NODETYPES_KEY))) {
       return false;
    } 

    if(!isNodeTypeMatch((InternalQName)conditions.get(NODETYPE_KEY))) {
       return false;
    }
    
    if(!isWorkspaceMatch((String)conditions.get(WORKSPACE_KEY))) {
      return false;
   } 


    return internalMatch(conditions);
  }
  
  public String dump() {
    String str = "SessionEventMatcher: "+eventTypes+"\n";

    if (paths != null) {
      str += "Paths (isDeep="+isDeep+"):\n";
      for (InternalQPath p : paths) {
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
  
  private boolean isPathMatch(InternalQPath itemPath) {
    if (this.paths == null || itemPath == null)
      return true;

    for(InternalQPath p : paths) {
      if(itemPath.equals(p) || itemPath.isDescendantOf(p, !isDeep))
        return true;
    }
    
    return false;
    //return path.equals(itemPath) || itemPath.isDescendantOf(path, !isDeep);
  }

  private boolean isParentNodeTypesMatch(NodeType[] nodeType) {
    if (this.parentNodeTypeNames == null || nodeType == null)
      return true;
    for(InternalQName nt : parentNodeTypeNames) {
      for (NodeType searchNt : nodeType) {
//        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>REC>>>>>>>>>>> "+
//            searchNt.getName()+" "+nt+" "+((ExtendedNodeType)searchNt).getQName().equals(nt));
        if(((ExtendedNodeType)searchNt).getQName().equals(nt))
          return true;
      }
    }
    
    return false;
  }
  
  private boolean isNodeTypeMatch(InternalQName nodeType) {
    if (this.nodeTypeNames == null || nodeType == null)
      return true;

    for(InternalQName nt : nodeTypeNames) {
      if(nt.equals(nodeType))
        return true;
    }
    
    return false;

//    return nodeTypeName.equals(nodeType); 
  }
  
  private boolean isWorkspaceMatch(String workspace) {
    if (this.workspaces == null || workspace == null)
      return true;
    for(String ws : workspaces) {
      if(ws.equals(workspace))
        return true;
    }
    
    return false;
  }
}
