/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.frameworks.jcr.cli;

import javax.jcr.Node;
import javax.jcr.Repository;

import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SAS
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
