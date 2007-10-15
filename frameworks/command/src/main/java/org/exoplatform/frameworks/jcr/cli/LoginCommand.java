/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.cli;

import javax.jcr.Node;
import javax.jcr.Repository;

import org.exoplatform.services.jcr.core.ManageableRepository;

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
      String workspace = null;
      if (ctx.getParameters().size()>=1) { //ws parameter is present
        workspace = ctx.getParameter(0);   
      }else {
        workspace = ctx.getSession().getWorkspace().getName();
      }
      //ctx.getSession().getRepository().login(workspace);
      ctx.setCurrentWorkspace(workspace);
      Node root = ctx.getSession().getRootNode();
      output = "Successfully logged into workspace " + workspace + " as " + ctx.getSession().getUserID() + "\n";
      ctx.setCurrentItem(root);
    }catch (Exception e) {
      //e.printStackTrace();
      output = "Can't execute command - " + e.getMessage() + "\n";
    }
    ctx.setOutput(output);
    return false;
  }
}
