/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.cli;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;

/**
 * Created by The eXo Platform SARL
 * 
 * @author Vitaliy Obmanjuk
 * @version $Id: $
 */

public class GetNodesCommand extends AbstractCliCommand {

  @Override
  public boolean perform(CliAppContext ctx) {
    String output = "";
    try {
      if (ctx.getCurrentItem().isNode()) {
        Node currentNode = (Node) ctx.getCurrentItem();
        PropertyIterator propertyIterator = currentNode.getProperties();
        output += "Properties list for " + currentNode.getPath() + ":\n";
        while (propertyIterator.hasNext()) {
          Property property = propertyIterator.nextProperty();
          output += property.getName() + "\n";
        }
        NodeIterator nodeIterator = currentNode.getNodes();
        output += "Nodes list for " + currentNode.getPath() + ":\n";
        while (nodeIterator.hasNext()) {
          Node node = nodeIterator.nextNode();
          output += node.getPath() + "\n";
        }
      }else {
        output += "Current item is property: " + ((Property)ctx.getCurrentItem()).getName() + "\n";
      }
    } catch (Exception e) {
      output = "Can't execute command - " + e.getMessage() + "\n";
    }
    ctx.setOutput(output);
    return false;
  }
}
