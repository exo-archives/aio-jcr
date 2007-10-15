/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.cli;

import javax.jcr.Node;
import javax.jcr.Property;

/**
 * Created by The eXo Platform SARL
 * @author Vitaliy Obmanjuk
 * @version $Id: $
 */

public class GetPropertyCommand extends AbstractCliCommand {

  @Override
  public boolean perform(CliAppContext ctx) {
    String output = "";
    try {
      String relPath = ctx.getParameter(0);
      Node currentNode = (Node)ctx.getCurrentItem();
      Property resultProperty = currentNode.getProperty(relPath);
      ctx.setCurrentItem(resultProperty);
      try {
       output = "Current property value: " + resultProperty.getValue().getString() + "\n";
      }catch (Exception e) {
       output = "Can't display the property value"; 
      }
    }catch (Exception e) {
      output = "Can't execute command - " + e.getMessage() + "\n";
    }
    ctx.setOutput(output);
    return false;
  }
}
