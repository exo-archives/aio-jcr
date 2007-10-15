/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.cli;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;

/**
 * Created by The eXo Platform SARL
 * @author Vitaliy Obmanjuk
 * @version $Id: $
 */

public class GetItemCommand extends AbstractCliCommand {

  @Override
  public boolean perform(CliAppContext ctx) {
    String output = "";
    try {
      String path = ctx.getParameter(0);
      Item resultItem = null;
      if (path.equals("..")) {
        Item currentItem = ctx.getCurrentItem();
        resultItem = currentItem.getParent();
      }else {
        //here we need to analize what kind of the path we have: relPath or absPath
        if (path.startsWith("/")) {//absPath
          resultItem = ctx.getSession().getItem(path);
        }else {//relPath
          if (ctx.getCurrentItem().isNode()) {
            Node node = (Node)ctx.getCurrentItem();
            resultItem = node.getNode(path);
          }else {//do nothing
            resultItem = ctx.getCurrentItem();
          }
        }
      }
      ctx.setCurrentItem(resultItem);
      output = "Current item: " + ctx.getCurrentItem().getPath() + "\n";
    }catch (Exception e) {
      output = "Can't execute command - " + e.getMessage() + "\n";
    }
    ctx.setOutput(output);
    return false;
  }
}
