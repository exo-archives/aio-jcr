/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.cli;

import javax.jcr.Workspace;

/**
 * Created by The eXo Platform SARL
 * @author Vitaliy Obmanjuk
 * @version $Id: $
 */

public class MoveNodeCommand extends AbstractCliCommand {
  
  public boolean perform(CliAppContext ctx) {
    String output = "";
    try {
      String srcAbsPath = ctx.getParameter(0);
      String destAbsPath = ctx.getParameter(1);
      Workspace workspace = ctx.getSession().getWorkspace();
      workspace.move(srcAbsPath, destAbsPath);
      output = "Node [" + srcAbsPath + "] has been moved to [" + destAbsPath + "] successfully \n";
    }catch (Exception e) {
      output = "Can't execute command - " + e.getMessage() + "\n";
    }
    ctx.setOutput(output);
    return false;
  }
}
