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
 * @version $Id: AddNodeCommand.java 5800 2006-05-28 18:03:31Z geaz $
 */

public class AddNodeCommand implements Command {

  private String pathKey = DefaultKeys.PATH;
  private String currentNodeKey = DefaultKeys.CURRENT_NODE;
  private String resultKey = DefaultKeys.RESULT;
  private String nodeTypeKey = DefaultKeys.NODE_TYPE;

  public boolean execute(Context context) throws Exception {
    
    Session session = ((JCRAppContext)context).getSession();
  
    Node parentNode = (Node)session.getItem((String)context.get(currentNodeKey));
    String relPath = (String)context.get(pathKey);
    if(context.containsKey(nodeTypeKey))   
      context.put(resultKey, parentNode.addNode(relPath, (String)context.get(nodeTypeKey)));
    else
      context.put(resultKey, parentNode.addNode(relPath));

    return true; 
  }

  public String getResultKey() {
    return resultKey;
  }

  public String getCurrentNodeKey() {
    return currentNodeKey;
  }

  public String getNodeTypeKey() {
    return nodeTypeKey;
  }

  public String getPathKey() {
    return pathKey;
  }
}
