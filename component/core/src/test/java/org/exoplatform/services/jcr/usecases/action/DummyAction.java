/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.usecases.action;

import org.apache.commons.chain.Context;
import org.exoplatform.services.command.action.Action;


/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: DummyAction.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class DummyAction implements Action {
  private  int actionExecuterCount = 0;
  public boolean execute(Context ctx) throws Exception {
    System.out.println("EXECUTE !!! ");
    actionExecuterCount++;
    return false;
  }
  public int getActionExecuterCount() {
    return actionExecuterCount;
  }
  public  void setActionExecuterCount(int actionExecuterCount) {
    this.actionExecuterCount = actionExecuterCount;
  }

}
