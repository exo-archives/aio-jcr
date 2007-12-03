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
package org.exoplatform.services.jcr.rmi.api.client.iterator;

import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.exoplatform.services.jcr.rmi.api.client.LocalAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRow;

/**
 * A ClientIterator for iterating remote rows.
 */
public class ClientRowIterator extends ClientIterator implements RowIterator {

  /**
   * Creates a ClientRowIterator instance.
   * 
   * @param iterator remote iterator
   * @param factory local adapter factory
   */
  public ClientRowIterator(RemoteIterator iterator, LocalAdapterFactory factory) {
    super(iterator, factory);
  }

  /**
   * Creates and returns a local adapter for the given remote row.
   * 
   * @param remote remote reference
   * @return local adapter
   * @see ClientIterator#getObject(Object)
   */
  protected Object getObject(Object remote) {
    return getFactory().getRow((RemoteRow) remote);
  }

  /**
   * Returns the next row in this iteration.
   * 
   * @return next row
   * @see RowIterator#nextRow()
   */
  public Row nextRow() {
    return (Row) next();
  }

}
