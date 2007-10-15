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
 * @version $Id: GetPropertyCommand.java 5800 2006-05-28 18:03:31Z geaz $
 */

public class GetPropertyCommand implements Command {
  
  private String pathKey = DefaultKeys.PATH;
  private String currentNodeKey = DefaultKeys.CURRENT_NODE;
  private String resultKey = DefaultKeys.RESULT;
  
  public boolean execute(Context context) throws Exception {
    Session session = ((JCRAppContext)context).getSession();
    String relPath = (String)context.get(pathKey);
    Node parentNode = (Node)session.getItem((String)context.get(currentNodeKey));
    
    context.put(resultKey, parentNode.getProperty(relPath));
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
