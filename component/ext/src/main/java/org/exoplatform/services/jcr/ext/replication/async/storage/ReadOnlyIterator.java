/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import java.util.Iterator;

import org.exoplatform.services.jcr.dataflow.ItemState;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 09.02.2009
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: ReadOnlyIterator.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class ReadOnlyIterator<S extends ItemState> implements Iterator<S> {

  private final Iterator<S> source;
  
  ReadOnlyIterator(Iterator<S> source) {
    this.source = source;
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean hasNext() {
    return source.hasNext();
  }

  /**
   * {@inheritDoc}
   */
  public S next() {
    return source.next();
  }

  /**
   * {@inheritDoc}
   */
  public void remove() {
    throw new RuntimeException("Not implemented");
  }

}
