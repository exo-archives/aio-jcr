/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.client;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRow;

/**
 * Local adapter for the JCR-RMI {@link RemoteRow RemoteRow} inteface. This
 * class makes a remote query row locally available using the JCR
 * {@link Row Row} interface.
 * 
 * @see javax.jcr.query.Row Row
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteRow
 */
public class ClientRow implements Row {

  /** The remote query row. */
  private RemoteRow remote;

  /**
   * Creates a client adapter for the given remote query row.
   * 
   * @param remote remote query row
   */
  public ClientRow(RemoteRow remote) {
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public Value[] getValues() throws RepositoryException {
    try {
      return remote.getValues();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Value getValue(String s) throws RepositoryException {
    try {
      return remote.getValue(s);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }
}
