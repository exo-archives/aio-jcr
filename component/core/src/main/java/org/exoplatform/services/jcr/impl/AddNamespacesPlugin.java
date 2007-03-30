/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

/**
 * Created by The eXo Platform SARL        .
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: AddNamespacesPlugin.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class AddNamespacesPlugin extends BaseComponentPlugin {

  private Map namespaces = new HashMap();

  public AddNamespacesPlugin(InitParams params) {
    PropertiesParam param = params.getPropertiesParam("namespaces");
    
    if (param != null) {
      namespaces = param.getProperties();
    }
  }

  public Map getNamespaces() {
    return namespaces;
  }
  
}
