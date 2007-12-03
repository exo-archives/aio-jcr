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
package org.exoplatform.services.jcr.rmi.impl.server;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRow;
import org.exoplatform.services.jcr.rmi.api.value.SerialValueFactory;

/**
 * Remote adapter for the JCR {@link javax.jcr.query.Row Row} interface. This
 * class makes a local session available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteRow RemoteRow}
 * interface.
 * 
 * @see javax.jcr.query.Row
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteRow
 */
public class ServerRow extends ServerObject implements RemoteRow {

  /**
   * 
   */
  private static final long serialVersionUID = 245247508563945975L;

  /** The adapted local row. */
  private Row               row;

  /**
   * Creates a remote adapter for the given local query row.
   * 
   * @param row local query row
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerRow(Row row, RemoteAdapterFactory factory) throws RemoteException {
    super(factory);
    this.row = row;
  }

  /** {@inheritDoc} */
  public Value[] getValues() throws RepositoryException, RemoteException {
    return SerialValueFactory.makeSerialValueArray(row.getValues());
  }

  /** {@inheritDoc} */
  public Value getValue(String propertyName) throws RepositoryException, RemoteException {
    return SerialValueFactory.makeSerialValue(row.getValue(propertyName));
  }
}
