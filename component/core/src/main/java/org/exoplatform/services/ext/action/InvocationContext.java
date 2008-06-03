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
package org.exoplatform.services.ext.action;

import java.util.HashMap;

import javax.jcr.Item;

import org.apache.commons.chain.Context;
import org.apache.poi.hssf.record.formula.functions.T;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.jcr.impl.core.ItemImpl;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: InvocationContext.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class InvocationContext extends HashMap implements Context {
  /**
   * Exo container.
   */
  public static final String EXO_CONTAINER = "exocontainer".intern();

  /**
   * Current item.
   */
  public static final String CURRENT_ITEM  = "currentItem".intern();

  /**
   * Context event.
   */
  public static final String EVENT         = "event".intern();

  public Object put(String key, ItemImpl item) {
    return super.put(key, item);
  }
  
  public Object put(String key, ExoContainer container) {
    return super.put(key, container);
  }

  public Object put(String key, int eventType) {
    return super.put(key, eventType);
  }

  public boolean getBoolean(final String name) {
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
  
  public String getString(final String name) {
    if (!containsKey(name))
      return null;
    return (String) (get(name));
  }
}
