/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.cli;

import java.util.Iterator;

import javax.jcr.Item;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;

/**
 * Created by The eXo Platform SARL
 * @author Vitaliy Obmanjuk
 * @version $Id: $
 */

public class GetContextInfoCommand extends AbstractCliCommand {
  
  public boolean perform(CliAppContext ctx) {
    String output = "";
    try {
      output = "Context info: \n";
      Item item = ctx.getCurrentItem();
      ItemDefinition itemDefinition;
      if (item.isNode()){
        itemDefinition = ((NodeImpl)item).getDefinition();
      } else {
        itemDefinition = ((PropertyImpl)item).getDefinition();
      }
      output += "username: " + ctx.getUserName() + "\n";
      output += "workspace: " + ctx.getCurrentWorkspace() + "\n";
      output += "item path: " + item.getPath() + "\n";
      String itemType = item.isNode() ? "Node" : "Property";
      output += "item type: " + itemType + "\n";
      output += "item definitions:\n";
      output += "  name: " + itemDefinition.getName() + "\n";
      output += "  autocreated:" + itemDefinition.isAutoCreated() + "\n";
      output += "  mandatory:" + itemDefinition.isMandatory() + "\n";
      output += "  protected:" + itemDefinition.isProtected() + "\n";
      output += "  onparentversion:" + itemDefinition.getOnParentVersion() + "\n";
      if (item.isNode() == false){
        Property property = (Property)item;
        int propertyType  = property.getValue().getType();
        if (propertyType != (PropertyType.BINARY)){
          PropertyDefinition propertyDefinition = (PropertyDefinition)itemDefinition;
          if (propertyDefinition.isMultiple() == false) {
            output += "property value:" + property.getValue().getString() + "\n";
          }else{
            output += "property value is multiple" + "\n";
          }
        } else {
          output += "can't show property value:" + "\n";
        }
      }
      output += "parameters:\n";
      Iterator parametersIterator = ctx.getParameters().iterator();
      int i = 0; 
      while (parametersIterator.hasNext()) {
        output += "  [" + i + "]" + " : " + (String)parametersIterator.next() + "\n";
        i++;
      }
    }catch (Exception e) {
      output = "Can't execute command - " + e.getMessage() + "\n";
    }  
    ctx.setOutput(output);
    return false;
  }
}
