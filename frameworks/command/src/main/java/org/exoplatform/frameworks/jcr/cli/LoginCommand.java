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

public class LoginCommand extends AbstractCliCommand {

  @Override
  public boolean perform(CliAppContext ctx) {
    String output = "";
    try {
      String ws = ctx.getParameter(0);
      ctx.setCurrentWorkspace(ws);
      Node root = ctx.getSession().getRootNode();
      output = "Successfully logged into: " + ws + "\n";
      ctx.setCurrentItem(root);
    }catch (Exception e) {
      output = "Can't execute command - " + e.getMessage() + "\n";
    }
    ctx.setOutput(output);
    return false;
  }
}
