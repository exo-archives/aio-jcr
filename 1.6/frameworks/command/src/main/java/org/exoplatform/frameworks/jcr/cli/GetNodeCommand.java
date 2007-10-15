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

public class GetNodeCommand extends AbstractCliCommand {

  @Override
  public boolean perform(CliAppContext ctx) {
    String output = "";
    try {
      String relPath = ctx.getParameter(0);
      Node currentNode = (Node)ctx.getCurrentItem();
      Node resultNode = currentNode.getNode(relPath);
      ctx.setCurrentItem(resultNode);
      output = "Current node: " + resultNode.getPath() + "\n";
    }catch (Exception e) {
      output = "Can't execute command - " + e.getMessage() + "\n";
    }
    ctx.setOutput(output);
    return false;
  }
}
