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

import java.util.Iterator;

import javax.jcr.Item;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.nodetype.PropertyDefinition;

import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;

/**
 * Created by The eXo Platform SAS
 * 
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
      if (item.isNode()) {
        itemDefinition = ((NodeImpl) item).getDefinition();
      } else {
        itemDefinition = ((PropertyImpl) item).getDefinition();
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
      output += "  onparentversion:" + itemDefinition.getOnParentVersion()
          + "\n";
      if (item.isNode() == false) {
        Property property = (Property) item;
        int propertyType = property.getValue().getType();
        if (propertyType != (PropertyType.BINARY)) {
          PropertyDefinition propertyDefinition = (PropertyDefinition) itemDefinition;
          if (propertyDefinition.isMultiple() == false) {
            output += "property value:" + property.getValue().getString()
                + "\n";
          } else {
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
        output += "  [" + i + "]" + " : " + (String) parametersIterator.next()
            + "\n";
        i++;
      }
    } catch (Exception e) {
      output = "Can't execute command - " + e.getMessage() + "\n";
    }
    ctx.setOutput(output);
    return false;
  }
}
