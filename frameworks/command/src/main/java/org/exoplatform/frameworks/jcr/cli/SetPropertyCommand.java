/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.cli;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;

/**
 * Created by The eXo Platform SARL
 * @author Vitaliy Obmanjuk
 * @version $Id: $
 */

public class SetPropertyCommand extends AbstractCliCommand {
  
  public boolean perform(CliAppContext ctx) {
    String output = "";
    try {
      int parametersCount = ctx.getParameters().size();
      String propertyName = ctx.getParameter(0);
      String propertyValue = ctx.getParameter(1);
      String propertyType = parametersCount == 3 ? ctx.getParameter(2) : null;  
      Node node = (Node) ctx.getCurrentItem();
      Property property = null;
      if (propertyType == null) {
        property = node.setProperty (propertyName, propertyValue);
      }else {
        int correctPropertyType = new Integer(propertyType);
        property = node.setProperty (propertyName, propertyValue, correctPropertyType);
      }
      ctx.getSession().save();
      output = "Property: " + propertyName + " , " + propertyValue + " created succesfully \n";
      ctx.setCurrentItem(property);
    }catch (Exception e) {
      output = "Can't execute command - " + e.getMessage() + "\n";
    }  
    ctx.setOutput(output);
    return false;
  }
}
