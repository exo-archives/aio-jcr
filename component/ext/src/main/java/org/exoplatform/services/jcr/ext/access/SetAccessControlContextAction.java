/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.access;

import org.apache.commons.chain.Context;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.access.AccessManager;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class SetAccessControlContextAction implements Action {

  public boolean execute(Context ctx) throws Exception {
    AccessManager accessManager = (AccessManager) ((ExoContainer) ctx.get("exocontainer"))
    .getComponentInstanceOfType(AccessManager.class);
    
    accessManager.setContext((InvocationContext)ctx);

    return false;
  }

}
