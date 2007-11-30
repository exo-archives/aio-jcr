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

package org.exoplatform.services.jcr.impl.ext.action;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;

/**
 * Created by The eXo Platform SAS.
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
