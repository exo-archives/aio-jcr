/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.cli;

import javax.jcr.Item;

/**
 * Created by The eXo Platform SARL
 * @author Vitaliy Obmanjuk
 * @version $Id: $
 */

public class RemoveItemCommand extends AbstractCliCommand {
  
  public boolean perform(CliAppContext ctx) {
    String output = "";
    try {
      Item currentItem = ctx.getCurrentItem();
      Item parentItem = currentItem.getParent();
      currentItem.remove();
      ctx.getSession().save();
      output = "Item " + currentItem.getPath() + " removed succesfully \n";
      ctx.setCurrentItem(parentItem);
      output = "Current item:  " + ctx.getCurrentItem().getPath() + "\n";
    }catch (Exception e) {
      output = "Can't execute command - " + e.getMessage() + "\n";
    }
    ctx.setOutput(output);
    return false;
  }
}
