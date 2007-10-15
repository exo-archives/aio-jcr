/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
