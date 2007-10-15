/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.actions;

import org.apache.commons.chain.Context;
import org.exoplatform.services.command.action.Action;


/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: DummyAction.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class DummyAction implements Action {

  public boolean execute(Context ctx) throws Exception {
    System.out.println("EXECUTE !!! ");
    return false;
  }

}
