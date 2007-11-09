/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command.ext;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.exoplatform.frameworks.jcr.command.DefaultKeys;
/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: FilterNodesByTypesCommand.java 5800 2006-05-28 18:03:31Z geaz $
 */

public class FilterNodesByTypesCommand implements Command {
  
  private String pathKey = DefaultKeys.PATH;
  private String incomingNodesKey = DefaultKeys.RESULT;
  private String typesKey = "nodeTypes";
  private String resultKey = DefaultKeys.RESULT;

  public boolean execute(Context context) throws Exception {
    Object obj = context.get(incomingNodesKey);
    if(obj == null || !(obj instanceof NodeIterator))
      throw new Exception("Invalid incoming nodes iterator "+obj);
    NodeIterator nodes = (NodeIterator)obj;
    
    obj = context.get(typesKey);
    if(obj == null || !(obj instanceof String[]))
      throw new Exception("Invalid node types object, expected String[] "+obj);
    String[] nts = (String[])context.get(typesKey);

    List nodes1 = new ArrayList();
    while(nodes.hasNext()) {
      Node n = nodes.nextNode();
      for(int i=0; i<nts.length; i++) {
        if(n.isNodeType(nts[i]))
          nodes1.add(n);
      }
    }
    //context.put(resultKey, new EntityCollection(nodes1));
    context.put(resultKey, nodes1);
    
    return false;
  }

  public String getIncomingNodesKey() {
    return incomingNodesKey;
  }

  public String getPathKey() {
    return pathKey;
  }

  public String getResultKey() {
    return resultKey;
  }

}
