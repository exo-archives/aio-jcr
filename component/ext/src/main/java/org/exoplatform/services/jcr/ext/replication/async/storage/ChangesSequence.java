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

import java.util.Iterator;

import org.exoplatform.services.jcr.dataflow.ItemState;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 17.12.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: ChangesSequence.java 25190 2008-12-16 23:03:33Z pnedonosko $
 */
public interface ChangesSequence<T extends ItemState> extends ChangesStorage, Iterator<T> {

  /**
   * {@inheritDoc}
   */
  boolean hasNext();
  
  /**
   * {@inheritDoc}
   */
  T next();
  
  /**
   * TODO not sure we need it.
   * next.
   *
   * @param after
   * @return
   */
  T next(T after);
  
  boolean hasPrevious();
  
  T previous();
  
  /**
   * TODO not sure we need it.
   * 
   * previous.
   *
   * @param to
   * @return
   */
  T previous(T to);
  
}
