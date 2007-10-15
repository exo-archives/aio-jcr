/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command.core;

import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.Session;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.exoplatform.frameworks.jcr.command.DefaultKeys;
import org.exoplatform.frameworks.jcr.command.JCRAppContext;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SetPropertyCommand.java 7137 2006-07-18 15:01:20Z vetal_ok $
 */

public class SetPropertyCommand implements Command {
  
  private String nameKey = DefaultKeys.NAME;
  private String currentNodeKey = DefaultKeys.CURRENT_NODE;
  private String resultKey = DefaultKeys.RESULT;
  private String propertyTypeKey = DefaultKeys.PROPERTY_TYPE;
  private String valuesKey = DefaultKeys.VALUES;
  private String multiValuedKey = DefaultKeys.MULTI_VALUED;

  public boolean execute(Context context) throws Exception {
    
    Session session = ((JCRAppContext)context).getSession();
  
    Node parentNode = (Node)session.getItem((String)context.get(currentNodeKey));
    String name = (String)context.get(nameKey);
    
    int type = PropertyType.valueFromName((String)context.get(propertyTypeKey));
    boolean multi;// = ((Boolean)context.get(multiValuedKey)).booleanValue();
    if (context.get(multiValuedKey).equals("true")){
      multi = true; 
    } else {
      multi = false;
    }
    Object values = context.get(valuesKey);
    if(values instanceof String)
      context.put(resultKey, parentNode.setProperty(name, (String)values, type));
    else if(values instanceof String[])
      context.put(resultKey, parentNode.setProperty(name, (String[])values, type));
    else if(values instanceof InputStream)
      context.put(resultKey, parentNode.setProperty(name, (InputStream)values));
    else
      throw new Exception("Values other than String, String[], InputStream is not supported");
    
    return false; 
  }


}
