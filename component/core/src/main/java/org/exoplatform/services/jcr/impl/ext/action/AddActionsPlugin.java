/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.ext.action;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: AddActionsPlugin.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class AddActionsPlugin extends BaseComponentPlugin {
  
  private ActionsConfig actionsConfig;
  
  public AddActionsPlugin(InitParams params) {
    ObjectParameter param = params.getObjectParam("actions");
    
    if (param != null) {
      actionsConfig = (ActionsConfig)param.getObject();
    }
  }

  public List<ActionConfiguration> getActions() {
    return actionsConfig.getActions();
  }
  
  public static class ActionsConfig {
    private List<ActionConfiguration> actions = new ArrayList<ActionConfiguration>();

    public List<ActionConfiguration> getActions() {
      return actions;
    }

    public void setActions(List<ActionConfiguration> actions) {
      this.actions = actions;
    }
    
  }
  
}
