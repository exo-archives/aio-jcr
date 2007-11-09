/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command.core;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.exoplatform.frameworks.jcr.command.DefaultKeys;
import org.exoplatform.frameworks.jcr.command.JCRAppContext;
/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: GetNodeCommand.java 5878 2006-05-31 09:44:14Z peterit $
 */

public class GetNodeCommand implements Command {
  
  private String pathKey = DefaultKeys.PATH;
  private String workspaceKey = DefaultKeys.WORKSPACE;
  private String currentNodeKey = DefaultKeys.CURRENT_NODE;
  private String resultKey = DefaultKeys.RESULT;
  
  public boolean execute(Context context) throws Exception {
    
    String wsName = (String)context.get(workspaceKey);
    if(wsName != null)
      ((JCRAppContext)context).setCurrentWorkspace(wsName);
    Session session = ((JCRAppContext)context).getSession();
    String relPath = (String)context.get(pathKey);
        
    Node parentNode = (Node)session.getItem((String)context.get(currentNodeKey));
    
    context.put(resultKey, parentNode.getNode(relPath));
    return false; 
  }

  public String getCurrentNodeKey() {
    return currentNodeKey;
  }

  public String getPathKey() {
    return pathKey;
  }

  public String getResultKey() {
    return resultKey;
  }


}
