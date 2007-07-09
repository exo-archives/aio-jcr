/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.cli;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL
 * @author Vitaliy Obmanjuk 
 * @version $Id: $
 */

public class AddNodeCommand extends AbstractCliCommand {
  
  public boolean perform(CliAppContext ctx) {
    String output = "";
    try {
      int parametersCount = ctx.getParameters().size();
      String nodeName = ctx.getParameter(0);
      //String nodeType = ctx.getParameter(1);
      String nodeType = parametersCount == 2 ? ctx.getParameter(1) : null;
      Node curNode = (Node) ctx.getCurrentItem();
      Node newNode = null;
      if (nodeType == null) {
        newNode = curNode.addNode(nodeName);
      }else {
        newNode = curNode.addNode(nodeName, nodeType);
      }
      ctx.getSession().save();
      ctx.setCurrentItem(newNode);
      output = "Node: " + newNode.getPath() + " created succesfully \n";
    }catch (Exception e) {
      output = "Can't execute command - " + e.getMessage() + "\n";
    }
    ctx.setOutput(output);
    return false;
  }
}
