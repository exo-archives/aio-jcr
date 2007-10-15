/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.ext.action;

import java.util.HashMap;

import javax.jcr.Item;

import org.apache.commons.chain.Context;
import org.exoplatform.container.ExoContainer;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class InvocationContext extends HashMap implements Context {

  public Item getCurrentItem() {
    return (Item)get("currentItem");
  }
  
  public int getEventType() {
    return (Integer)get("event");
  }
  
  public ExoContainer getContainer() {
    return (ExoContainer)get("exocontainer");
  }

}
