/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command;

import javax.jcr.Node;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: GetNodeAsXMLCommand.java 5800 2006-05-28 18:03:31Z geaz $
 */

public class GetNodeAsXMLCommand implements Command {

  
  private String pathKey = DefaultKeys.PATH;
  private String incomingNodeKey = DefaultKeys.RESULT;
  private String resultKey = DefaultKeys.RESULT;


  public boolean execute(Context context) throws Exception {
    Object obj = context.get(incomingNodeKey);
    if(obj == null || !(obj instanceof Node))
      throw new Exception("Invalid incoming node "+obj);
    Node node = (Node)context.get(incomingNodeKey);
    String xml = "<node path='"+node.getPath()+"'/>";
    context.put(resultKey, xml);
    return false;
  }

  public String getIncomingNodeKey() {
    return incomingNodeKey;
  }

  public String getPathKey() {
    return pathKey;
  }

  public String getResultKey() {
    return resultKey;
  }

}
