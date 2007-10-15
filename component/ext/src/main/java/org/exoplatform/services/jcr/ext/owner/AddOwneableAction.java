/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.owner;

import org.apache.commons.chain.Context;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.jcr.impl.core.NodeImpl;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: AddOwneableAction.java 12017 2007-01-17 16:26:04Z ksm $
 */

public class AddOwneableAction implements Action {

  public boolean execute(Context ctx) throws Exception {
    NodeImpl node = (NodeImpl)ctx.get("currentItem");
    if(node != null && node.canAddMixin("exo:owneable")){
      node.addMixin("exo:owneable");
    }
    return false;
  }

}
