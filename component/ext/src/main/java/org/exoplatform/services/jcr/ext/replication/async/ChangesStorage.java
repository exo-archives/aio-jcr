/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.CompositeChangesLog;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public abstract class ChangesStorage<T extends CompositeChangesLog> implements Iterable<T> {

  private final List<T> storage;
  
  protected ChangesStorage() {
    this.storage = new ArrayList<T>();
  }
  
  /**
   * Add changes to a storage.
   *
   * @param changes CompositeChangesLog
   */
  public void add(T changes) {
    this.storage.add(changes);
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<T> iterator() {
    return this.storage.iterator();
  }
    
  /**
   * Clear storage.
   *
   */
  public void clear() {
    this.storage.clear();
  }
  
}
