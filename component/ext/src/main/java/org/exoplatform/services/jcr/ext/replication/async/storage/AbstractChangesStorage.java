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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.IOException;
import java.util.Iterator;

import org.exoplatform.services.jcr.dataflow.ItemState;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 16.01.2009
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public abstract class AbstractChangesStorage<T extends ItemState> implements ChangesStorage<T> {

  /**
   * {@inheritDoc}
   */
  public String dump() throws ClassCastException, IOException, ClassNotFoundException {
    StringBuilder str = new StringBuilder();
    str.append("\r\nState\tID\t\t\t\t\tPersist\tEvent\tInternl\tPath\r\n");
    
    for (Iterator<T> iter = this.getChanges(); iter.hasNext();) {
      T state = iter.next();
      str.append(ItemState.nameFromValue(state.getState()));
      str.append("\t");
      str.append(state.getData().getIdentifier());
      str.append("\t");
      str.append(state.isPersisted());
      str.append("\t");
      str.append(state.isEventFire());
      str.append("\t");
      str.append(state.isInternallyCreated());
      str.append("\t");
      str.append(state.getData().getQPath().getAsString());
      str.append("\r\n");
    }
    
    return str.toString();
  }

  
  
}
