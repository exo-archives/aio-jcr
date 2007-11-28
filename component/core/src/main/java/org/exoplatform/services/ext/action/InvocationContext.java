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
 * Created by The eXo Platform SARL.
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class InvocationContext extends HashMap implements Context {
  /**
   * Exo container.
   */
  public static final String EXO_CONTAINER = "exocontainer";

  /**
   * Current item.
   */
  public static final String CURRENT_ITEM  = "currentItem";

  /**
   * Context event.
   */
  public static final String EVENT         = "event";

  public boolean getBoolean(String name) {
    if (!containsKey(name))
      return false;
    return (Boolean) (get(name));
  }

  /**
   * @return Exo container
   */
  public final ExoContainer getContainer() {
    return (ExoContainer) get(EXO_CONTAINER);
  }

  /**
   * @return Current item.
   */
  public final Item getCurrentItem() {
    return (Item) get(CURRENT_ITEM);
  }

  /**
   * @return Context event
   */
  public final int getEventType() {
    return (Integer) get(EVENT);
  }

  public String getString(String name) {
    if (!containsKey(name))
      return null;
    return (String) (get(name));
  }
}
